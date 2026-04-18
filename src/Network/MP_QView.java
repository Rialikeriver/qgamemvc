package Network;

import Pack_1.Answer;
import Pack_1.QModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

/**
 * Multiplayer Question View (MP_QView)
 *
 * Displays:
 * - Question text
 * - Four answer buttons
 * - Lifeline buttons
 * - Shared timer
 * - Player name cards (top, with colors and totals)
 * - Money ladder (left, with pips)
 * - Player list + chat (right)
 *
 * This version restores:
 * - Local color palette and per-player color assignment
 * - Player cards rendering
 * - Ladder pips using the same palette
 */
public class MP_QView extends StackPane {

    private final BorderPane gamePane;

    private final Label questionLabel;
    private Label earningsLabel;

    private final Button btnA, btnB, btnC, btnD;
    private final Button superpositionBtn, entanglementBtn, interferenceBtn;
    private final Button menuDiamond;

    private final VBox moneyLadder;
    private final List<Label> ladderCells = new ArrayList<>();

    private final HBox playerNameCardsBar;

    private final TextArea chatLog;
    private final TextField chatInput;
    private final ListView<String> playerList;
    private final Label sharedTimerLabel;

    // Local color palette for players (restored)
    private final Map<String, String> playerColors = new HashMap<>();
    private int nextColorIndex = 0;
    private static final String[] COLOR_PALETTE = {
            "#ff66cc", // magenta-ish (first player)
            "#55ff55",
            "#5599ff",
            "#ffcc33",
            "#ff5555",
            "#66cccc",
            "#cc66ff",
            "#ffaa88"
    };

