package Pack_1;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

/**
 * Main gameplay view for Quantum Millionaire. This class builds the full
 * interactive UI: question display, answer grid, lifeline controls, timer
 * panel, money ladder, and overlay support. It acts purely as a presentation
 * layer, with all game logic handled by {@link QController}.
 *
 * <p>The view exposes getters for all interactive controls so the controller
 * can attach event handlers. It also provides helper methods for updating
 * question text, highlighting the ladder, disabling answers, applying themes,
 * and showing/hiding overlays.</p>
 */
public class QView extends StackPane {

    private BorderPane gamePane;

    protected Label questionLabel;
    protected Label timerLabel;
    protected Label earningsLabel;

    protected Button btnA, btnB, btnC, btnD;
    protected Button superpositionBtn, entanglementBtn, interferenceBtn;
    protected Button menuDiamond;

    protected VBox moneyLadder;
    private List<Label> ladderCells = new ArrayList<>();

    /**
     * Constructs the full gameplay UI and loads the background image if available.
     */
    public QView() {
        gamePane = new BorderPane();
        gamePane.getStyleClass().add("game-pane");
        gamePane.setPadding(new Insets(20));

        try {
            var resource = getClass().getResource("/WWTB_A_Millionaire_Background.png");
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

        initUI();
        getChildren().add(gamePane);
    }

    /**
     * Builds all UI components: top bar, money ladder, timer panel, question box,
     * answer grid, and lifeline buttons.
     */
    private void initUI() {

        // Top-right menu button
        menuDiamond = new Button();
        menuDiamond.getStyleClass().add("menu-diamond");

        timerLabel = new Label("20s");
        timerLabel.getStyleClass().add("timer-base");

        StackPane topPane = new StackPane(menuDiamond);
        StackPane.setAlignment(menuDiamond, Pos.TOP_RIGHT);
        gamePane.setTop(topPane);

        // Money ladder (left)
        moneyLadder = createLadder();
        moneyLadder.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
        gamePane.setLeft(moneyLadder);

        // Timer panel (right)
        VBox timerPane = new VBox();
        timerPane.setAlignment(Pos.CENTER);
        timerPane.setPadding(new Insets(20));
        timerPane.setMinWidth(200);
        timerPane.setPrefWidth(200);
        timerLabel.setMaxWidth(Double.MAX_VALUE);
        timerLabel.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(timerLabel, Priority.ALWAYS);
        timerPane.getChildren().add(timerLabel);
        gamePane.setRight(timerPane);

        // Center: question + answers + lifelines
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.CENTER);

        questionLabel = new Label("Loading...");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setPrefWidth(600);

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

        superpositionBtn = new Button("SUPERPOSITION");
        superpositionBtn.getStyleClass().add("answer-btn");

        entanglementBtn = new Button("ENTANGLEMENT");
        entanglementBtn.getStyleClass().add("answer-btn");

        interferenceBtn = new Button("INTERFERENCE");
        interferenceBtn.getStyleClass().add("answer-btn");

        HBox lifelineBox = new HBox(20, superpositionBtn, entanglementBtn, interferenceBtn);
        lifelineBox.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(questionLabel, grid, lifelineBox);
        gamePane.setCenter(centerBox);
    }

    /**
     * Creates a styled answer button with fixed dimensions.
     */
    private Button createAnswerButton(String label) {
        Button b = new Button(label);
        b.getStyleClass().add("answer-btn");
        b.setPrefWidth(300);
        b.setPrefHeight(60);
        return b;
    }

    /**
     * Builds the money ladder UI and stores references to each tier cell.
     */
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

    /**
     * Updates the question text and answer button labels.
     */
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

    /**
     * Highlights the current tier on the money ladder.
     */
    public void updateLadderHighlight(int currentIndex) {
        for (int i = 0; i < ladderCells.size(); i++) {
            ladderCells.get(i).setId(i == ladderCells.size() - 1 - currentIndex ? "current-level" : "");
        }
    }

    /**
     * Re-enables all answer buttons and restores full opacity.
     */
    public void resetButtons() {
        Button[] btns = {btnA, btnB, btnC, btnD};
        for (Button b : btns) {
            b.setDisable(false);
            b.setOpacity(1.0);
        }
    }

    /**
     * Disables the specified answers (used by Superposition lifeline).
     */
    public void disableAnswers(List<Answer> toDisable) {
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

    /**
     * Applies a visual theme class to the root view.
     */
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

    // --- Getters for controller wiring ---

    public Button getBtnA() { return btnA; }
    public Button getBtnB() { return btnB; }
    public Button getBtnC() { return btnC; }
    public Button getBtnD() { return btnD; }

    public Button getSuperpositionBtn() { return superpositionBtn; }
    public Button getEntanglementBtn() { return entanglementBtn; }
    public Button getInterferenceBtn() { return interferenceBtn; }

    public Button getMenuDiamond() { return menuDiamond; }

    public Label getTimerLabel() { return timerLabel; }
    public Label getQuestionLabel() { return questionLabel; }
    public Label getEarningsLabel() { return earningsLabel; }

    /**
     * Adds an overlay node (e.g., GameOverlayView or MiniPopupView) above the game UI.
     */
    public void showOverlay(Node overlay) {
        getChildren().add(overlay);
    }

    /**
     * Removes a previously shown overlay node.
     */
    public void hideOverlay(Node overlay) {
        getChildren().remove(overlay);
    }
}
