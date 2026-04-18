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

    private String pendingPlayerList = null;

    private final Set<String> knownPlayers = new LinkedHashSet<>();

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

        knownPlayers.add(this.localPlayerName);

        wireActions();
        installWindowRecentering();
    }

    private void wireActions() {

        view.getHostBtn().setOnAction(e -> {
            int port = view.getPort();
            server = new MP_Server(port);
            server.start();
            view.log("Server started on port " + port + ". Waiting for players...");
            isHost = true;

            connectAsClient("127.0.0.1", port);
        });

        view.getJoinBtn().setOnAction(e -> {
            String ip = view.getIp();
            int port = view.getPort();
            isHost = false;
            connectAsClient(ip, port);
        });

        view.getBackBtn().setOnAction(e -> {
            view.log("Back to main menu requested.");
        });
    }

    private void connectAsClient(String ip, int port) {
        client = new MP_Client();
        try {
            client.connect(ip, port, this::handleIncomingMessage);
            view.log("Connected to " + ip + ":" + port);

            switchToWaitingRoom();

        } catch (Exception ex) {
            view.log("Connection failed: " + ex.getMessage());
        }
    }

    private void switchToWaitingRoom() {
        Platform.runLater(() -> {

            MP_WaitingRoomView wrView = new MP_WaitingRoomView(isHost);

            waitingRoomController =
                    new MP_WaitingRoomController(
                            wrView,
                            client,
                            isHost,
                            localPlayerName,
                            this::switchToGameView
                    );

            waitingRoomController.addLocalPlayerImmediately();

            for (String name : knownPlayers) {
                waitingRoomController.ensurePlayerListed(name);
            }

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

    private void returnToNetworkSetup() {
        Platform.runLater(() -> {

            // ⭐ Create a brand new view
            MP_ConnectionView newView = new MP_ConnectionView();

            // Rewire the back button
            newView.getBackBtn().setOnAction(e -> {
                // Return to mode selection
                // (QMillionaireMVC will recreate everything cleanly)
                primaryStage.setScene(null);
            });

            // ⭐ Recreate the controller with the new view
            new MP_ConnectionController(
                    newView,
                    localPlayerName,
                    primaryStage,
                    userManager,
                    session
            );

            Scene scene = new Scene(newView, 1280, 720);
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

            client.send(MP_Protocol.format(
                    MP_Protocol.REQUEST_PLAYER_LIST,
                    localPlayerName,
                    ""
            ));

            gameView.getPlayerList().getItems().clear();
            for (String name : knownPlayers) {
                if (!gameView.getPlayerList().getItems().contains(name)) {
                    gameView.getPlayerList().getItems().add(name);
                }
            }

            gameView.updatePlayerNameCards(gameView.getPlayerList().getItems(), null);

            gameView.getSuperpositionBtn().setVisible(true);
            gameView.getEntanglementBtn().setVisible(true);
            gameView.getInterferenceBtn().setVisible(true);

            gameView.getMenuDiamond().setText("MENU");

            gameView.getChatInput().setOnAction(e -> {
                String text = gameView.getChatInput().getText().trim();
                if (!text.isEmpty()) {
                    client.send(MP_Protocol.format(MP_Protocol.CHAT, localPlayerName, text));
                    gameView.getChatInput().clear();
                }
            });

            List<String> initialPlayers = knownPlayers.stream().collect(Collectors.toList());

            Runnable returnToMainMenu = () -> {
                // Call back into QMillionaireMVC
                Platform.runLater(() -> {
                    // We cannot call showModeSelection() directly from here,
                    // so we signal by clearing the scene.
                    primaryStage.setScene(null);
                });
            };


            if (isHost) {
                hostGameController = new MP_HostGameController(
                        gameView,
                        server,
                        client,
                        localPlayerName,
                        initialPlayers,
                        userManager,
                        session,
                        returnToMainMenu
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
                        returnToMainMenu
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

            if (type.equals(MP_Protocol.REQUEST_PLAYER_LIST)) {
                return;
            }

            if (type.equals(MP_Protocol.PLAYER_LIST)) {

                List<String> players = Arrays.asList(payload.split(","));

                if (waitingRoomController != null) {
                    for (String p : players) {
                        waitingRoomController.ensurePlayerListed(p);
                    }
                    return;
                }

                if (gameView != null) {
                    gameView.getPlayerList().getItems().setAll(players);
                    gameView.updatePlayerNameCards(players, null);
                    return;
                }

                pendingPlayerList = payload;
                return;
            }

            if (waitingRoomController != null) {
                waitingRoomController.handleNetworkMessage(type, sender, payload);
                return;
            }

            if (gameView == null) {
                if (type.equals(MP_Protocol.PLAYER_LIST)) {
                    pendingPlayerList = payload;
                }
                return;
            }

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

    private void broadcastPlayerList() {
        String payload = String.join(",", knownPlayers);
        client.send(MP_Protocol.format(
                MP_Protocol.PLAYER_LIST,
                localPlayerName,
                payload
        ));
    }

}
