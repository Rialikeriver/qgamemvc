package Network;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.*;

/**
 * Client-side multiplayer game controller.
 * - Does NOT own a QModel; host is authoritative.
 * - Reacts to QUESTION/TIMER/WIN/BOARD/LIFELINE/SCOREBOARD_TIMER messages.
 * - Sends ANSWER, LIFELINE requests, and CONTINUE when local player interacts.
 */
public class MP_ClientGameController {

    private final MP_QView mpView;
    private final MP_Client client;
    private final String playerName;
    private MP_ScoreboardOverlayView currentOverlay;

    private boolean hasAnsweredCurrentQuestion = false;

    // Callback to return to Network Setup after match ends
    private final Runnable returnToNetworkSetup;

    public MP_ClientGameController(MP_QView mpView,
                                   MP_Client client,
                                   String playerName,
                                   Runnable returnToNetworkSetup) {
        this.mpView = mpView;
        this.client = client;
        this.playerName = (playerName == null || playerName.isBlank())
                ? "Player"
                : playerName;

        this.returnToNetworkSetup = returnToNetworkSetup;

        mpView.getMenuDiamond().setOnAction(e -> showSettingsMenu());

        wireAnswerButtons();
    }

    // -------------------------------------------------------------------------
    // LOCAL ANSWER HANDLING
    // -------------------------------------------------------------------------

    private void wireAnswerButtons() {
        mpView.getBtnA().setOnAction(e -> sendAnswer("A"));
        mpView.getBtnB().setOnAction(e -> sendAnswer("B"));
        mpView.getBtnC().setOnAction(e -> sendAnswer("C"));
        mpView.getBtnD().setOnAction(e -> sendAnswer("D"));
    }

    private void lockLocalSelection(String label) {
        Button a = mpView.getBtnA();
        Button b = mpView.getBtnB();
        Button c = mpView.getBtnC();
        Button d = mpView.getBtnD();

        a.getStyleClass().remove("answer-selected");
        b.getStyleClass().remove("answer-selected");
        c.getStyleClass().remove("answer-selected");
        d.getStyleClass().remove("answer-selected");

        a.setDisable(true);
        b.setDisable(true);
        c.setDisable(true);
        d.setDisable(true);

        switch (label.toUpperCase()) {
            case "A" -> a.getStyleClass().add("answer-selected");
            case "B" -> b.getStyleClass().add("answer-selected");
            case "C" -> c.getStyleClass().add("answer-selected");
            case "D" -> d.getStyleClass().add("answer-selected");
        }
    }

    private void sendAnswer(String label) {
        if (hasAnsweredCurrentQuestion) return;
        hasAnsweredCurrentQuestion = true;

        Platform.runLater(() -> lockLocalSelection(label));

        String msg = MP_Protocol.format(
                MP_Protocol.ANSWER,
                playerName,
                label
        );
        client.send(msg);
    }

    // -------------------------------------------------------------------------
    // LIFELINE REQUESTS (CLIENT → HOST)
    // -------------------------------------------------------------------------

    public void requestSuperposition() {
        String payload = "REQUEST|SUPERPOSITION";
        client.send(MP_Protocol.format(
                MP_Protocol.LIFELINE,
                playerName,
                payload
        ));
    }

    public void requestEntanglement() {
        String payload = "REQUEST|ENTANGLEMENT";
        client.send(MP_Protocol.format(
                MP_Protocol.LIFELINE,
                playerName,
                payload
        ));
    }

    public void requestInterference() {
        String payload = "REQUEST|INTERFERENCE";
        client.send(MP_Protocol.format(
                MP_Protocol.LIFELINE,
                playerName,
                payload
        ));
    }

    // -------------------------------------------------------------------------
    // NETWORK MESSAGE HANDLING
    // -------------------------------------------------------------------------

    public void handleNetworkMessage(String type, String sender, String payload) {
        switch (type) {
            case MP_Protocol.QUESTION -> handleQuestion(payload);
            case MP_Protocol.TIMER    -> handleTimer(payload);
            case MP_Protocol.WIN      -> handleWin(payload);
            case MP_Protocol.BOARD    -> showScoreboard(payload);
            case MP_Protocol.LIFELINE -> handleLifeline(payload);
            case MP_Protocol.SCOREBOARD_TIMER -> {
                if (currentOverlay != null) {
                    currentOverlay.getCountdownLabel().setText(
                        "Next question in " + payload + "s"
                    );
                }
            }
            default -> {}
        }
    }

