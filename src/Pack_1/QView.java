package Pack_1;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class QView extends StackPane {
    private BorderPane gamePane;
    protected Label questionLabel, timerLabel;
    protected Button btnA, btnB, btnC, btnD, superpositionBtn, entanglementBtn, menuDiamond, interferenceBtn;
    protected VBox moneyLadder;
    
    public QView() {
        gamePane = new BorderPane();
        gamePane.getStyleClass().add("game-pane");
        gamePane.setPadding(new Insets(20));

        try {
            var resource = getClass().getResource("/WWTB_A_Millionaire_Background.png");
            if (resource != null) {
                gamePane.setStyle("-fx-background-image: url('" + resource.toExternalForm() + "'); " +
                                  "-fx-background-size: cover; -fx-background-position: center;");
            } else {
                gamePane.setStyle("-fx-background-color: #000022;");
            }
        } catch (Exception e) { e.printStackTrace(); }

        initUI();
        getChildren().add(gamePane);
    }

    private void initUI() {
        // Menu and Timer Row
        menuDiamond = new Button();
        menuDiamond.getStyleClass().add("menu-diamond");
        
        timerLabel = new Label("20s");
        timerLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");
        
        StackPane topPane = new StackPane(timerLabel, menuDiamond);
        StackPane.setAlignment(timerLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(menuDiamond, Pos.TOP_RIGHT);
        gamePane.setTop(topPane);

        moneyLadder = createLadder();
        moneyLadder.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
        gamePane.setLeft(moneyLadder);
        
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.CENTER);

        questionLabel = new Label("Loading...");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setPrefWidth(600);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        btnA = createAnswerButton("A");
        btnB = createAnswerButton("B");
        btnC = createAnswerButton("C");
        btnD = createAnswerButton("D");

        grid.add(btnA, 0, 0); grid.add(btnB, 1, 0);
        grid.add(btnC, 0, 1); grid.add(btnD, 1, 1);

        superpositionBtn = new Button("SUPERPOSITION");
        superpositionBtn.getStyleClass().add("answer-btn");
        entanglementBtn = new Button("ENTANGLEMENT");
        entanglementBtn.getStyleClass().add("answer-btn");
        
        interferenceBtn = new Button("INTERFERENCE"); //^
        interferenceBtn.getStyleClass().add("answer-btn");
        

        HBox lifelineBox = new HBox(20, superpositionBtn, entanglementBtn, interferenceBtn);
        lifelineBox.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(questionLabel, grid, lifelineBox);
        gamePane.setCenter(centerBox);
    }

    private Button createAnswerButton(String label) {
        Button b = new Button(label);
        b.getStyleClass().add("answer-btn");
        b.setPrefWidth(300); b.setPrefHeight(60);
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
        }
        return ladder;
    }

    public void updateQuestion(String text, List<Answer> answers) {
        questionLabel.setText(text);
        btnA.setText(""); btnB.setText(""); btnC.setText(""); btnD.setText("");
        if (answers != null) {
            for (Answer a : answers) {
                String label = a.getLabel().toUpperCase();
                String display = label + ": " + a.getText();
                if (label.equals("A")) btnA.setText(display);
                else if (label.equals("B")) btnB.setText(display);
                else if (label.equals("C")) btnC.setText(display);
                else if (label.equals("D")) btnD.setText(display);
            }
        }
    }

    public void updateLadderHighlight(int currentIndex) {
        int total = moneyLadder.getChildren().size();
        int target = total - 1 - currentIndex; 
        for (int i = 0; i < total; i++) {
            moneyLadder.getChildren().get(i).setId(i == target ? "current-level" : "");
        }
    }

    public void resetButtons() {
        Button[] btns = {btnA, btnB, btnC, btnD};
        for (Button b : btns) { b.setDisable(false); b.setOpacity(1.0); }
    }

    public void disableAnswers(List<Answer> toDisable) {
        for (Answer a : toDisable) {
            String label = a.getLabel().toUpperCase();
            if (label.equals("A")) { btnA.setDisable(true); btnA.setOpacity(0.3); }
            else if (label.equals("B")) { btnB.setDisable(true); btnB.setOpacity(0.3); }
            else if (label.equals("C")) { btnC.setDisable(true); btnC.setOpacity(0.3); }
            else if (label.equals("D")) { btnD.setDisable(true); btnD.setOpacity(0.3); }
        }
    }

    public void applyTheme(String themeClass) {
        this.getStyleClass().removeAll("theme-deuteranopia", "theme-tritanopia", "modern-style", "classic-style");
        if (themeClass != null && !themeClass.isEmpty()) this.getStyleClass().add(themeClass);
    }

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
    
    // Overlay Methods
    public void showOverlay(javafx.scene.Node overlay) {
        getChildren().add(overlay);
    }

    public void hideOverlay(javafx.scene.Node overlay) {
        getChildren().remove(overlay);
    }
}

