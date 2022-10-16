package p2pOverlay.handlers;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import p2pOverlay.model.message.Message;
import p2pOverlay.services.PeerService;

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
        //String receivedMsg = ((ByteBuf) msg).toString(CharsetUtil.US_ASCII);
        Message receivedMsg = (Message) msg;
        System.out.printf("Received %s\n", receivedMsg.getMessageContent());
        ps.handleImmediateMessage(ctx, receivedMsg);
        this.constructedMessage = "";
        System.out.printf("%s\n", ctx.channel().remoteAddress());
//        ByteBuf out = ctx.alloc().buffer(receivedMsg.length()*2);
//        out.writeBytes(Encoding.str_to_bb(receivedMsg));
//        ctx.writeAndFlush(out);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