    private void handleQuestion(String payload) {
        hasAnsweredCurrentQuestion = false;

        mpView.getChildren().removeIf(node -> node instanceof MP_ScoreboardOverlayView);
        currentOverlay = null;

        String[] parts = payload.split("\\|");
        if (parts.length < 3) return;

        // Host sends: questionIndex|tier|questionText|...
        String indexStr = parts[0];
        String questionText = parts[2];

        mpView.getQuestionLabel().setText(questionText);
        mpView.resetButtons();

        mpView.getBtnA().setText("A:");
        mpView.getBtnB().setText("B:");
        mpView.getBtnC().setText("C:");
        mpView.getBtnD().setText("D:");

        for (int i = 3; i < parts.length; i++) {
            String entry = parts[i];
            if (entry.isEmpty()) continue;
            String[] kv = entry.split(":", 2);
            if (kv.length < 2) continue;
            String label = kv[0];
            String text = kv[1];

            switch (label.toUpperCase()) {
                case "A" -> mpView.getBtnA().setText("A: " + text);
                case "B" -> mpView.getBtnB().setText("B: " + text);
                case "C" -> mpView.getBtnC().setText("C: " + text);
                case "D" -> mpView.getBtnD().setText("D: " + text);
            }
        }

        try {
            int qIndex = Integer.parseInt(indexStr);
            int index = Math.max(0, qIndex);
            mpView.updateLadderHighlight(index);
        } catch (NumberFormatException ignored) {}
    }

    private void handleTimer(String payload) {
        mpView.getSharedTimerLabel().setText(payload + "s");
    }

