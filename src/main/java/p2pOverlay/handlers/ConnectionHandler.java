package p2pOverlay.handlers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionHandler {

    private Queue<String> incomingMessages;

    public ConnectionHandler(){
        incomingMessages = new LinkedList<>();
    }

    private class ServerListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String receivedString = (String) evt.getNewValue();
            incomingMessages.add(receivedString);
            System.out.println(receivedString);
        }
    }

}
