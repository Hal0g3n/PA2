package p2pOverlay.services;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import p2pOverlay.Peer;
import p2pOverlay.util.Connection;
import p2pOverlay.util.Encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class PeerService {

    /*
    * This should store the Peer object?
    * The Peer itself will contain information about the connections it should have
    * This will help Peer interface with the ConnectionService through a ConnectionHandler
    *
    * */

    private Peer head;
    private ConnectionService connectionService;
    private int port;

    private ArrayList<Integer> tempCounter;

    // TODO: Finish constructor, doing routing first
    public PeerService(int port) {
        this.head = new Peer();
        this.port = port;
        this.tempCounter = new ArrayList<>();
    }

    // TODO: routing, join, leave
    // Assumes both strings are of equal length
    private int commonPrefixLen(String a, String b) {
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return a.length();
    }

//    public Peer findPeer(String peerId) {
//        return findPeer(head, peerId);
//    }

//    public Peer findPeer(Peer source, String destinationId) {
//        if (Objects.equals(source.id, destinationId)) return source;
//
//        //int ringLevel = commonPrefixLen(source.id, destinationId); fix later
//        int ringLevel = 0;
//        return findPeer(source.routeTable[0].get(ringLevel).peer, destinationId);
//    }

    // Connection object is not supposed to contain a peer object, only the peerID and address

    public void startService(){
        try {
            this.connectionService = new ConnectionService(this, port);
            System.out.println("in main, called");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(){
        // TODO: make this less jank
        connectionService.sendMessage("register " + port, "127.0.01", 8080);
    }

    public void handleMessage(ChannelHandlerContext ctx, String msg){
        System.out.printf("Handling message %s from %s\n", msg, ctx.channel().remoteAddress());

        String[] tokens = msg.split(" ");
        String command = tokens[0];
        System.out.println(Arrays.toString(tokens));
        switch (command) {
            case "register" -> {
                // incoming registration, thus i am the gateway
                head.setId(0);
                tempCounter.add(Integer.parseInt(tokens[1]));
                String demoResponse = "approved " + tempCounter.size();
                ByteBuf out = ctx.alloc().buffer(demoResponse.length() * 2);
                out.writeBytes(Encoding.str_to_bb(demoResponse));
                ctx.writeAndFlush(out);
            }
            case "approved" -> {
                head.setId(Integer.parseInt(tokens[1])); // set peerID based on gateway response
                System.out.println("Approved by gateway! Given ID " + tokens[1]);
            }
        }

    }
}
