package Network;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Canonical set of all known players (host + all clients).
     * This is updated on every JOIN/LEAVE and used to seed both
     * the waiting room and the in‑game player list.
     */
    private final Set<String> knownPlayers = new LinkedHashSet<>();

    public MP_ConnectionController(MP_ConnectionView view,
                                   String localPlayerName,
                                   Stage primaryStage) {

        this.view = view;
        this.primaryStage = primaryStage;
        this.localPlayerName =
                (localPlayerName == null || localPlayerName.isBlank())
                        ? "Player"
                        : localPlayerName;

        // Local player is always known
        knownPlayers.add(this.localPlayerName);

        wireActions();
        installWindowRecentering();
    }

    // -------------------------------------------------------------------------
    // UI WIRING (HOST / JOIN / BACK)
    // -------------------------------------------------------------------------

    private void wireActions() {

        // HOST BUTTON
        view.getHostBtn().setOnAction(e -> {
            int port = view.getPort();
            server = new MP_Server(port);
            server.start();
            view.log("Server started on port " + port + ". Waiting for players...");
            isHost = true;

            // Host also connects as a client to its own server
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

    /**
     * Connects as a client and then transitions to the waiting room.
     * We create the waiting room BEFORE sending JOIN so the JOIN
     * message is never "lost" before the controller exists.
     */
    private void connectAsClient(String ip, int port) {
        client = new MP_Client();
        try {
            client.connect(ip, port, this::handleIncomingMessage);
            view.log("Connected to " + ip + ":" + port);

            // Move to waiting room (controller will send JOIN once ready)
            switchToWaitingRoom();

        } catch (Exception ex) {
            view.log("Connection failed: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // WAITING ROOM
    // -------------------------------------------------------------------------

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

            // Ensure the local player is visible immediately in the waiting room
            waitingRoomController.addLocalPlayerImmediately();

            // Seed any already-known players (e.g., if we joined late)
            for (String name : knownPlayers) {
                waitingRoomController.ensurePlayerListed(name);
            }

            // Now that the controller exists, announce JOIN so everyone
            // (including ourselves) sees this player.
            client.send(MP_Protocol.format(MP_Protocol.JOIN, localPlayerName, ""));

            Scene scene = new Scene(wrView, 1280, 720);

            try {
                String css = getClass().getResource("/Pack_1/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ignored) {}

            primaryStage.setScene(scene);
        });
    }

    // -------------------------------------------------------------------------
    // GAME VIEW
    // -------------------------------------------------------------------------

    private void switchToGameView() {
        Platform.runLater(() -> {

            gameView = new MP_QView();

            // Seed the player list from the canonical knownPlayers set
            gameView.getPlayerList().getItems().clear();
            for (String name : knownPlayers) {
                if (!gameView.getPlayerList().getItems().contains(name)) {
                    gameView.getPlayerList().getItems().add(name);
                }
            }

            // Initial name cards with zero totals (host/client controllers
            // will update earnings later)
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

            // Snapshot of players at game start
            List<String> initialPlayers = knownPlayers.stream().collect(Collectors.toList());

            // Create appropriate controller
            if (isHost) {
                hostGameController = new MP_HostGameController(
                        gameView,
                        server,
                        client,
                        localPlayerName,
                        initialPlayers
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

            // Waiting room no longer active
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

    // -------------------------------------------------------------------------
    // WINDOW RECENTERING
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // MESSAGE ROUTING
    // -------------------------------------------------------------------------

    private void handleIncomingMessage(String raw) {
        String[] parts = MP_Protocol.parse(raw);
        String type = parts[0];
        String sender = parts[1];
        String payload = parts[2];

        Platform.runLater(() -> {

            // Maintain canonical player set
            if (MP_Protocol.JOIN.equals(type)) {
                knownPlayers.add(sender);

                // Host echoes its own JOIN back to new players so they see the host
                if (isHost && !sender.equals(localPlayerName)) {
                    client.send(MP_Protocol.format(
                            MP_Protocol.JOIN,
                            localPlayerName,
                            ""
                    ));
                }

            } else if (MP_Protocol.LEAVE.equals(type)) {
                knownPlayers.remove(sender);
            }

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
