package p2pOverlay.handlers;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Queue;

public class ConnectionServerHandler extends SimpleChannelInboundHandler {

    private String constructedMessage;
    private Queue<String> msgQueue;

    public ConnectionServerHandler(Queue<String> msgQueue){
        this.constructedMessage = "";
        this.msgQueue = msgQueue;
        // msgQueue as string for now, have to shift to message later
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        String inputChar = ((ByteBuf) msg).toString(CharsetUtil.US_ASCII);
        if(inputChar.equals("\n")){
            System.out.printf("Received %s\n", this.constructedMessage);
            msgQueue.add(this.constructedMessage);
            this.constructedMessage = "";
        } else this.constructedMessage+=inputChar;
    }
}
