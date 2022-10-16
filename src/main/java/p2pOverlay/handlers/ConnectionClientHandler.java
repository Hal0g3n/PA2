package p2pOverlay.handlers;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import p2pOverlay.model.message.Message;
import p2pOverlay.services.PeerService;

public class ConnectionClientHandler extends SimpleChannelInboundHandler<Object> {

    private Message content;
    private ChannelHandlerContext ctx;
    private PeerService ps;

    public ConnectionClientHandler(Message msg, PeerService ps){
        this.content = msg;
        this.ps = ps;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;

        // object is serialised in the channel pipeline... supposedly

        ChannelFuture cf = ctx.writeAndFlush(this.content);
        cf.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // when a message is received, we forward it over to PeerService
        Message receivedMsg = (Message) msg;
        System.out.printf("Received %s\n", receivedMsg.getMessageContent());
        ps.handleImmediateMessage(ctx, receivedMsg);
        System.out.printf("%s\n", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
