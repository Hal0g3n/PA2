package p2pOverlay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import p2pOverlay.services.PeerService;
import p2pOverlay.util.Encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.BitSet;
import java.util.regex.Pattern;

public class LanternaMain {

    final static Window window = new BasicWindow();
    static WindowBasedTextGUI textGUI;
    static PeerService registeredService;
    static boolean registered = false;


    // Attempting to convert TempMain to Lanterna
    public static void main(String[] args) {
        Terminal terminal;
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

        // DefaultTerminalFactory decides which terminal to use
        try {
            terminal = defaultTerminalFactory.createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            textGUI = new MultiWindowTextGUI(screen);

            // Setup WindowBasedTextGUI for dialogs

            // Temp populate peers with 8080-8100
            // Capture system output
            // Create a stream to hold the output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(printStream);
            terminal.setCursorPosition(0, 0);
            terminal.resetColorAndSGR();
            int rows = 0;
            for (int i = 8080; i < 8100; i++) {
                PeerService ps = new PeerService(i);
                ps.startService();

                if (i != 8080) ps.register();

                for (String line : baos.toString().split("\n")) {
                    if (rows == terminal.getTerminalSize().getRows()) {
                        terminal.clearScreen();
                        rows = 0;
                    }

                    terminal.putString(line);
                    terminal.setCursorPosition(0, terminal.getCursorPosition().getRow() + 1);
                    terminal.flush();
                    rows++;
                }
                baos.reset();
                terminal.setCursorPosition(0, terminal.getCursorPosition().getRow() + 1);
            }
            System.out.flush();
            System.setOut(old);


            ActionListDialogBuilder actionListDialog = new ActionListDialogBuilder().setTitle("Choose command");
            Panel registeredPanel = constructMessageDialog(actionListDialog, "Already Registered");

            actionListDialog.addAction("Send Message", new Runnable() {
                        @Override
                        public void run() {
                            if (!registered) {
                                Panel notRegisteredPanel = constructMessageDialog(actionListDialog, "Not Registered!");
                                window.setComponent(notRegisteredPanel);
                                textGUI.addWindowAndWait(window);
                            } else {
                                Panel buttonPanel = new Panel();
                                buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));

                                Panel mainPanel = new Panel();
                                mainPanel.setLayoutManager(
                                        new GridLayout(1)
                                                .setLeftMarginSize(1)
                                                .setRightMarginSize(1));

                                mainPanel.addComponent(new Label("Input numeric ID"));
                                TextBox numericIdTB = new TextBox();
                                numericIdTB.setLayoutData(
                                                GridLayout.createLayoutData(
                                                        GridLayout.Alignment.FILL,
                                                        GridLayout.Alignment.CENTER,
                                                        true,
                                                        false))
                                        .setValidationPattern(Pattern.compile("\\d*"))
                                        .addTo(mainPanel);
                                mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

                                mainPanel.addComponent(new Label("Input message contents"));
                                TextBox contentTB = new TextBox(new TerminalSize(mainPanel.getSize().getColumns(), 5));
                                contentTB.setLayoutData(
                                                GridLayout.createLayoutData(
                                                        GridLayout.Alignment.FILL,
                                                        GridLayout.Alignment.CENTER,
                                                        true,
                                                        false))
                                        .addTo(mainPanel);
                                mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

                                buttonPanel.addComponent(new Button("OK", () -> {
                                    BitSet destId = BitSet.valueOf(new long[]{Long.parseLong(numericIdTB.getText())});
                                    String contents = contentTB.getText();

                                    registeredService.registerOnAcknowledge(new PopUpAcknowledge(textGUI));
                                    // uh oh it hangs the gui
                                    registeredService.sendMessage(destId, contents);
                                }));
                                buttonPanel.addComponent(new Button(LocalizedString.Cancel.toString(), this::onCancel));
                                buttonPanel.setLayoutData(
                                                GridLayout.createLayoutData(
                                                        GridLayout.Alignment.END,
                                                        GridLayout.Alignment.CENTER,
                                                        false,
                                                        false))
                                        .addTo(mainPanel);
                                window.setComponent(mainPanel);
                                textGUI.addWindowAndWait(window);
                            }
                        }

                        private void onCancel() {
                            window.close();
                            actionListDialog.build().showDialog(textGUI);
                        }
                    })

