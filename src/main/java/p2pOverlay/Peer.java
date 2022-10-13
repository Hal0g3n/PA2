package p2pOverlay;

import p2pOverlay.util.Connection;

import java.util.ArrayList;

public class Peer {
    // Peer contains two arraylist for its clockwise and anticlockwise neighbours at each level
    // The index of the arraylist corresponds to the level
    private final ArrayList<Connection> clockwise;
    private final ArrayList<Connection> antiClockwise;
    public int id;
    public static ArrayList<Connection>[] routeTable;

    public Peer(int id, ArrayList<Connection> clockwise, ArrayList<Connection> antiClockwise) {
        this.id = id;
        this.clockwise = clockwise;
        this.antiClockwise = antiClockwise;
        routeTable = new ArrayList[]{clockwise, antiClockwise};
    }
    public Peer(int id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public Peer(){ this(-1); }

    // TODO: check if ring level exists
    public void updateTable(int h, Connection clockwiseNeighbour, Connection antiClockwiseNeighbour) {
        clockwise.set(h, clockwiseNeighbour);
        antiClockwise.set(h, antiClockwiseNeighbour);
    }

}
