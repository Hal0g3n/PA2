package p2pOverlay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import p2pOverlay.services.PeerService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.BitSet;

public class LanternaMain {

    private static final int GATEWAY_PORT = 8080;
    final static Window window = new BasicWindow();
    static WindowBasedTextGUI textGUI;

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

            // Register
            actionListDialog.addAction("Choose peer", new Runnable() {
                        @Override
                        public void run() {
                            new MessageDialogBuilder()
                                    .setTitle("Oops")
                                    .setText("Communication with R tree isnt done yet sowwy :(")
                                    .build()
                                    .showDialog(textGUI);
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

                            Panel mainPanel = new Panel();
                            mainPanel.setLayoutManager(
                                    new GridLayout(1)
                                            .setLeftMarginSize(1)
                                            .setRightMarginSize(1));

                            Label fileLabel = new Label("Choose a config file");
                            mainPanel.addComponent(fileLabel);
                            mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

                            Button fileButton = new Button("Open config file", new Runnable() {
                                @Override
                                public void run() {
                                    File input = new FileDialogBuilder()
                                            .setTitle("Open File")
                                            .setDescription("Choose a file")
                                            .setActionLabel("Open")
                                            .build()
                                            .showDialog(textGUI);
                                    System.out.println(input);
                                    fileLabel.setText(input.getName());
                                }
                            });
                            fileButton.setLayoutData(
                                            GridLayout.createLayoutData(
                                                    GridLayout.Alignment.FILL,
                                                    GridLayout.Alignment.CENTER,
                                                    true,
                                                    false))
                                    .addTo(mainPanel);
                            mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));


                            Panel buttonPanel = new Panel();
                            buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
                            buttonPanel.addComponent(new Button(LocalizedString.OK.toString(), new Runnable() {
                                @Override
                                public void run() {
                                    // TODO: Register using config, now just use a dummy port number
                                    int portNumber = 8000;
                                    PeerService ps = new PeerService(portNumber);
                                    ps.startService();
                                    ps.register();

                                    if (ps.assignedNum) {
                                        new MessageDialogBuilder()
                                                .setTitle("Registration success")
                                                .setText(String.format("PeerNum: %d, NumericID: %s", ps.getPeerNumber(), ps.getNumericID()))
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

                        private void onCancel() {
                            window.close();
                            actionListDialog.build().showDialog(textGUI);
                        }

                    }).build().showDialog(textGUI);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
