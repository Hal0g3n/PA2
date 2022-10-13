package p2pOverlay;

import p2pOverlay.services.PeerService;

public class TempMain {

    public static void main(String[] args){
        PeerService ps = new PeerService();
        ps.startService();
    }

}
