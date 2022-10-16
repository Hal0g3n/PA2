package p2pOverlay.model;

import java.util.ArrayList;
import java.util.BitSet;

public class JoinMessage extends Message {
    private ArrayList<Connection> ringClockwise;
    private ArrayList<Connection> ringAnticlockwise;
    private boolean performInsertions;
    private Connection joiningNode;
    private final int originalLvl;
    private int ringLvl;

    public JoinMessage(Connection sourceNode, String messageContent, String messageCommand, boolean performInsertions, Connection joiningNode, int ringLvl) {
        super(sourceNode, messageContent, messageCommand);

        this.performInsertions = performInsertions;
        this.joiningNode = joiningNode;
        this.ringLvl = ringLvl;
        this.originalLvl = ringLvl;
        this.ringClockwise = new ArrayList<>();
        this.ringAnticlockwise = new ArrayList<>();

        for(int i = 0; i <= ringLvl; i++){
            ringClockwise.add(null);
            ringAnticlockwise.add(null);
        }

    }

    public Connection getJoiningNode() {
        return joiningNode;
    }

    public void setJoiningNode(Connection joiningNode) {
        this.joiningNode = joiningNode;
    }

    public Connection getRingClockwise(int ringLvl) {
        return ringClockwise.get(ringLvl);
    }

    public void setRingClockwise(int ringLvl, Connection ringClockwise) {
        this.ringClockwise.set(ringLvl, ringClockwise);
    }

    public Connection getRingAnticlockwise(int ringLvl) {
        return ringAnticlockwise.get(ringLvl);
    }

    public void setRingAnticlockwise(int ringLvl, Connection ringAnticlockwise) {
        this.ringAnticlockwise.set(ringLvl, ringAnticlockwise);
    }

    public boolean isPerformInsertions() {
        return performInsertions;
    }

    public void setPerformInsertions(boolean performInsertions) {
        this.performInsertions = performInsertions;
    }

    public int getRingLvl() {
        return ringLvl;
    }

    public void setRingLvl(int ringLvl) {
        this.ringLvl = ringLvl;
    }

    public int getOriginalLvl() {
        return originalLvl;
    }

}
