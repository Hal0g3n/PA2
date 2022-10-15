package p2pOverlay;

import p2pOverlay.services.PeerService;
import p2pOverlay.model.Connection;
import p2pOverlay.util.Encoding;

import java.util.ArrayList;
import java.util.BitSet;

public class Peer {
    // Peer contains two arraylist for its clockwise and anticlockwise neighbours at each level
    // The index of the arraylist corresponds to the level
    private final ArrayList<Connection> clockwise;
    private final ArrayList<Connection> antiClockwise;
    private BitSet peerID;
    private static final int NBITS = 32;
    public static ArrayList<Connection>[] routeTable;
    private int peerNumber;

    private BitSet numericID;

    // temp arraylist for testing
    private final ArrayList<Connection> knownConnections;
    private ArrayList<Integer> loadWavelet;

    public Peer(BitSet peerID, ArrayList<Connection> clockwise, ArrayList<Connection> antiClockwise) {
        this.peerID = peerID;
        this.clockwise = clockwise;
        this.antiClockwise = antiClockwise;

        routeTable = new ArrayList[]{clockwise, antiClockwise};

        // routeTable[ 0 (clockwise) / 1 (anticlockwise) ][ringLvl]

        this.knownConnections = new ArrayList<>();
    }
    public Peer(BitSet peerID) {
        this(peerID, new ArrayList<>(), new ArrayList<>());
    }
    public Peer (String peerID) {
        this(Encoding.stringToBitSet(peerID));
    }

    public Peer(){ this(new BitSet(NBITS)); }

    public long getLongId() {
        long[] longArray = peerID.toLongArray();

        // if id is set to 0, longArray will have no elements
        if(longArray.length > 0) return longArray[0];
        return 0;
    }

    public void setPeerNumber(int n){this.peerNumber = n;}
    public int getPeerNumber(){ return peerNumber; }

    public Connection getAnticlockwiseNeighbour(int ringLvl){
        return routeTable[1].get(ringLvl);
    }

    public Connection getClockwiseNeighbour(int ringLvl){
        return routeTable[0].get(ringLvl);
    }

    public void setPeerID(int peerID){this.peerID = BitSet.valueOf(new long[] {peerID});}
    public void setId(String id) {
        this.peerID = Encoding.stringToBitSet(id);
    }

    public void updateTable(int h, Connection clockwiseNeighbour, Connection antiClockwiseNeighbour) {
        clockwise.set(h, clockwiseNeighbour);
        antiClockwise.set(h, antiClockwiseNeighbour);
    }


    public BitSet getNumericID() {
        return numericID;
    }

    public void setNumericID(BitSet numericID) {
        this.numericID = numericID;
    }

}
