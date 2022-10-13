package p2pOverlay.handlers;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import p2pOverlay.services.PeerService;
import p2pOverlay.util.Encoding;

import java.util.Queue;

public class ConnectionServerHandler extends SimpleChannelInboundHandler {

    private String constructedMessage;
    private PeerService ps;

    public ConnectionServerHandler(PeerService ps){
        this.constructedMessage = "";
        this.ps = ps;
        // msgQueue as string for now, have to shift to message later
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        String inputChar = ((ByteBuf) msg).toString(CharsetUtil.US_ASCII);
        System.out.printf("Received %s\n", inputChar);
        ps.handleMessage(ctx, inputChar);
        this.constructedMessage = "";
        System.out.printf("%s\n", ctx.channel().remoteAddress());
//        ByteBuf out = ctx.alloc().buffer(inputChar.length()*2);
//        out.writeBytes(Encoding.str_to_bb(inputChar));
//        ctx.writeAndFlush(out);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
