package Pack_1;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class QView extends BorderPane {
    protected Label questionLabel;
    // Added entanglementBtn here
    protected Button btnA, btnB, btnC, btnD, superpositionBtn, entanglementBtn, menuDiamond;
    protected VBox moneyLadder;
    
    public QView() {
        this.getStyleClass().add("game-pane");
        this.setPadding(new Insets(20));
        
        try {
            var resource = getClass().getResource("/WWTB_A_Millionaire_Background.png");
            if (resource != null) {
                String bgPath = resource.toExternalForm();
                this.setStyle(
                    "-fx-background-image: url('" + bgPath + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center; " +
                    "-fx-background-repeat: no-repeat;"
                );
            } else {
                this.setStyle("-fx-background-color: #000022;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
    }

    public void disableAnswers(List<Answer> toDisable) {
        for (Answer a : toDisable) {
            String label = a.getLabel().toUpperCase(); 
            
            if (label.equals("A")) { getBtnA().setDisable(true); getBtnA().setOpacity(0.3); }
            else if (label.equals("B")) { getBtnB().setDisable(true); getBtnB().setOpacity(0.3); }
            else if (label.equals("C")) { getBtnC().setDisable(true); getBtnC().setOpacity(0.3); }
            else if (label.equals("D")) { getBtnD().setDisable(true); getBtnD().setOpacity(0.3); }
        }
    }
    
    private void initUI() {
        menuDiamond = new Button();
        menuDiamond.getStyleClass().add("menu-diamond");
        StackPane topPane = new StackPane(menuDiamond);
        topPane.setAlignment(Pos.TOP_RIGHT);
        this.setTop(topPane);

        moneyLadder = createLadder();
        moneyLadder.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
        this.setLeft(moneyLadder);
        
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: transparent;");

        questionLabel = new Label("Loading...");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setPrefWidth(600);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: transparent;");

        btnA = createAnswerButton("A");
        btnB = createAnswerButton("B");
        btnC = createAnswerButton("C");
        btnD = createAnswerButton("D");

        grid.add(btnA, 0, 0); grid.add(btnB, 1, 0);
        grid.add(btnC, 0, 1); grid.add(btnD, 1, 1);

        // Setup Superposition Button
        superpositionBtn = new Button("SUPERPOSITION");
        superpositionBtn.getStyleClass().add("answer-btn");
        superpositionBtn.setPrefWidth(200);

        // Setup Entanglement Button
        entanglementBtn = new Button("ENTANGLEMENT");
        entanglementBtn.getStyleClass().add("answer-btn");
        entanglementBtn.setPrefWidth(200);

        // Put the lifelines in an HBox so they sit side-by-side
        HBox lifelineBox = new HBox(20, superpositionBtn, entanglementBtn);
        lifelineBox.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(questionLabel, grid, lifelineBox);
        this.setCenter(centerBox);
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

    public void updateLadderHighlight(int currentIndex) {
        int total = moneyLadder.getChildren().size();
        int target = total - 1 - currentIndex; 

        for (int i = 0; i < total; i++) {
            Node n = moneyLadder.getChildren().get(i);
            n.setId(i == target ? "current-level" : "");
        }
    }
    public void updateQuestion(String text, List<Answer> answers) {
        questionLabel.setText(text);
        
        // Clear old text first to be safe
        btnA.setText(""); btnB.setText(""); btnC.setText(""); btnD.setText("");

        if (answers != null) {
            for (Answer a : answers) {
                String label = a.getLabel().toUpperCase();
                String displayText = label + ": " + a.getText();
                
                // This places the answer on the button matching its NEW label
                switch (label) {
                    case "A": btnA.setText(displayText); break;
                    case "B": btnB.setText(displayText); break;
                    case "C": btnC.setText(displayText); break;
                    case "D": btnD.setText(displayText); break;
                }
            }
        }
    }

    public void highlightQuantumState(List<Answer> remaining) {
        Button[] btns = {btnA, btnB, btnC, btnD};
        for (Button b : btns) {
            boolean match = remaining.stream().anyMatch(a -> b.getText().contains(a.getText()));
            if (!match) b.setVisible(false);
        }
    }

    public void resetButtons() {
        getBtnA().setDisable(false); getBtnA().setOpacity(1.0);
        getBtnB().setDisable(false); getBtnB().setOpacity(1.0);
        getBtnC().setDisable(false); getBtnC().setOpacity(1.0);
        getBtnD().setDisable(false); getBtnD().setOpacity(1.0);
    }

    public void applyTheme(String themeClass) {
        this.getStyleClass().removeAll("theme-deuteranopia", "theme-tritanopia", "modern-style", "classic-style");
        if (themeClass != null && !themeClass.isEmpty()) {
            this.getStyleClass().add(themeClass);
        }
    }

    // --- Getters ---
    public Button getBtnA() { return btnA; }
    public Button getBtnB() { return btnB; }
    public Button getBtnC() { return btnC; }
    public Button getBtnD() { return btnD; }
    public Button getSuperpositionBtn() { return superpositionBtn; }
    // ADDED GETTER FOR ENTANGLEMENT
    public Button getEntanglementBtn() { return entanglementBtn; }
    public Button getMenuDiamond() { return menuDiamond; }
}