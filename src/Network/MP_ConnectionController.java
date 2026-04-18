package Network;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import Pack_1.profile.Session;
import Pack_1.profile.UserManager;

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

    /** Cache PLAYER_LIST if it arrives before gameView exists */
    private String pendingPlayerList = null;

    /**
     * Canonical set of all known players (host + all clients).
     * This is updated on every JOIN/LEAVE and used to seed both
     * the waiting room and the in‑game player list.
     */
    private final Set<String> knownPlayers = new LinkedHashSet<>();

    // Profile / session wiring for multiplayer stats
    private final UserManager userManager;
    private final Session session;

    public MP_ConnectionController(MP_ConnectionView view,
                                   String localPlayerName,
                                   Stage primaryStage,
                                   UserManager userManager,
                                   Session session) {

        this.view = view;
        this.primaryStage = primaryStage;
        this.localPlayerName =
                (localPlayerName == null || localPlayerName.isBlank())
                        ? "Player"
                        : localPlayerName;

        this.userManager = userManager;
        this.session = session;

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
    // RETURN TO NETWORK SETUP
    // -------------------------------------------------------------------------

    /**
     * Returns to the original Network Setup screen (MP_ConnectionView).
     * Used after multiplayer match ends.
     */
    private void returnToNetworkSetup() {
        Platform.runLater(() -> {
            Scene scene = new Scene(view, 1280, 720);
            try {
                String css = getClass().getResource("/Pack_1/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ignored) {}
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        });
    }

    // -------------------------------------------------------------------------
    // GAME VIEW
    // -------------------------------------------------------------------------

    private void switchToGameView() {
        Platform.runLater(() -> {

            gameView = new MP_QView();

            // 🔥 NEW: Ask server for authoritative list
            client.send(MP_Protocol.format(
                    MP_Protocol.REQUEST_PLAYER_LIST,
                    localPlayerName,
                    ""
            ));

            // Seed the player list from the canonical knownPlayers set
            gameView.getPlayerList().getItems().clear();
            for (String name : knownPlayers) {
                if (!gameView.getPlayerList().getItems().contains(name)) {
                    gameView.getPlayerList().getItems().add(name);
                }
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

            // Snapshot of players at game start
            List<String> initialPlayers = knownPlayers.stream().collect(Collectors.toList());

            // Callback to return to Network Setup after match ends
            Runnable returnToNetwork = this::returnToNetworkSetup;

            // Create appropriate controller
            if (isHost) {
                hostGameController = new MP_HostGameController(
                        gameView,
                        server,
                        client,
                        localPlayerName,
                        initialPlayers,
                        userManager,
                        session,
                        returnToNetwork
                );

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
                        localPlayerName,
                        returnToNetwork
                );

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

            // Apply pending PLAYER_LIST now that gameView exists
            if (pendingPlayerList != null) {
                List<String> players = Arrays.asList(pendingPlayerList.split(","));
                gameView.getPlayerList().getItems().setAll(players);
                gameView.updatePlayerNameCards(players, null);
                pendingPlayerList = null;
            }

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

                if (isHost) {
                    broadcastPlayerList();
                }

            } else if (MP_Protocol.LEAVE.equals(type)) {
                knownPlayers.remove(sender);

                if (isHost) {
                    broadcastPlayerList();
                }
            }

            // 🔥 NEW: Handle REQUEST_PLAYER_LIST reply
            if (type.equals(MP_Protocol.REQUEST_PLAYER_LIST)) {
                return;
            }

            // 🔥 PLAYER_LIST must be handled globally and lifecycle‑aware
            if (type.equals(MP_Protocol.PLAYER_LIST)) {

                List<String> players = Arrays.asList(payload.split(","));

                // 1. Waiting room active → update waiting room
                if (waitingRoomController != null) {
                    for (String p : players) {
                        waitingRoomController.ensurePlayerListed(p);
                    }
                    return;
                }

                // 2. Game view active → update game view
                if (gameView != null) {
                    gameView.getPlayerList().getItems().setAll(players);
                    gameView.updatePlayerNameCards(players, null);
                    return;
                }

                // 3. Neither exists yet → cache for later
                pendingPlayerList = payload;
                return;
            }

            // Waiting room handles everything else
            if (waitingRoomController != null) {
                waitingRoomController.handleNetworkMessage(type, sender, payload);
                return;
            }

            // Ignore game messages until gameView exists
            if (gameView == null) {

                // Cache PLAYER_LIST until gameView exists
                if (type.equals(MP_Protocol.PLAYER_LIST)) {
                    pendingPlayerList = payload;
                }

                return;
            }

            // GAME VIEW MESSAGE HANDLING
            switch (type) {

                case MP_Protocol.PLAYER_LIST -> {
                    List<String> players = Arrays.asList(payload.split(","));
                    gameView.getPlayerList().getItems().setAll(players);
                    gameView.updatePlayerNameCards(players, null);
                }

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

    // Helper for multi-user joins
    private void broadcastPlayerList() {
        String payload = String.join(",", knownPlayers);
        client.send(MP_Protocol.format(
                MP_Protocol.PLAYER_LIST,
                localPlayerName,
                payload
        ));
    }

}
