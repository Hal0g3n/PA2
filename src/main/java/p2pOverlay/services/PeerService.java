package p2pOverlay.services;

import io.netty.channel.ChannelHandlerContext;
import p2pOverlay.Peer;
import p2pOverlay.model.Connection;
import p2pOverlay.model.message.JoinMessage;
import p2pOverlay.model.message.Message;
import p2pOverlay.model.message.RouteMessage;
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

    public final static int NUMERIC_ID_LEN = 5; // no more than 32 peers in the demo

    private HashMap<Integer, Boolean> usedId;

    private Peer head;
    private ConnectionService connectionService;
    private int port;
    private int treeHeight;

    private int peerNumberCounter;
    private Connection selfConnection;

    private ArrayList<Integer> tempCounter;


    public PeerService(int port) {
        this.head = new Peer();
        this.port = port;
        this.treeHeight = 0;
        this.selfConnection = new Connection(
                0,
                new BitSet(NUMERIC_ID_LEN),
                new InetSocketAddress("127.0.0.1", port)
                );
        for(int i = 0; i < NUMERIC_ID_LEN; i++){
            head.setAnticlockwiseNeighbour(i, null);
            head.setClockwiseNeighbour(i, null);
        }
        this.usedId = new HashMap<>();

        if (this.port == 8080) {
            // this is the gateway node, and will always be the first one in the network
            // this is for simulation purposes, after all

            // also, as the first node, it will always hold the leftmost node
            head.setPeerID(0, treeHeight);
            head.setNumericID(Encoding.intToBitSet(0, NUMERIC_ID_LEN));
            selfConnection.setPeerNum(0);
            selfConnection.setNumericID(Encoding.intToBitSet(0, NUMERIC_ID_LEN));
            usedId.put(0, true);

            head.setClockwiseNeighbour(0, selfConnection);
            head.setAnticlockwiseNeighbour(0, selfConnection);
            // currently only root ring exists with gateway there

        }
        this.peerNumberCounter = 0;
        this.usedId = new HashMap<>();
        this.tempCounter = new ArrayList<>();
    }


    public static int commonPrefixLen(BitSet a, BitSet b) {

        for(int i = 0; i < NUMERIC_ID_LEN; i++){
            if(a.get(i) != b.get(i)) return i;
        }
        return NUMERIC_ID_LEN;
    }

    public void startService() {
        try {
            this.connectionService = new ConnectionService(this, port);
            System.out.println("Starting 127.0.0.1:" + port);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopService(){
        // before we can stop the service, we first have to inform all our friends
        // on the routeTable that I'm no longer here and driving a wedge
        // between their loving relationship :(

        // for each ringLvl, tell the clockwise to set your current anticlockwise as neighbour
        // similarly do the same to the anticlockwise

        for(int i = 0; i < NUMERIC_ID_LEN; i++){
            Connection clockwise = head.getClockwiseNeighbour(i);
            Connection anticlockwise = head.getAnticlockwiseNeighbour(i);
            if(clockwise != null && anticlockwise != null){

                Message becomeNeighbour;

                // informing the clockwise
                becomeNeighbour = new Message(
                    clockwise,
                        String.format("clockwise:%d", i),
                        "neighbour"
                );
                connectionService.sendMessage(becomeNeighbour, anticlockwise);

                // informing the anticlockwise
                becomeNeighbour = new Message(
                        anticlockwise,
                        String.format("anticlockwise:%d", i),
                        "neighbour"
                );
                connectionService.sendMessage(becomeNeighbour, clockwise);
            }
        }
        connectionService.stopServer();
    }

    public void printRouteTable(){
        System.out.println(head.routeTableString(NUMERIC_ID_LEN));
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

    public void requestInsertion(){
        RouteMessage insertionMsg = new RouteMessage(
                selfConnection,
                "insertion",
                "routing",
                selfConnection.getNumericID(),
                null,
                null,
                -1,
                false
        );
        connectionService.sendMessage(insertionMsg, "127.0.0.1", 8080);
    }

    private void routeByNumericID(RouteMessage msg){

        System.out.println("Well, it looks like I need to route a message!!");

        if(head.getNumericID() == msg.getDestId() || msg.isFinalDestination()){
            System.out.println("Looks like I'm the intended peer for this message. On to processing!");
            // if you have the correct numericId, then you should get the message
            // that or you're the closest as can be
            handleMessage(msg);
            return;
        }

        if(msg.getStartNode() != null){
            System.out.printf("Source node is no longer null, %s == %s?\n",
                Encoding.bitSetToString(head.getNumericID(), NUMERIC_ID_LEN),
                    Encoding.bitSetToString(msg.getStartNode().getNumericID(), NUMERIC_ID_LEN)
            );
        if(head.getNumericID().equals(msg.getStartNode().getNumericID())){
            // we just went in a loop!
            // we have finished this ring, so we should be good

            System.out.println("We went in a loop, dammit! Sending back to the best node");
            msg.setFinalDestination(true);
            connectionService.sendMessage(msg, msg.getBestNode());
            return;
        }}

        int height = commonPrefixLen(msg.getDestId(), head.getNumericID());

        System.out.printf("The common prefix length between %s and %s is %d\n",
                Encoding.bitSetToString(msg.getDestId(), NUMERIC_ID_LEN),
                Encoding.bitSetToString(head.getNumericID(), NUMERIC_ID_LEN),
                height);

        if(height > msg.getRingLevel()){

            System.out.println("Since height is greater, we're going up");

            msg.setRingLevel(height);
            msg.setStartNode(selfConnection);
            msg.setBestNode(selfConnection);
        } else if(msg.getBestNode() != null)
        {if ( Math.abs(
                Encoding.BitSetToInt(selfConnection.getNumericID())
                - Encoding.BitSetToInt(msg.getDestId())
        ) < Math.abs(
                Encoding.BitSetToInt(msg.getBestNode().getNumericID())
                        - Encoding.BitSetToInt(msg.getDestId()) )){
            msg.setBestNode(selfConnection);
        }}

        Connection nextPeer = head.getClockwiseNeighbour(msg.getRingLevel());

        if(nextPeer == null) // well, we're working with an empty ring
        {
            head.setClockwiseNeighbour(msg.getRingLevel(), selfConnection);
            head.setAnticlockwiseNeighbour(msg.getRingLevel(), selfConnection);
            nextPeer = head.getClockwiseNeighbour(msg.getRingLevel());
        }
        System.out.printf("Forwarding along the ring to Peer %d @ %s, see ya\n", nextPeer.getPeerNum(), nextPeer.getAddress());
        connectionService.sendMessage(msg, nextPeer);
        return;
    }

    private void handleMessage(Message msg){
        String msgContent = msg.getMessageContent();
        switch (msgContent){
            case "insertion" -> {

                // the peer which receives "insertion" is the """Gateway""" for it
                // it will start sending the join message to its friends
                Connection joiningNode = msg.getSourceNode();
                System.out.printf("The joining node has numeric id %d\n", Encoding.BitSetToInt(joiningNode.getNumericID()));
                JoinMessage joinMessage = new JoinMessage(
                        selfConnection,
                        "join",
                        "join",
                        false,
                        joiningNode,
                        // in this case, the numID of source is where we want to insert it
                        commonPrefixLen(selfConnection.getNumericID(), msg.getSourceNode().getNumericID())
                );
                insertNode(joinMessage);
            }
        }
    }

    private void insertNode(JoinMessage joinMessage){

        System.out.printf("Received join message at ring lvl %d\n", joinMessage.getRingLvl());

        if(joinMessage.isPerformInsertions()){
            System.out.println("Looks like im here to ask for some insertions");
            // the joinMessage should have all the neighbours you should have now <3
            insertIntoRings(joinMessage);
            return;
        }

        while(joinMessage.getRingLvl() >= 0){
            // keep going lower
            Connection clockwise = head.getClockwiseNeighbour(joinMessage.getRingLvl());
            if(clockwise!=null){
                System.out.printf("We are trying to do an insert of %d [%d] %d\n", head.getPeerNumber(),
                        joinMessage.getJoiningNode().getPeerNum(),
                        clockwise.getPeerNum());
            }
            // if it is null, this means that it's free
            if(clockwise == null || // there should be a short circuit
                    // if not, then make sure that it lies between
                liesBetween(
                        head.getPeerNumber(),
                        joinMessage.getJoiningNode().getPeerNum(),
                        clockwise.getPeerNum()
                )
            ){



                int ringLvl = joinMessage.getRingLvl();
                joinMessage.setRingAnticlockwise(ringLvl, selfConnection);

                if(clockwise != null) joinMessage.setRingClockwise(ringLvl, clockwise);
                else joinMessage.setRingClockwise(ringLvl, selfConnection);
                System.out.printf("Set c to %d and ac to %d\n",
                        joinMessage.getRingClockwise(ringLvl).getPeerNum(),
                        joinMessage.getRingAnticlockwise(ringLvl).getPeerNum());
                joinMessage.setRingLvl(ringLvl-1);
            } else {
                connectionService.sendMessage(joinMessage, clockwise); // hand it over to the next
                return;
            }
        }

        joinMessage.setPerformInsertions(true);
        connectionService.sendMessage(joinMessage, joinMessage.getJoiningNode());
    }

    private void insertIntoRings(JoinMessage joinMessage){
        for(int i = 0; i <= joinMessage.getOriginalLvl(); i++){ // numID len is max ring depth
            System.out.printf("Making request at lvl %d where c is %d and ac is %d\n",
                    i,
                    joinMessage.getRingClockwise(i).getPeerNum(),
                    joinMessage.getRingAnticlockwise(i).getPeerNum());

            if(joinMessage.getRingAnticlockwise(i) != null){
                System.out.printf("Requesting insertion from %d\n", Encoding.BitSetToInt(joinMessage.getRingAnticlockwise(i).getNumericID()));
                head.setAnticlockwiseNeighbour(i, joinMessage.getRingAnticlockwise(i));
                Message becomeNeighbour = new Message(
                        selfConnection,
                        String.format("clockwise:%d",i),
                        "neighbour"
                );
                connectionService.sendMessage(becomeNeighbour, joinMessage.getRingAnticlockwise(i));
            }

            if(joinMessage.getRingClockwise(i) != null){
                System.out.printf("Requesting insertion from %d\n", Encoding.BitSetToInt(joinMessage.getRingClockwise(i).getNumericID()));
                head.setClockwiseNeighbour(i, joinMessage.getRingClockwise(i));
                Message becomeNeighbour = new Message(
                        selfConnection,
                        String.format("anticlockwise:%d",i),
                        "neighbour"
                );
                connectionService.sendMessage(becomeNeighbour, joinMessage.getRingClockwise(i));
            }
        }
    }

    private boolean liesBetween(int a, int b, int c){

        if(c == 0){
            if(a < b) return true;
        }
        if(a < b && b < c || a > b && b > c) return true;

        return a == c;
    }

    public void handleImmediateMessage(ChannelHandlerContext ctx, Message msg) {

        String msgCommand = msg.getMessageCommand();

        System.out.printf("Handling message %s from %s\n", msgCommand, ctx.channel().remoteAddress());

        switch (msgCommand) {
            case "register" -> {
                // we need to give the peer its peerNumber and numericID
                peerNumberCounter++;
                int numID = ThreadLocalRandom.current().nextInt(0, (1 << NUMERIC_ID_LEN));
                while(usedId.containsKey(numID)) numID = ThreadLocalRandom.current().nextInt(0, (1 << NUMERIC_ID_LEN));
                usedId.put(numID, true);
                System.out.printf("Received a registration, assigning IDs %d %d\n", peerNumberCounter, numID);
                Message responseMsg = new Message(selfConnection,
                        String.format("%d %d", peerNumberCounter, numID),
                        "assignedNum");
                ctx.writeAndFlush(responseMsg);
            }

            case "assignedNum" -> {
                // I received a numericID from the gateway
                // messageContent = peerNumber numID
                String[] tokens = msg.getMessageContent().split(" ");
                this.selfConnection.setNumericID(Encoding.intToBitSet(Integer.parseInt(tokens[1]), NUMERIC_ID_LEN));
                this.head.setNumericID(Encoding.intToBitSet(Integer.parseInt(tokens[1]), NUMERIC_ID_LEN));
                this.head.setPeerNumber(Integer.parseInt(tokens[0]));
                this.selfConnection.setPeerNum(Integer.parseInt(tokens[0]));
                // now that I have a numericID, I need to insert myself into the skipgraph
                System.out.printf("Peer registration complete with assigned IDs %s %s\n", tokens[0], tokens[1]);
            }

            case "routing" -> {
                routeByNumericID((RouteMessage) msg);
            }

            case "join" -> {
                insertNode((JoinMessage) msg);
            }

            case "neighbour" -> {
                String[] tokens = msg.getMessageContent().split(":");
                String direction = tokens[0];
                int ringLvl = Integer.parseInt(tokens[1]);
                if(direction.equals("clockwise")) {
                    head.setClockwiseNeighbour(ringLvl, msg.getSourceNode());
                } else {
                    head.setAnticlockwiseNeighbour(ringLvl, msg.getSourceNode());
                }
            }

            case "getPeerID" -> {
                System.out.println("Received request for my peerID");
                Message responseMsg = new Message(selfConnection,
                        String.format("%d %d", peerNumberCounter, Encoding.BitSetToInt(head.getPeerID())),
                        "retPeerID"
                        );
                ctx.writeAndFlush(responseMsg);
            }
        }
        ctx.close();
    }

}