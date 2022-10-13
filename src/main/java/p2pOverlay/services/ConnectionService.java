package p2pOverlay.services;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import p2pOverlay.DiscardClientHandler;
import p2pOverlay.DiscardServerHandler;
import p2pOverlay.handlers.ConnectionServerHandler;
import p2pOverlay.util.ServerUtil;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionService {

    /*
    *
    * Both client and server logic should be handled here
    * Bootstrap for the server will be init'd here
    *
    * */

    private Queue<String> incomingMessages;

    public ConnectionService() throws InterruptedException {
        this.incomingMessages = new LinkedList<>();
        startServer(8009, incomingMessages);
    }

    public String getMessage(){
        if (!incomingMessages.isEmpty()) return incomingMessages.remove();
        else return null;
    }

    public ChannelFuture startServer(int port, Queue<String> msgQueue) throws InterruptedException {
        ChannelFuture closeFuture = null;
        try {
            final SslContext sslCtx = ServerUtil.buildSslContext();
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                if (sslCtx != null) {
                                    p.addLast(sslCtx.newHandler(ch.alloc()));
                                }
                                p.addLast(new ConnectionServerHandler(msgQueue));
                            }
                        });

                // Bind and start to accept incoming connections.
                closeFuture = b.bind(port).sync();

                // Wait until the server socket is closed.
                // In this example, this does not happen, but you can do that to gracefully
                // shut down your server.
                closeFuture.channel().closeFuture().sync();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        } catch (CertificateException | SSLException e) {
            throw new RuntimeException(e);
        }
        return closeFuture;
    }

    public void sendMessage(String message, String ip, int port){
        final SslContext sslCtx;
        try {
            sslCtx = ServerUtil.buildSslContext();
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                if (sslCtx != null) {
                                    p.addLast(sslCtx.newHandler(ch.alloc(), ip, port));
                                }
                                p.addLast(new DiscardClientHandler());
                            }
                        });

                // Make the connection attempt.
                ChannelFuture f = b.connect(ip, port).sync();

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                group.shutdownGracefully();
            }
        } catch (CertificateException | SSLException e) {
            throw new RuntimeException(e);
        }
    }

}
