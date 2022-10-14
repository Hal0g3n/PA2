package p2pOverlay.services;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import p2pOverlay.Peer;
import p2pOverlay.util.Connection;
import p2pOverlay.util.Encoding;

import java.util.*;

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
    public static int commonPrefixLen(BitSet a, BitSet b) {
        int maxLength = Math.max(a.length(), b.length());

        for (int i = maxLength - 1; i >= 0; i--) {
            if (a.get(i) != b.get(i)) return maxLength - i - 1;
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

    public void sendMsg(int targetId, String message){
        // ok so basically right now we only relay messages to the gateway
        // but in the future, each peer would also store some connections which it knows the peerId of
        // and it that case, it fills in the port of that peerId as opposed to gateway

        // TODO: pull ip and port from peer pointer table when the time comes
        System.out.println("Attempting ping");
        sendMsg(head.getLongId(), targetId, message, "127.0.0.1", 8080);
    }

    private void sendMsg(long sourceId, int targetId, String message, String ip, int port){
        connectionService.sendMessage(String.format("ping %d %d %d %s", sourceId, head.getLongId(), targetId, message), ip, port);
    }

    // temporary measure while i think of how to better do this
    private Queue<ChannelHandlerContext> ctx_queue = new LinkedList<>();

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
                ctx.close(); // seems like CTX is blocking...
            }
            case "approved" -> {
                head.setId(Integer.parseInt(tokens[1])); // set peerID based on gateway response
                System.out.println("Approved by gateway! Given ID " + tokens[1]);
            }
            case "ping" -> { // ping sourceID fromID targetID msg

                int sourceID = Integer.parseInt(tokens[1]);
                // nextID = head.id anyways
                int targetID = Integer.parseInt(tokens[3]);
                String pingMsg = tokens[4];

                if(head.getLongId() == targetID){
                    // we now initiate a pong back
                    // pong sourceID fromID targetID msg
                    String demoResponse = String.format(
                            "pong %d %d %d %s",
                            targetID, head.getLongId(), sourceID, pingMsg);
                    ByteBuf out = ctx.alloc().buffer(demoResponse.length() * 2);
                    out.writeBytes(Encoding.str_to_bb(demoResponse));
                    ctx.writeAndFlush(out);
                    ctx.close();
                }
                else {
                    // i am an intermediate
                    // program logic is supposed to determine the next peer to send to
                    // for now, we only check if we have the peer to send to, which should be true for the gateway
                    sendMsg(sourceID, targetID, pingMsg, "127.0.0.1", tempCounter.get(1)); // passing the message to peer 2
                    ctx.close();
                }
            }
            case "pong" -> { // pong sourceID fromID targetID msg
                // the logic should be similar to ping
                ctx.close(); // you should usually send back an ACK

                int sourceID = Integer.parseInt(tokens[1]);
                int targetID = Integer.parseInt(tokens[3]);
                String pongMsg = tokens[4];

                if(head.getLongId() == targetID){ // i am the one who pong'd
                    System.out.printf("Received a pong from %d! msg : %s\n", sourceID, pongMsg);
                } else { // keep passing on the pong
                    // again, there should be program logic for this
                    // for now, we just pull the port from tempCounter

                    connectionService.sendMessage(String.format(
                            "pong %d %d %d %s",
                            sourceID, head.getLongId(), targetID, pongMsg),
                            "127.0.0.1", tempCounter.get(0));
                            // we know we have to pong back to peer 1

                }
            }
        }

    }

    // Testing bitset
//    public static void main(String[] args) {
//        BitSet bitSet = BitSet.valueOf(new long[] {11});
//        BitSet bitSet1 = BitSet.valueOf(new long[] {10});
//
//        System.out.println(commonPrefixLen(bitSet, bitSet1));
//    }
}
