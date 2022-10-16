package p2pOverlay.model;

import java.util.BitSet;

public class RouteMessage extends Message {
    private BitSet destId;
    private Connection bestNode;
    private int ringLevel;
    private boolean finalDestination;

    public RouteMessage(Connection sourceNode, String messageContent, String messageCommand, BitSet destId, Connection bestNode, int ringLevel, boolean finalDestination) {
        super(sourceNode, messageContent, messageCommand);
        this.destId = destId;
        this.bestNode = bestNode;
        this.ringLevel = ringLevel;
        this.finalDestination = finalDestination;
    }

    public boolean isFinalDestination() {
        return finalDestination;
    }

    public void setFinalDestination(boolean finalDestination) {
        this.finalDestination = finalDestination;
    }

    public BitSet getDestId() {
        return destId;
    }

    public void setDestId(BitSet destId) {
        this.destId = destId;
    }

    public Connection getBestNode() {
        return bestNode;
    }

    public void setBestNode(Connection bestNode) {
        this.bestNode = bestNode;
    }

    public int getRingLevel() {
        return ringLevel;
    }

    public void setRingLevel(int ringLevel) {
        this.ringLevel = ringLevel;
    }
}
