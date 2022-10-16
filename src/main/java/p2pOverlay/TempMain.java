package p2pOverlay;

import kotlin.text.Charsets;
import p2pOverlay.services.PeerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TempMain {

    private static final int GATEWAY_PORT = 8080;

    public static void main(String[] args){

        String line;
        final BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));
        int PORT_NUMBER = 0;
        System.out.print("Specify a port number (temporary implementation): ");
        try {
            PORT_NUMBER = Integer.parseInt(userInput.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PeerService ps = new PeerService(PORT_NUMBER);

        // we let the gateway have port 8080

        ps.startService();
        if(PORT_NUMBER != GATEWAY_PORT) {
            System.out.print("Available commands:\n" +
                    "1. Get peerID (register)\n" +
                    "2. Join skipgraph (insert)\n");
            try{
                while((line = userInput.readLine()) != null){
                    String[] tokens = line.split(" ");
                    System.out.println("Command recognised as: " + tokens[0]);
                    switch(tokens[0]){
                        case "register" -> ps.register();
                        case "insert" -> ps.requestInsertion();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.print("You are GATEWAY, just chill");
        }
    }

}
