package p2pOverlay;

import java.util.ArrayList;
import java.util.BitSet;
import p2pOverlay.model.Connection;
import p2pOverlay.services.PeerService;
import p2pOverlay.util.Encoding;

public class Peer {

  // Peer contains two arraylist for its clockwise and anticlockwise neighbours at each level
  // The index of the arraylist corresponds to the level
  private final ArrayList<Connection> clockwise;
  private final ArrayList<Connection> antiClockwise;
  private BitSet peerID;
  private static final int NBITS = 32;
  public static ArrayList<Connection>[] routeTable;
  private int peerNumber;
  private int load;
  private BitSet numericID;

  // temp arraylist for testing

  private ArrayList<Integer> loadWavelet;

  public Peer(
    BitSet peerID,
    ArrayList<Connection> clockwise,
    ArrayList<Connection> antiClockwise
  ) {
    this.peerID = peerID;
    this.clockwise = clockwise;
    this.antiClockwise = antiClockwise;
    this.load = 0;
    routeTable = new ArrayList[] { clockwise, antiClockwise };
    // routeTable[ 0 (clockwise) / 1 (anticlockwise) ][ringLvl]
  }

  public Peer(BitSet peerID) {
    this(peerID, new ArrayList<>(), new ArrayList<>());
  }

  public Peer(String peerID) {
    this(Encoding.stringToBitSet(peerID));
  }

  public Peer() {
    this(new BitSet(NBITS));
  }

  public long getLongId() {
    long[] longArray = peerID.toLongArray();

    // if id is set to 0, longArray will have no elements
    if (longArray.length > 0) return longArray[0];
    return 0;
  }

  public void setPeerNumber(int n) {
    this.peerNumber = n;
  }

  public int getPeerNumber() {
    return peerNumber;
  }

  public Connection getAnticlockwiseNeighbour(int ringLvl) {
    if (ringLvl >= routeTable[1].size()) return null;
    return routeTable[1].get(ringLvl);
  }

  public Connection getClockwiseNeighbour(int ringLvl) {
    if (ringLvl >= routeTable[0].size()) return null;
    return routeTable[0].get(ringLvl);
  }

  public void setClockwiseNeighbour(int ringLvl, Connection neighbour) {
    if (ringLvl >= routeTable[0].size()) routeTable[0].add(
        ringLvl,
        neighbour
      ); else routeTable[0].set(ringLvl, neighbour);
  }

  public void setAnticlockwiseNeighbour(int ringLvl, Connection neighbour) {
    if (ringLvl >= routeTable[1].size()) routeTable[1].add(
        ringLvl,
        neighbour
      ); else routeTable[1].set(ringLvl, neighbour);
  }

  public void setPeerID(int peerID, int nLen) {
    this.peerID = Encoding.intToBitSet(peerID, nLen);
  }

  public void setId(String id) {
    this.peerID = Encoding.stringToBitSet(id);
  }

  public BitSet getPeerID() {
    return peerID;
  }

  public void updateTable(
    int h,
    Connection clockwiseNeighbour,
    Connection antiClockwiseNeighbour
  ) {
    clockwise.set(h, clockwiseNeighbour);
    antiClockwise.set(h, antiClockwiseNeighbour);
  }

  public BitSet getNumericID() {
    return numericID;
  }

  public void setNumericID(BitSet numericID) {
    this.numericID = numericID;
  }

  public String routeTableString(int nLen) {
    StringBuilder string = new StringBuilder(
      "Level | Clockwise | AntiClockwise"
    );
    for (int i = 0; i < routeTable[0].size(); i++) {
      Connection clockwise = routeTable[0].get(i);
      Connection antiClockwise = routeTable[1].get(i);
      string
        .append("\n")
        .append(
          String.format(
            "%d | %s,%s | %s,%s",
            i,
            clockwise == null
              ? "N/A"
              : Encoding.bitSetToString(clockwise.getNumericID(), nLen),
            clockwise == null ? "N/A" : clockwise.getAddress(),
            antiClockwise == null
              ? "N/A"
              : Encoding.bitSetToString(antiClockwise.getNumericID(), nLen),
            antiClockwise == null ? "N/A" : antiClockwise.getAddress()
          )
        );
    }

    return string.toString();
  }

  public int getLoad() {
    return load;
  }

  public void setLoad(int load) {
    this.load = load;
  }
}
