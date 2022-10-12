package p2pOverlay.services;

import p2pOverlay.Peer;
import p2pOverlay.util.Connection;

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
}
