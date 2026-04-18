package Network;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Handles hosting/joining a multiplayer session and transitioning
 * into the multiplayer game view (MP_QView).
 *
 * Delegates game-related messages to either:
 * - MP_HostGameController (when this instance is the host)
 * - MP_ClientGameController (when this instance is a client)
 */
public class MP_ConnectionController {

    private final MP_ConnectionView view;
    private final Stage primaryStage;

    private MP_Server server;
    private MP_Client client;
    private MP_QView gameView;

    private MP_HostGameController hostGameController;
    private MP_ClientGameController clientGameController;
    private MP_WaitingRoomController waitingRoomController;

    private final String localPlayerName;
    private boolean isHost = false;

    private boolean recenteringInstalled = false;

    public MP_ConnectionController(MP_ConnectionView view,
                                   String localPlayerName,
                                   Stage primaryStage) {

        this.view = view;
        this.primaryStage = primaryStage;
        this.localPlayerName =
                (localPlayerName == null || localPlayerName.isBlank())
                        ? "Player"
                        : localPlayerName;

        wireActions();
        installWindowRecentering();
    }

    private void wireActions() {

        // HOST BUTTON
        view.getHostBtn().setOnAction(e -> {
            int port = view.getPort();
            server = new MP_Server(port);
            server.start();
            view.log("Server started on port " + port + ". Waiting for players...");
            isHost = true;

            connectAsClient("127.0.0.1", port);
        });

        // JOIN BUTTON
        view.getJoinBtn().setOnAction(e -> {
            String ip = view.getIp();
            int port = view.getPort();
            isHost = false;
            connectAsClient(ip, port);
        });

        // BACK BUTTON
        view.getBackBtn().setOnAction(e -> {
            view.log("Back to main menu requested.");
            // Hook into your main MVC if needed
        });
    }

    private void connectAsClient(String ip, int port) {
        client = new MP_Client();
        try {
            client.connect(ip, port, this::handleIncomingMessage);
            view.log("Connected to " + ip + ":" + port);

            client.send(MP_Protocol.format(MP_Protocol.JOIN, localPlayerName, ""));

            switchToWaitingRoom();

        } catch (Exception ex) {
            view.log("Connection failed: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------
    // WAITING ROOM
    // ------------------------------------------------------------

    private void switchToWaitingRoom() {
        Platform.runLater(() -> {

            MP_WaitingRoomView wrView = new MP_WaitingRoomView(isHost);

            waitingRoomController =
                    new MP_WaitingRoomController(
                            wrView,
                            client,
                            isHost,
                            localPlayerName,
                            this::switchToGameView   // callback when START received
                    );

            Scene scene = new Scene(wrView, 1280, 720);

            try {
                String css = getClass().getResource("/Pack_1/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ignored) {}

            primaryStage.setScene(scene);
        });
    }

    // ------------------------------------------------------------
    // GAME VIEW
    // ------------------------------------------------------------

    private void switchToGameView() {
        Platform.runLater(() -> {

            gameView = new MP_QView();

            // Ensure local player appears immediately in the player list
            if (!gameView.getPlayerList().getItems().contains(localPlayerName)) {
                gameView.getPlayerList().getItems().add(localPlayerName);
            }

            // Initial name cards with zero totals
            gameView.updatePlayerNameCards(gameView.getPlayerList().getItems(), null);

            // Lifelines visible for ALL players
            gameView.getSuperpositionBtn().setVisible(true);
            gameView.getEntanglementBtn().setVisible(true);
            gameView.getInterferenceBtn().setVisible(true);

            // Label the menu diamond
            gameView.getMenuDiamond().setText("MENU");

            // Chat input
            gameView.getChatInput().setOnAction(e -> {
                String text = gameView.getChatInput().getText().trim();
                if (!text.isEmpty()) {
                    client.send(MP_Protocol.format(MP_Protocol.CHAT, localPlayerName, text));
                    gameView.getChatInput().clear();
                }
            });

            // Create appropriate controller
            if (isHost) {
                hostGameController = new MP_HostGameController(
                        gameView,
                        server,
                        client,
                        localPlayerName
                );

                // Host lifeline buttons (host is a normal player)
                gameView.getSuperpositionBtn().setOnAction(e ->
                        hostGameController.applySuperposition(localPlayerName)
                );
                gameView.getEntanglementBtn().setOnAction(e ->
                        hostGameController.applyEntanglement(localPlayerName)
                );
                gameView.getInterferenceBtn().setOnAction(e ->
                        hostGameController.applyInterference(localPlayerName)
                );

            } else {
                clientGameController = new MP_ClientGameController(
                        gameView,
                        client,
                        localPlayerName
                );

                // Client lifeline buttons send requests to host
                gameView.getSuperpositionBtn().setOnAction(e ->
                        clientGameController.requestSuperposition()
                );
                gameView.getEntanglementBtn().setOnAction(e ->
                        clientGameController.requestEntanglement()
                );
                gameView.getInterferenceBtn().setOnAction(e ->
                        clientGameController.requestInterference()
                );
            }

            waitingRoomController = null;

            Scene gameScene = new Scene(gameView, 1440, 720);

            try {
                String css = getClass().getResource("/Pack_1/style.css").toExternalForm();
                gameScene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Could not load CSS in Multiplayer View: " + ex.getMessage());
            }

            primaryStage.setScene(gameScene);
            primaryStage.centerOnScreen();

        });
    }

    // ------------------------------------------------------------
    // WINDOW RECENTERING
    // ------------------------------------------------------------

    private void installWindowRecentering() {
        if (recenteringInstalled) return;
        recenteringInstalled = true;

        primaryStage.setOnShown(ev -> {
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                primaryStage.centerOnScreen();
            });

            primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                primaryStage.centerOnScreen();
            });
        });
    }

    // ------------------------------------------------------------
    // MESSAGE ROUTING
    // ------------------------------------------------------------

    private void handleIncomingMessage(String raw) {
        String[] parts = MP_Protocol.parse(raw);
        String type = parts[0];
        String sender = parts[1];
        String payload = parts[2];

        Platform.runLater(() -> {

            // Waiting room gets first chance
            if (waitingRoomController != null) {
                waitingRoomController.handleNetworkMessage(type, sender, payload);
                return;
            }

            // Ignore game messages until gameView exists
            if (gameView == null) return;

            switch (type) {

                case MP_Protocol.CHAT -> {
                    gameView.appendChat(sender + ": " + payload);
                }

                case MP_Protocol.JOIN -> {
                    if (!gameView.getPlayerList().getItems().contains(sender)) {
                        gameView.getPlayerList().getItems().add(sender);
                    }
                    gameView.updatePlayerNameCards(gameView.getPlayerList().getItems(), null);
                    gameView.appendChat("*** " + sender + " joined ***");
                }

                case MP_Protocol.LEAVE -> {
                    gameView.getPlayerList().getItems().remove(sender);
                    gameView.updatePlayerNameCards(gameView.getPlayerList().getItems(), null);
                    gameView.appendChat("*** " + sender + " left ***");
                }

                default -> {
                    if (isHost && hostGameController != null) {
                        hostGameController.handleNetworkMessage(type, sender, payload);
                    } else if (!isHost && clientGameController != null) {
                        clientGameController.handleNetworkMessage(type, sender, payload);
                    }
                }
            }
        });
    }
}