                    // Echo
                    .addAction("Register", new Runnable() {
                        @Override
                        public void run() {
                            // mfw i cant use the default text input builder cause it kills the actionDialog
                            // But the code here is essentially the code being used in the default textInputDialog
                            // Just that i need to change on cancel
                            if (registered) {
                                window.setComponent(registeredPanel);
                                textGUI.addWindowAndWait(window);
                            } else {
                                Panel mainPanel = new Panel();
                                mainPanel.setLayoutManager(
                                        new GridLayout(1)
                                                .setLeftMarginSize(1)
                                                .setRightMarginSize(1));

                                Label fileLabel = new Label("Input port number");
                                mainPanel.addComponent(fileLabel);
                                mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

                                // Config files not implemented yet, will just go with user input port
//                                Button fileButton = new Button("Open config file", new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        File input = new FileDialogBuilder()
//                                                .setTitle("Open File")
//                                                .setDescription("Choose a file")
//                                                .setActionLabel("Open")
//                                                .build()
//                                                .showDialog(textGUI);
//                                        fileLabel.setText(input.getName());
//                                    }
//                                });
//                                fileButton.setLayoutData(
//                                                GridLayout.createLayoutData(
//                                                        GridLayout.Alignment.FILL,
//                                                        GridLayout.Alignment.CENTER,
//                                                        true,
//                                                        false))
//                                        .addTo(mainPanel);

                                TextBox portNumberTB = new TextBox();
                                portNumberTB.setLayoutData(
                                                GridLayout.createLayoutData(
                                                        GridLayout.Alignment.FILL,
                                                        GridLayout.Alignment.CENTER,
                                                        true,
                                                        false))
                                        .setValidationPattern(Pattern.compile("\\d*"))
                                        .addTo(mainPanel);
                                mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

                                mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));


                                Panel buttonPanel = new Panel();
                                buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
                                buttonPanel.addComponent(new Button(LocalizedString.OK.toString(), () -> {
                                    // TODO: Register using config, now just use a dummy port number
                                    if (registered) {
                                        window.setComponent(registeredPanel);
                                        textGUI.addWindowAndWait(window);
                                    } else {
                                        int portNumber = Integer.parseInt(portNumberTB.getText());
                                        registeredService = new PeerService(portNumber);
                                        registeredService.startService();
                                        registeredService.register();
                                        registered = true;

                                        if (registeredService.assignedNum) {
                                            new MessageDialogBuilder()
                                                    .setTitle("Registration success")
                                                    .setText(String.format("PeerNum: %d, NumericID: %s", registeredService.getPeerNumber(), Encoding.BitSetToInt(registeredService.getNumericID())))
                                                    .build()
                                                    .showDialog(textGUI);
                                        }
                                    }
                                }).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false)));
                                buttonPanel.addComponent(new Button(LocalizedString.Cancel.toString(), this::onCancel));
                                buttonPanel.setLayoutData(
                                                GridLayout.createLayoutData(
                                                        GridLayout.Alignment.END,
                                                        GridLayout.Alignment.CENTER,
                                                        false,
                                                        false))
                                        .addTo(mainPanel);

                                window.setComponent(mainPanel);
                                textGUI.addWindowAndWait(window);
                            }
                        }

                        private void onCancel() {
                            window.close();
                            actionListDialog.build().showDialog(textGUI);
                        }

                    }).build().showDialog(textGUI);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Panel constructMessageDialog(ActionListDialogBuilder actionListDialog, String description) {
        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new GridLayout(1).setHorizontalSpacing(1));
        buttonPanel.addComponent(new Button("OK", () -> {
            window.close();
            actionListDialog.build().showDialog(textGUI);
        }));

        Panel panel = new Panel();
        panel.setLayoutManager(
                new GridLayout(1)
                        .setLeftMarginSize(1)
                        .setRightMarginSize(1));
        panel.addComponent(new Label(description));
        panel.addComponent(new EmptySpace(TerminalSize.ONE));
        buttonPanel.setLayoutData(
                        GridLayout.createLayoutData(
                                GridLayout.Alignment.END,
                                GridLayout.Alignment.CENTER,
                                false,
                                false))
                .addTo(panel);

        return panel;
    }

}

class PopUpAcknowledge implements PeerService.ACKCallback {
    private final WindowBasedTextGUI textGUI;

    public PopUpAcknowledge(WindowBasedTextGUI textGUI) {
        this.textGUI = textGUI;
    }

    @Override
    public void onAcknowledge(BitSet numericId) {
        new MessageDialogBuilder()
                .setTitle("Message Acknowledged")
                .setText(String.format("ID %d has acknowledged your message", Encoding.BitSetToInt(numericId)))
                .build()
                .showDialog(textGUI);
    }
}
