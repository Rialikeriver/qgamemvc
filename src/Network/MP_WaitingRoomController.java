package Network;

import java.util.HashMap;
import java.util.Map;

public class MP_WaitingRoomController {

    private final MP_WaitingRoomView view;
    private final MP_Client client;
    private final boolean isHost;
    private final String localPlayerName;
    private final Runnable onStartGame;

    private final Map<String, Boolean> readyMap = new HashMap<>();

    public MP_WaitingRoomController(MP_WaitingRoomView view,
                                    MP_Client client,
                                    boolean isHost,
                                    String localPlayerName,
                                    Runnable onStartGame) {

        this.view = view;
        this.client = client;
        this.isHost = isHost;
        this.localPlayerName = localPlayerName;
        this.onStartGame = onStartGame;

        wireActions();
    }

    /**
     * Called by MP_ConnectionController immediately after construction
     * to ensure the local player is visible in the waiting room even
     * before any JOIN messages are processed.
     */
    public void addLocalPlayerImmediately() {
        ensurePlayerListed(localPlayerName);
    }

    /**
     * Ensures a player is present in the waiting room list and ready map.
     * Safe to call multiple times.
     */
    public void ensurePlayerListed(String name) {
        if (!view.getPlayerList().getItems().contains(name)) {
            view.getPlayerList().getItems().add(name);
        }
        readyMap.putIfAbsent(name, false);
        updateStatus();
    }

    private void wireActions() {
        view.getReadyBtn().setOnAction(e -> {
            client.send(MP_Protocol.format(
                    MP_Protocol.READY,
                    localPlayerName,
                    "OK"
            ));
            view.getReadyBtn().setDisable(true);
            view.getStatusLabel().setText("You are ready");

            // Immediately reflect local readiness
            markReady(localPlayerName);
        });

        view.getStartBtn().setOnAction(e -> {
            if (onStartGame != null) onStartGame.run();

            client.send(MP_Protocol.format(
                    MP_Protocol.START,
                    localPlayerName,
                    ""
            ));
        });

    }

    public void handleNetworkMessage(String type, String sender, String payload) {
        switch (type) {
            case MP_Protocol.JOIN -> addPlayer(sender);
            case MP_Protocol.LEAVE -> removePlayer(sender);
            case MP_Protocol.READY -> {
                if (!sender.equals(localPlayerName)) {
                    markReady(sender);
                }
            }
            case MP_Protocol.START -> beginGame();
        }
    }

    private void addPlayer(String name) {
        ensurePlayerListed(name);
    }

    private void removePlayer(String name) {
        view.getPlayerList().getItems().remove(name);
        readyMap.remove(name);
        updateStatus();
    }

    private void markReady(String name) {
        readyMap.put(name, true);
        updateStatus();

        if (isHost && allReady()) {
            view.getStatusLabel().setText("All players ready — you may start");
            view.getStartBtn().setDisable(false);
        }
    }

    private boolean allReady() {
        return !readyMap.isEmpty() && readyMap.values().stream().allMatch(v -> v);
    }

    private void updateStatus() {
        long readyCount = readyMap.values().stream().filter(v -> v).count();
        long total = readyMap.size();
        view.getStatusLabel().setText(readyCount + "/" + total + " players ready");
    }

    private void beginGame() {
        if (onStartGame != null) {
            onStartGame.run();
        }
    }
}
