package p2pOverlay.util;

import p2pOverlay.Peer;

import java.net.InetSocketAddress;
import java.util.BitSet;

public record Connection(BitSet peerID, InetSocketAddress address) {
}
