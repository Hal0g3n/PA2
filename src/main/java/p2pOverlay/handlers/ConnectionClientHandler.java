package p2pOverlay.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import p2pOverlay.services.PeerService;
import p2pOverlay.util.Encoding;

import java.nio.ByteBuffer;

public class ConnectionClientHandler extends SimpleChannelInboundHandler<Object> {

    private String content;
    private ChannelHandlerContext ctx;
    private PeerService ps;

    public ConnectionClientHandler(String msg, PeerService ps){
        this.content = msg;
        this.ps = ps;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ByteBuf out = ctx.alloc().buffer(content.length()*2).writeBytes(Encoding.str_to_bb(content));
        ctx.writeAndFlush(out);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Server is supposed to send nothing, but if it sends something, discard it.
        String receivedMsg = ((ByteBuf) msg).toString(CharsetUtil.US_ASCII);
        System.out.printf("Received %s\n", receivedMsg);
        ps.handleMessage(ctx, receivedMsg);
        System.out.printf("%s\n", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
