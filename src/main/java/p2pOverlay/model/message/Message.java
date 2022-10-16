package p2pOverlay.model.message;

import io.netty.handler.codec.MessageToMessageEncoder;
import p2pOverlay.model.Connection;

import java.io.Serializable;

public class Message implements Serializable {
    private Connection sourceNode;
    private String messageContent;
    private String messageCommand;

    public Message(Connection sourceNode, String messageContent, String messageCommand) {
        this.sourceNode = sourceNode;
        this.messageContent = messageContent;
        this.messageCommand = messageCommand;
    }

    public Connection getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Connection sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageCommand() {
        return messageCommand;
    }

    public void setMessageCommand(String messageCommand) {
        this.messageCommand = messageCommand;
    }
}
