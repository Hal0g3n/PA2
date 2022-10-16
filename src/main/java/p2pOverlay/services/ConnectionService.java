package p2pOverlay.services;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import p2pOverlay.handlers.ConnectionClientHandler;
import p2pOverlay.handlers.ConnectionServerHandler;
import p2pOverlay.model.Message;
import p2pOverlay.util.ServerUtil;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class ConnectionService {

    /*
    *
    * Both client and server logic should be handled here
    * Bootstrap for the server will be init'd here
    *
    * */

    private PeerService ps;

    public ConnectionService(PeerService ps, int port) throws InterruptedException {
        this.ps = ps;
        startServer(port, ps);
    }

    public ChannelFuture startServer(int port, PeerService ps) throws InterruptedException {
        ChannelFuture closeFuture = null;
        try {
            final SslContext sslCtx = ServerUtil.buildSslContext();
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.SO_BACKLOG, 100)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                if (sslCtx != null) {
                                    p.addLast(sslCtx.newHandler(ch.alloc()));
                                }
                                p.addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ConnectionServerHandler(ps)
                                );
                            }
                        });

                // Bind and start to accept incoming connections.
                final ChannelFuture bindFuture = b.bind(port).sync();

                if(bindFuture.isSuccess()){
                    System.out.printf("Server bound on localhost:%d\n",port);
                    final Channel serverChannel = bindFuture.channel();
                    closeFuture = serverChannel.closeFuture();
                }
            } finally {

            }
        } catch (CertificateException | SSLException e) {
            throw new RuntimeException(e);
        }
        return closeFuture;
    }

    public void sendMessage(Message message, String ip, int port){
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

                                p.addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ConnectionClientHandler(message, ps)
                                );

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