    private void handleWin(String payload) {
        mpView.appendChat("*** Game over: " + payload + " ***");

        // Simple end-of-game overlay showing winners
        MP_ScoreboardOverlayView overlay = new MP_ScoreboardOverlayView(false);
        VBox list = overlay.getPlayerListBox();

        String winnersText = (payload == null || payload.isBlank())
                ? "No winners"
                : "Winner(s): " + payload;

        Label lbl = new Label(winnersText);
        lbl.setStyle(
            "-fx-text-fill: #d4af37;" +
            "-fx-font-size: 22px;"
        );
        list.getChildren().add(lbl);

        overlay.getCountdownLabel().setText("Match complete");

        overlay.getContinueBtn().setDisable(true);

        mpView.getChildren().add(overlay);
        currentOverlay = overlay;

        // Return to Network Setup after ~20 seconds
        Timeline exitTimer = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            if (returnToNetworkSetup != null) {
                returnToNetworkSetup.run();
            }
        }));
        exitTimer.setCycleCount(1);
        exitTimer.play();
    }

    private void handleLifeline(String payload) {
        String[] parts = payload.split("\\|");
        if (parts.length < 2) return;

        String type = parts[0];
        String data = parts[1];
        String usedBy = (parts.length >= 3) ? parts[2] : "Host";

        switch (type) {
            case "SUPERPOSITION" -> {
                for (String label : data.split(",")) {
                    switch (label) {
                        case "A" -> { mpView.getBtnA().setDisable(true); mpView.getBtnA().setOpacity(0.3); }
                        case "B" -> { mpView.getBtnB().setDisable(true); mpView.getBtnB().setOpacity(0.3); }
                        case "C" -> { mpView.getBtnC().setDisable(true); mpView.getBtnC().setOpacity(0.3); }
                        case "D" -> { mpView.getBtnD().setDisable(true); mpView.getBtnD().setOpacity(0.3); }
                    }
                }
                mpView.getSuperpositionBtn().setDisable(true);
                mpView.appendChat("★ " + usedBy + " used SUPERPOSITION");
            }
            case "ENTANGLEMENT" -> {
                mpView.getEntanglementBtn().setDisable(true);
                mpView.appendChat("★ " + usedBy + " used ENTANGLEMENT (entangled: " + data + ")");
            }
            case "INTERFERENCE" -> {
                mpView.getInterferenceBtn().setDisable(true);
                mpView.appendChat("★ " + usedBy + " used INTERFERENCE (reveals: " + data + ")");
            }
        }
    }

    // -------------------------------------------------------------------------
    // SCOREBOARD OVERLAY (CLIENT VIEW)
    // -------------------------------------------------------------------------

    private void showScoreboard(String payload) {
        MP_ScoreboardOverlayView overlay = new MP_ScoreboardOverlayView(false);
        VBox list = overlay.getPlayerListBox();

        Map<String, Integer> totals = new LinkedHashMap<>();
        Map<Integer, List<String>> tierHits = new HashMap<>();

        String[] entries = payload.split("\\|");
        for (String entry : entries) {
            if (entry.startsWith("TIERS:")) {
                String tiersPart = entry.substring("TIERS:".length());
                if (!tiersPart.isEmpty()) {
                    String[] segments = tiersPart.split(";");
                    for (String seg : segments) {
                        if (seg.isEmpty()) continue;
                        String[] kv = seg.split("=", 2);
                        if (kv.length < 2) continue;
                        try {
                            int tierIndex = Integer.parseInt(kv[0]);
                            String[] names = kv[1].split(",");
                            List<String> players = new ArrayList<>();
                            for (String n : names) {
                                if (!n.isEmpty()) players.add(n);
                            }
                            if (!players.isEmpty()) {
                                tierHits.put(tierIndex, players);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                continue;
            }

            String[] parts2 = entry.split(":");
            if (parts2.length < 4) continue;

            String name = parts2[0];
            String alive = parts2[1];
            String correct = parts2[2];
            String moneyStr = parts2[3];

            int money = 0;
            try {
                money = Integer.parseInt(moneyStr.trim());
            } catch (NumberFormatException ignored) {}

            totals.put(name, money);

            Label lbl = new Label(name + " — " + correct.toUpperCase() + " — $" + money);
            
            String hex = mpView.getPlayerColor(name);

            lbl.setStyle(
                "-fx-text-fill: " + hex + ";" +
                "-fx-font-size: 18px;"
            );


            list.getChildren().add(lbl);
        }

        // Update top name cards and ladder pips
        mpView.updatePlayerNameCards(mpView.getPlayerList().getItems(), totals);
        mpView.updateLadderMarkers(tierHits);

        Integer myTotal = totals.get(playerName);
        if (myTotal != null) {
            mpView.updateEarnings(myTotal);
        }

        overlay.getCountdownLabel().setText("Next question in 30s");

        overlay.getContinueBtn().setOnAction(e -> {
            client.send(MP_Protocol.format(
                    MP_Protocol.CONTINUE,
                    playerName,
                    ""
            ));
            overlay.getContinueBtn().setDisable(true);
        });

        currentOverlay = overlay;

        mpView.getChildren().add(overlay);
    }

    // -------------------------------------------------------------------------
    // SETTINGS MENU
    // -------------------------------------------------------------------------

    private void showSettingsMenu() {
        ContextMenu settingsMenu = new ContextMenu();

        Menu themeMenu = new Menu("Themes & Accessibility");

        MenuItem modern = new MenuItem("Modern Style");
        MenuItem classic = new MenuItem("Classic Style");
        MenuItem deuteranopia = new MenuItem("Deuteranopia (Red-Green)");
        MenuItem tritanopia = new MenuItem("Tritanopia (Blue-Yellow)");

        themeMenu.getItems().addAll(
                modern,
                classic,
                new SeparatorMenuItem(),
                deuteranopia,
                tritanopia
        );

        modern.setOnAction(e -> mpView.applyTheme("modern-style"));
        classic.setOnAction(e -> mpView.applyTheme("classic-style"));
        deuteranopia.setOnAction(e -> mpView.applyTheme("theme-deuteranopia"));
        tritanopia.setOnAction(e -> mpView.applyTheme("theme-tritanopia"));

        settingsMenu.getItems().add(themeMenu);

        settingsMenu.show(mpView.getMenuDiamond(), Side.BOTTOM, 0, 0);
    }
}
