package p2pOverlay.model;

public class Message {
    private Connection sourceNode;
    private String messageContent;

    public Message(Connection sourceNode, String messageContent) {
        this.sourceNode = sourceNode;
        this.messageContent = messageContent;
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
}
