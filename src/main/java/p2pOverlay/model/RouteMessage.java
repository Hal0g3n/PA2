package p2pOverlay.model;

import java.util.BitSet;

public class RouteMessage extends Message {
    private BitSet destId;
    private Connection bestNode;
    private int ringLevel;

    public RouteMessage(Connection sourceNode, String messageContent, BitSet destId, Connection bestNode, int ringLevel) {
        super(sourceNode, messageContent);
        this.destId = destId;
        this.bestNode = bestNode;
        this.ringLevel = ringLevel;
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
