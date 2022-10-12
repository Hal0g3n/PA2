package p2pOverlay;

import p2pOverlay.util.Connection;

import java.util.ArrayList;

public record Peer(ArrayList<Connection> clockwise, ArrayList<Connection> antiClockwise) {
    // Peer contains two arraylist for its clockwise and anticlockwise neighbours at each level
    // The index of the arraylist corresponds to the level
    public Peer() {
        this(new ArrayList<>(), new ArrayList<>());
    }
}
