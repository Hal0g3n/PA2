package p2pOverlay.services;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import p2pOverlay.Peer;
import p2pOverlay.model.Connection;
import p2pOverlay.model.Message;
import p2pOverlay.model.RouteMessage;
import p2pOverlay.util.Encoding;

import java.net.InetSocketAddress;
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
    private Connection selfConnection;

    private ArrayList<Integer> tempCounter;


    public PeerService(int port) {
        this.head = new Peer();
        this.port = port;
        this.selfConnection = new Connection(new BitSet(NUMERIC_ID_LEN),
                new InetSocketAddress("127.0.0.1", port)
                );
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

        Message registrationMsg = new Message(selfConnection, "", "register");
        connectionService.sendMessage(registrationMsg, "127.0.01", 8080);
    }

    public void insertNode(int numericId){


    }

    private void routeByNumericID(RouteMessage msg){

        if(head.getNumericID() == msg.getDestId()){

            // if you have the correct numericId, then you should get the message
            // the message should be arbitrary
            return;
        }

        if(head.getNumericID() == msg.getSourceNode().getNumericID()){
            // we just went in a loop!
            // we have finished this ring, so we go to the next ring
            msg.setFinalDestination(true);
            connectionService.sendMessage(msg, msg.getBestNode());
            return;
        }

        int height = commonPrefixLen(msg.getSourceNode().getNumericID(), head.getNumericID());
        if(height > msg.getRingLevel()){
            msg.setRingLevel(height);
            msg.setSourceNode(selfConnection);
            msg.setBestNode(selfConnection);
        } else if ( Math.abs(
                Encoding.BitSetToInt(selfConnection.getNumericID())
                - Encoding.BitSetToInt(msg.getSourceNode().getNumericID())
        ) < Math.abs(
                Encoding.BitSetToInt(msg.getBestNode().getNumericID())
                        - Encoding.BitSetToInt(msg.getSourceNode().getNumericID()) )){
            msg.setBestNode(selfConnection);
        }

        Connection nextPeer = head.getClockwiseNeighbour(msg.getRingLevel());
        connectionService.sendMessage(msg, nextPeer);
        return;
    }


    public void sendMsg(int targetId, String message) {
        // ok so basically right now we only relay messages to the gateway
        // but in the future, each peer would also store some connections which it knows the peerId of
        // and it that case, it fills in the port of that peerId as opposed to gateway

        System.out.println("Attempting ping");
        sendMsg(head.getLongId(), targetId, message, "127.0.0.1", 8080);
    }

    private void sendMsg(long sourceId, int targetId, String message, String ip, int port) {
        //connectionService.sendMessage(String.format("ping %d %d %d %s", sourceId, head.getLongId(), targetId, message), ip, port);
    }

    public void handleMessage(ChannelHandlerContext ctx, Message msg) {


        String msgCommand = msg.getMessageCommand();

        System.out.printf("Handling message %s from %s\n", msgCommand, ctx.channel().remoteAddress());

        //String[] tokens = msg.split(" ");
        //System.out.println(Arrays.toString(tokens));


        switch (msgCommand) {
            case "register" -> {
                // incoming registration, thus i am the gateway
                head.setPeerID(0);
                // we need to give the peer its peerNumber and numericID
                peerNumberCounter++;
                int numID = ThreadLocalRandom.current().nextInt(0, 1 << NUMERIC_ID_LEN + 1);
                while(usedId.containsKey(numID)) numID = ThreadLocalRandom.current().nextInt(0, 1 << NUMERIC_ID_LEN + 1);
                usedId.put(numID, true);
                System.out.printf("Received a registration, assigning IDs %d %d\n", peerNumberCounter, numID);
                Message responseMsg = new Message(selfConnection,
                        String.format("%d %d", peerNumberCounter, numID),
                        "assignedNum");
                ctx.writeAndFlush(responseMsg);
                ctx.close(); // seems like CTX is blocking...
            }

            case "assignedNum" -> {

                // I received a numericID from the gateway

                // messageContent = peerNumber numID
                String[] tokens = msg.getMessageContent().split(" ");
                this.selfConnection.setNumericID(Encoding.intToBitSet(Integer.parseInt(tokens[1]), NUMERIC_ID_LEN));
                this.head.setPeerNumber(Integer.parseInt(tokens[0]));
                // now that I have a numericID, I need to insert myself into the skipgraph
                System.out.printf("Peer registration complete with assigned IDs %s %s\n", tokens[0], tokens[1]);
            }

            case "routing" -> routeByNumericID((RouteMessage) msg);

        }

    }

}