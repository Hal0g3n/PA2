package p2pOverlay.services;

import p2pOverlay.Peer;
import p2pOverlay.util.Connection;

import java.util.Objects;

public class PeerService {

    /*
    * This should store the Peer object?
    * The Peer itself will contain information about the connections it should have
    * This will help Peer interface with the ConnectionService through a ConnectionHandler
    *
    * */

    Peer head;

    // TODO: Finish constructor, doing routing first
    public PeerService(String id) { this.head = new Peer(id); }

    // TODO: routing, join, leave
    // Assumes both strings are of equal length
    private int commonPrefixLen(String a, String b) {
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return a.length();
    }

    public Peer findPeer(String peerId) {
        return findPeer(head, peerId);
    }

    public Peer findPeer(Peer source, String destinationId) {
        if (Objects.equals(source.id, destinationId)) return source;

        int ringLevel = commonPrefixLen(source.id, destinationId);
        return findPeer(source.routeTable[0].get(ringLevel).peer(), destinationId);
    }


}