    public MP_QView() {

        // ---------------------------------------------------------------------
        // ROOT LAYOUT
        // ---------------------------------------------------------------------
        gamePane = new BorderPane();
        gamePane.getStyleClass().add("game-pane");
        gamePane.setPadding(new Insets(40, 15, 40, 45));

        try {
            var resource = getClass().getResource("/assets/WWTB_A_Millionaire_Background.png");
            if (resource != null) {
                gamePane.setStyle(
                        "-fx-background-image: url('" + resource.toExternalForm() + "');" +
                        "-fx-background-size: cover; -fx-background-position: center;"
                );
            } else {
                gamePane.setStyle("-fx-background-color: #000022;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---------------------------------------------------------------------
        // TOP: MENU DIAMOND
        // ---------------------------------------------------------------------
        menuDiamond = new Button();
        menuDiamond.getStyleClass().add("menu-diamond");
        StackPane topPane = new StackPane(menuDiamond);
        StackPane.setAlignment(menuDiamond, Pos.TOP_RIGHT);
        gamePane.setTop(topPane);

        // ---------------------------------------------------------------------
        // LEFT: MONEY LADDER
        // ---------------------------------------------------------------------
        moneyLadder = createLadder();
        moneyLadder.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
        gamePane.setLeft(moneyLadder);

        // ---------------------------------------------------------------------
        // CENTER COLUMN
        // ---------------------------------------------------------------------
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(20, 0, 0, 0));

        // Player name cards
        playerNameCardsBar = new HBox(15);
        playerNameCardsBar.setAlignment(Pos.CENTER);
        playerNameCardsBar.setPadding(new Insets(5, 0, 10, 0));

        // Question label
        questionLabel = new Label("Loading...");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setPrefWidth(600);

        // Answer grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        btnA = createAnswerButton("A");
        btnB = createAnswerButton("B");
        btnC = createAnswerButton("C");
        btnD = createAnswerButton("D");

        grid.add(btnA, 0, 0);
        grid.add(btnB, 1, 0);
        grid.add(btnC, 0, 1);
        grid.add(btnD, 1, 1);

        // Lifelines
        superpositionBtn = new Button("SUPERPOSITION");
        superpositionBtn.getStyleClass().add("answer-btn");

        entanglementBtn = new Button("ENTANGLEMENT");
        entanglementBtn.getStyleClass().add("answer-btn");

        interferenceBtn = new Button("INTERFERENCE");
        interferenceBtn.getStyleClass().add("answer-btn");

        HBox lifelineBox = new HBox(20, superpositionBtn, entanglementBtn, interferenceBtn);
        lifelineBox.setAlignment(Pos.CENTER);

        // Shared timer
        sharedTimerLabel = new Label("30s");
        sharedTimerLabel.setStyle(
                "-fx-font-size: 38px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: linear-gradient(#ffdf00, #d4af37);" +
                "-fx-effect: dropshadow(gaussian, #000000, 10, 0.4, 0, 0);" +
                "-fx-padding: 6 14 6 14;"
        );

        StackPane timerCard = new StackPane(sharedTimerLabel);
        timerCard.setMaxWidth(140);
        timerCard.setMinWidth(120);
        timerCard.setStyle(
                "-fx-background-color: rgba(0,0,0,0.45);" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-border-color: rgba(212,175,55,0.35);" +
                "-fx-border-width: 2;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-effect: dropshadow(gaussian, #000000, 8, 0.3, 0, 0);"
        );

        HBox timerRow = new HBox(timerCard);
        timerRow.setAlignment(Pos.CENTER);
        HBox.setMargin(timerCard, new Insets(0, 0, 0, 14));

        centerBox.getChildren().addAll(
                playerNameCardsBar,
                questionLabel,
                grid,
                lifelineBox,
                timerRow
        );

        BorderPane.setMargin(centerBox, new Insets(20, 0, 0, 40));
        gamePane.setCenter(centerBox);

        // ---------------------------------------------------------------------
        // RIGHT: PLAYER LIST + CHAT
        // ---------------------------------------------------------------------
        VBox rightPane = new VBox(15);
        rightPane.setPadding(new Insets(0, 0, 0, 20));
        rightPane.setPrefWidth(280);

        Label playersTitle = new Label("PLAYERS");
        playersTitle.setStyle(
                "-fx-text-fill: #d4af37;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: rgba(0,0,0,0.55);" +
                "-fx-padding: 4 10 4 10;" +
                "-fx-background-radius: 8;"
        );

        playerList = new ListView<>();
        playerList.setPrefHeight(150);
        playerList.setStyle(
                "-fx-control-inner-background: rgba(0,0,0,0.6);" +
                "-fx-text-fill: white;"
        );

        Label chatTitle = new Label("CHAT");
        chatTitle.setStyle(
                "-fx-text-fill: #d4af37;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: rgba(0,0,0,0.55);" +
                "-fx-padding: 4 10 4 10;" +
                "-fx-background-radius: 8;"
        );

        chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.setWrapText(true);
        chatLog.setPrefHeight(350);
        chatLog.setStyle(
                "-fx-control-inner-background: rgba(0,0,0,0.6);" +
                "-fx-text-fill: white;"
        );

        chatInput = new TextField();
        chatInput.setPromptText("Type message and press Enter...");
        chatInput.getStyleClass().add("answer-btn");

        rightPane.getChildren().addAll(
                playersTitle,
                playerList,
                chatTitle,
                chatLog,
                chatInput
        );
        gamePane.setRight(rightPane);

        getChildren().add(gamePane);
    }

    // -------------------------------------------------------------------------
    // Internal UI builders
    // -------------------------------------------------------------------------

    private Button createAnswerButton(String label) {
        Button b = new Button(label);
        b.getStyleClass().add("answer-btn");
        b.setPrefWidth(300);
        b.setPrefHeight(60);
        return b;
    }

    private VBox createLadder() {
        VBox ladder = new VBox(5);
        ladder.getStyleClass().add("ladder-container");

        List<String> levels = new ArrayList<>(Arrays.asList(QModel.LADDER_VALUES));
        Collections.reverse(levels);

        for (String s : levels) {
            Label l = new Label(s);
            l.getStyleClass().add("ladder-cell");
            l.setMaxWidth(Double.MAX_VALUE);
            ladder.getChildren().add(l);
            ladderCells.add(l);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        ladder.getChildren().add(spacer);

        earningsLabel = new Label("$0");
        earningsLabel.setAlignment(Pos.CENTER);
        earningsLabel.setMaxWidth(Double.MAX_VALUE);
        earningsLabel.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-text-fill: linear-gradient(#ffdf00, #d4af37);" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 0 0 0;" +
                "-fx-effect: dropshadow(gaussian, #d4af37, 8, 0.2, 0, 0);"
        );

        ladder.getChildren().add(earningsLabel);
        return ladder;
    }

    private String getColorForPlayer(String name) {
        return playerColors.computeIfAbsent(name, n -> {
            String c = COLOR_PALETTE[nextColorIndex % COLOR_PALETTE.length];
            nextColorIndex++;
            return c;
        });
    }

    // -------------------------------------------------------------------------
    // Gameplay helpers
    // -------------------------------------------------------------------------

    public void updateQuestion(String text, List<Answer> answers) {
        questionLabel.setText(text);
        btnA.setText(""); btnB.setText(""); btnC.setText(""); btnD.setText("");

        if (answers != null) {
            for (Answer a : answers) {
                String label = a.getLabel().toUpperCase();
                String display = label + ": " + a.getText();
                switch (label) {
                    case "A" -> btnA.setText(display);
                    case "B" -> btnB.setText(display);
                    case "C" -> btnC.setText(display);
                    case "D" -> btnD.setText(display);
                }
            }
        }
    }

    public void updateLadderHighlight(int currentIndex) {
        for (int i = 0; i < ladderCells.size(); i++) {
            ladderCells.get(i).setId(i == ladderCells.size() - 1 - currentIndex ? "current-level" : "");
        }
    }

    public void resetButtons() {
        Button[] btns = {btnA, btnB, btnC, btnD};
        for (Button b : btns) {
            b.setDisable(false);
            b.setOpacity(1.0);
            b.getStyleClass().remove("answer-selected");
        }
    }

    public void disableAnswers(java.util.List<Answer> toDisable) {
        for (Answer a : toDisable) {
            String label = a.getLabel().toUpperCase();
            switch (label) {
                case "A" -> { btnA.setDisable(true); btnA.setOpacity(0.3); }
                case "B" -> { btnB.setDisable(true); btnB.setOpacity(0.3); }
                case "C" -> { btnC.setDisable(true); btnC.setOpacity(0.3); }
                case "D" -> { btnD.setDisable(true); btnD.setOpacity(0.3); }
            }
        }
    }

    public void applyTheme(String themeClass) {
        this.getStyleClass().removeAll(
                "theme-deuteranopia",
                "theme-tritanopia",
                "modern-style",
                "classic-style"
        );
        if (themeClass != null && !themeClass.isEmpty()) {
            this.getStyleClass().add(themeClass);
        }
    }

    public void updateEarnings(int totalMoney) {
        earningsLabel.setText("$" + totalMoney);
    }

    // -------------------------------------------------------------------------
    // Player cards (restored)
    // -------------------------------------------------------------------------

    /**
     * Updates the top player name cards.
     *
     * @param players list of player names (in display order)
     * @param totals  map name -> total earnings (may be null for 0)
     */
    public void updatePlayerNameCards(List<String> players, Map<String, Integer> totals) {
        playerNameCardsBar.getChildren().clear();
        if (players == null || players.isEmpty()) return;

        for (String name : players) {
            String color = getColorForPlayer(name);
            int money = (totals == null) ? 0 : totals.getOrDefault(name, 0);

            Label nameLabel = new Label(name);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setStyle(
                    "-fx-text-fill: " + color + ";" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
            );
            nameLabel.setMaxWidth(160);

            Label moneyLabel = new Label("$" + money);
            moneyLabel.setStyle(
                    "-fx-text-fill: " + color + ";" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
            );

            VBox textBox = new VBox(4, nameLabel, moneyLabel);
            textBox.setAlignment(Pos.CENTER);

            StackPane card = new StackPane(textBox);
            card.setPadding(new Insets(8, 10, 8, 10));
            card.setPrefWidth(180);
            card.setStyle(
                    "-fx-background-color: rgba(40,0,80,0.85);" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-color: #d4af37;" +
                    "-fx-border-width: 2;" +
                    "-fx-effect: dropshadow(gaussian, #000000, 10, 0.4, 0, 0);"
            );

            playerNameCardsBar.getChildren().add(card);
        }
    }

    // -------------------------------------------------------------------------
    // Ladder pips (restored)
    // -------------------------------------------------------------------------

    /**
     * Updates ladder pips for each tier.
     *
     * @param tierToPlayers map tierIndex -> list of player names who hit that tier
     */
    public void updateLadderMarkers(Map<Integer, List<String>> tierToPlayers) {
        if (tierToPlayers == null) {
            for (Label cell : ladderCells) {
                cell.setGraphic(null);
            }
            return;
        }

        int n = ladderCells.size();
        for (int tierIndex = 0; tierIndex < QModel.LADDER_VALUES.length; tierIndex++) {
            int cellIndex = n - 1 - tierIndex;
            if (cellIndex < 0 || cellIndex >= n) continue;

            Label cell = ladderCells.get(cellIndex);
            List<String> players = tierToPlayers.get(tierIndex);
            if (players == null || players.isEmpty()) {
                cell.setGraphic(null);
                continue;
            }

            HBox dots = new HBox(4);
            dots.setAlignment(Pos.CENTER_RIGHT);

            for (String name : players) {
                String color = getColorForPlayer(name);
                Region dot = new Region();
                dot.setPrefSize(10, 10);
                dot.setStyle(
                        "-fx-background-radius: 5;" +
                        "-fx-background-color: " + color + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 1;"
                );
                dots.getChildren().add(dot);
            }

            cell.setGraphic(dots);
        }
    }

    // -------------------------------------------------------------------------
    // Overlay helpers
    // -------------------------------------------------------------------------

    public void showOverlay(Node overlay) {
        getChildren().add(overlay);
    }

    public void hideOverlay(Node overlay) {
        getChildren().remove(overlay);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public TextArea getChatLog() { return chatLog; }
    public TextField getChatInput() { return chatInput; }
    public ListView<String> getPlayerList() { return playerList; }
    public Label getSharedTimerLabel() { return sharedTimerLabel; }

    public void appendChat(String msg) {
        chatLog.appendText(msg + "\n");
    }

    public Button getBtnA() { return btnA; }
    public Button getBtnB() { return btnB; }
    public Button getBtnC() { return btnC; }
    public Button getBtnD() { return btnD; }

    public Button getSuperpositionBtn() { return superpositionBtn; }
    public Button getEntanglementBtn()  { return entanglementBtn; }
    public Button getInterferenceBtn()  { return interferenceBtn; }

    public Button getMenuDiamond() { return menuDiamond; }

    public Label getQuestionLabel() { return questionLabel; }
    public Label getEarningsLabel() { return earningsLabel; }
    
    public String getPlayerColor(String player) {
        return playerColors.getOrDefault(player, "#ffffff");
    }

}
