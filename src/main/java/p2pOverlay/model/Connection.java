package p2pOverlay.model;

import p2pOverlay.Peer;

import java.net.InetSocketAddress;
import java.util.BitSet;

public record Connection(BitSet numericID, InetSocketAddress address, int load) {
}
