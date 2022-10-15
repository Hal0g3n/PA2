package p2pOverlay.services;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import p2pOverlay.Peer;
import p2pOverlay.model.RouteMessage;
import p2pOverlay.util.Encoding;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PeerService {

    /*
     * This should store the Peer object?
     * The Peer itself will contain information about the connections it should have
     * This will help Peer interface with the ConnectionService through a ConnectionHandler
     *
     * */

    private final static int NUMERIC_ID_LEN = 5; // no more than 32 peers in the demo

    private HashMap<Integer, Boolean> usedId;

    private Peer head;
    private ConnectionService connectionService;
    private int port;


    private int peerNumberCounter;



    private ArrayList<Integer> tempCounter;


    public PeerService(int port) {
        this.head = new Peer();
        this.port = port;

        if (this.port == 8080) {
            // this is the gateway node, and will always be the first one in the network
            // this is for simulation purposes, after all

        }
        this.peerNumberCounter = 0;
        this.usedId = new HashMap<>();
        this.tempCounter = new ArrayList<>();
    }


    // this function is for peerID, not numericID
    public static int commonPrefixLen(BitSet a, BitSet b) {
        int maxLength = Math.max(a.length(), b.length());

        for (int i = maxLength - 1; i >= 0; i--) {
            if (a.get(i) != b.get(i)) return maxLength - i - 1;
        }
        return a.length();
    }

    public void startService() {
        try {
            this.connectionService = new ConnectionService(this, port);
            System.out.println("in main, called");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {

        // in this case, it is more convenient to just send a registration to 8080
        // however, it SHOULD be able to be sent to any other port
        // because we are only sending registration commands to localhost,
        //                  we will just maintain a counter for peerNumber

        // this is extremely cursed, but will make do for now
        connectionService.sendMessage("register " + port, "127.0.01", 8080);
    }

    public void insertNode(int numericId){


    }

    private void routeByNumericID(RouteMessage msg){
        if(head.getNumericID() == msg.getDestId()){

            // if you have the correct numericId, then you should get the message
            // the message should be arbitrary
            return;
        }

        if(head.getNumericID() == msg.getSourceNode().numericID()){
            // we have finished this ring, so we go to the next ring

        }

    }


    public void sendMsg(int targetId, String message) {
        // ok so basically right now we only relay messages to the gateway
        // but in the future, each peer would also store some connections which it knows the peerId of
        // and it that case, it fills in the port of that peerId as opposed to gateway

        System.out.println("Attempting ping");
        sendMsg(head.getLongId(), targetId, message, "127.0.0.1", 8080);
    }

    private void sendMsg(long sourceId, int targetId, String message, String ip, int port) {
        connectionService.sendMessage(String.format("ping %d %d %d %s", sourceId, head.getLongId(), targetId, message), ip, port);
    }

    public void handleMessage(ChannelHandlerContext ctx, String msg) {


        System.out.printf("Handling message %s from %s\n", msg, ctx.channel().remoteAddress());

        String[] tokens = msg.split(" ");
        String command = tokens[0];
        System.out.println(Arrays.toString(tokens));
        switch (command) {

            case "register" -> {
                // incoming registration, thus i am the gateway
                head.setPeerID(0);

                // we need to give the peer its peerNumber and numericID
                peerNumberCounter++;
                int numID = ThreadLocalRandom.current().nextInt(0, 2 << NUMERIC_ID_LEN + 1);
                while(usedId.containsKey(numID)) numID = ThreadLocalRandom.current().nextInt(0, 2 << NUMERIC_ID_LEN + 1);
                usedId.put(numID, true);

                

                tempCounter.add(Integer.parseInt(tokens[1]));



                // TODO: Convert these to message object

                String response = "assignedNum " + peerNumberCounter;
                ByteBuf out = ctx.alloc().buffer(response.length() * 2);
                out.writeBytes(Encoding.str_to_bb(response));
                ctx.writeAndFlush(out);
                ctx.close(); // seems like CTX is blocking...
            }

            case "assignedNum" -> {

                // I received a numericID from the gateway
                head.setPeerNumber(Integer.parseInt(tokens[1])); // set numericID based on gateway response
                // now that I have a numericID, I need to insert myself into the skipgraph

                // in this case, once again since deletions will begin with the gateway
                //          it will simplify the process to an extent, just a bit.
            }


            case "approved" -> {

                System.out.println("Approved by gateway! Given ID " + tokens[1]);
            }
            case "ping" -> { // ping sourceID fromID targetID msg

                int sourceID = Integer.parseInt(tokens[1]);
                // nextID = head.id anyways
                int targetID = Integer.parseInt(tokens[3]);
                String pingMsg = tokens[4];

                if (head.getLongId() == targetID) {
                    // we now initiate a pong back
                    // pong sourceID fromID targetID msg
                    String demoResponse = String.format(
                            "pong %d %d %d %s",
                            targetID, head.getLongId(), sourceID, pingMsg);
                    ByteBuf out = ctx.alloc().buffer(demoResponse.length() * 2);
                    out.writeBytes(Encoding.str_to_bb(demoResponse));
                    ctx.writeAndFlush(out);
                    ctx.close();
                } else {
                    // i am an intermediate
                    // program logic is supposed to determine the next peer to send to
                    // for now, we only check if we have the peer to send to, which should be true for the gateway
                    //Connection connection = head.getConnection();
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

                if (head.getLongId() == targetID) { // i am the one who pong'd
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

}