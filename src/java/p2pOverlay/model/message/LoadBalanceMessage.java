package p2pOverlay.model.message;

import p2pOverlay.model.Connection;

public class LoadBalanceMessage extends Message {
    private int members;
    private int cumLoad;
    private double averageLoad;
    private double loadMultiplier;
    private boolean merging;
    private boolean summing;

    public LoadBalanceMessage(Connection sourceNode, String messageContent, String messageCommand, int members, int cumLoad, double averageLoad, double loadMultiplier, boolean merging, boolean summing) {
        super(sourceNode, messageContent, messageCommand);
        this.members = members;
        this.cumLoad = cumLoad;
        this.averageLoad = averageLoad;
        this.loadMultiplier = loadMultiplier;
        this.merging = merging;
        this.summing = summing;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public int getCumLoad() {
        return cumLoad;
    }

    public void setCumLoad(int cumLoad) {
        this.cumLoad = cumLoad;
    }

    public double getAverageLoad() {
        return averageLoad;
    }

    public void setAverageLoad() {
        this.averageLoad = this.cumLoad*1.0/this.members;
    }

    public double getLoadMultiplier() {
        return loadMultiplier;
    }

    public void setLoadMultiplier(double loadMultiplier) {
        this.loadMultiplier = loadMultiplier;
    }

    public double getThreshold(){ return loadMultiplier*averageLoad;}

    public boolean isMerging() {
        return merging;
    }

    public void setMerging(boolean merging) {
        this.merging = merging;
    }

    public boolean isSumming() {
        return summing;
    }

    public void setSumming(boolean summing) {
        this.summing = summing;
    }
}

