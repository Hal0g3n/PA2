package p2pOverlay.handlers;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ConnectionServerHandler extends SimpleChannelInboundHandler {

    private String constructedMessage;
    private PropertyChangeSupport support;

    public ConnectionServerHandler(PropertyChangeListener listener){
        this.constructedMessage = "";
        support.addPropertyChangeListener(listener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        String inputChar = ((ByteBuf) msg).toString(CharsetUtil.US_ASCII);
        if(inputChar.equals("\n")){
            support.firePropertyChange("message", this.constructedMessage, this.constructedMessage);
            this.constructedMessage = "";
        } else this.constructedMessage+=inputChar;
    }
}
