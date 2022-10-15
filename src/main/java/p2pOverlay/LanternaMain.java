package p2pOverlay;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import p2pOverlay.services.PeerService;

import java.io.IOException;
import java.util.regex.Pattern;

public class LanternaMain {

    private static final int GATEWAY_PORT = 8080;

    // Attempting to convert TempMain to Lanterna
    public static void main(String[] args) {
        Terminal terminal = null;
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

        // DefaultTerminalFactory decides which terminal to use
        try {
            terminal = defaultTerminalFactory.createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            // Setup WindowBasedTextGUI for dialogs
            final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
            final Window window = new BasicWindow();

            int PORT_NUMBER = Integer.parseInt(new TextInputDialogBuilder()
                    .setTitle("Input Port Number")
                    .setValidationPattern(Pattern.compile("\\d*"), "Port number must be an integer!")
                    .build()
                    .showDialog(textGUI)
            );


            ActionListDialogBuilder actionListDialog = new ActionListDialogBuilder().setTitle("Choose command");

            PeerService ps = new PeerService(PORT_NUMBER);
            ps.startService();
            if (PORT_NUMBER != GATEWAY_PORT) {
                // Register
                actionListDialog.addAction("Register", new Runnable() {
                            @Override
                            public void run() {
                                // TODO: display port id after registering
                                ps.register();
                                // Idk how to make this persistent without recalling this every time
                                actionListDialog.build().showDialog(textGUI);
                            }
                        })

                        // Echo
                        .addAction("Echo", new Runnable() {
                            @Override
                            public void run() {
                                Panel echoPanel = new Panel();
                                echoPanel.setLayoutManager(new GridLayout(2));

                                // Input port ID
                                echoPanel.addComponent(new Label("Port ID"));
                                final TextBox portIDTB = new TextBox().setValidationPattern(Pattern.compile("\\d*")).addTo(echoPanel);

                                // Input message
                                echoPanel.addComponent(new Label("Message"));
                                final TextBox msgTB = new TextBox().addTo(echoPanel);

                                Label resultLabel = new Label("");
                                echoPanel.addComponent(resultLabel);
                                echoPanel.addComponent(new EmptySpace());

                                // Submit
                                echoPanel.addComponent(new Button("Submit", new Runnable() {
                                    @Override
                                    public void run() {
                                        String portId = portIDTB.getText();
                                        String msg = msgTB.getText();

                                        // TODO: get result string from echo
                                        resultLabel.setText(portId + " " + msg);
                                    }
                                }));
                                // Close
                                echoPanel.addComponent(new Button("Close", new Runnable() {
                                    @Override
                                    public void run() {
                                        window.close();
                                        actionListDialog.build().showDialog(textGUI);
                                    }
                                }));
                                window.setComponent(echoPanel);
                                textGUI.addWindowAndWait(window);
                            }
                        }).build().showDialog(textGUI);
            }

            // actually idk if this is needed but ill keep it here :slight_smile:
            terminal.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
