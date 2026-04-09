package Network;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Handles the logic for connecting to a server and transitioning
 * to the multiplayer game view.
 */
public class MP_ConnectionController {

    private MP_ConnectionView view;
    private MP_Server server;
    private MP_Client client;
    private MP_QView gameView;

    public MP_ConnectionController(MP_ConnectionView view) {
        this.view = view;
        wireActions();
    }

    private void wireActions() {
        // HOST BUTTON LOGIC
        view.getHostBtn().setOnAction(e -> {
            int port = view.getPort();
            server = new MP_Server(port);
            server.start();
            view.log("Server started on port " + port + ". Waiting for players...");
            
            // The Host auto-joins as a client so they can play too
            autoJoinHost("127.0.0.1", port);
        });

        // JOIN BUTTON LOGIC
        view.getJoinBtn().setOnAction(e -> {
            String ip = view.getIp();
            int port = view.getPort();
            client = new MP_Client();
            
            try {
                client.connect(ip, port, this::handleIncomingMessage);
                view.log("Connected to " + ip + ":" + port);
                switchToGameView(); 
            } catch (IOException ex) {
                view.log("Connection failed: " + ex.getMessage());
            }
        });
    }

    private void autoJoinHost(String ip, int port) {
        client = new MP_Client();
        try {
            client.connect(ip, port, this::handleIncomingMessage);
            switchToGameView();
        } catch (IOException e) {
            view.log("Host failed to auto-join session.");
        }
    }

    private void switchToGameView() {
        Platform.runLater(() -> {
            gameView = new MP_QView();
            
            // Set up Chat Input behavior
            gameView.getChatInput().setOnAction(e -> {
                String text = gameView.getChatInput().getText().trim();
                if (!text.isEmpty()) {
                    // Protocol: CHAT@Player@Message
                    // Note: You can replace "Player" with a real username from your session later
                    String msg = MP_Protocol.format(MP_Protocol.CHAT, "User", text);
                    client.send(msg);
                    gameView.getChatInput().clear();
                }
            });

            // Swap the scene on the current stage
            Stage stage = (Stage) view.getScene().getWindow();
            Scene gameScene = new Scene(gameView, 1280, 720);
            
            // Apply the style.css to the new scene
            try {
                String css = getClass().getResource("/Pack_1/style.css").toExternalForm();
                gameScene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Could not load CSS in Multiplayer View: " + ex.getMessage());
            }

            stage.setScene(gameScene);
        });
    }

    private void handleIncomingMessage(String raw) {
        Platform.runLater(() -> {
            String[] parts = MP_Protocol.parse(raw);
            if (parts.length < 3) return;

            String type = parts[0];
            String sender = parts[1];
            String content = parts[2];

            if (type.equals(MP_Protocol.CHAT)) {
                if (gameView != null) {
                    gameView.appendMessage(sender + ": " + content);
                }
            }
            // Future steps: Handle QUESTION@ or TIMER@ here
        });
    }
}