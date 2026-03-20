package Pack_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class PlayerMenuView extends BorderPane {
    private Button newGameBtn;
    private Button loadGameBtn;
    private Button quitBtn;
    private Button backBtn;

    public PlayerMenuView() {
        setupUI();
    }

    private void setupUI() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        this.setPadding(new Insets(20));

        Label title = new Label("PLAYER MENU");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        title.setPadding(new Insets(20));
        this.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        newGameBtn = new Button("New Game");
        loadGameBtn = new Button("Load Game");
        quitBtn = new Button("Quit");
        backBtn = new Button("Back");
        backBtn.getStyleClass().add("answer-btn");

        newGameBtn.getStyleClass().add("answer-btn");
        loadGameBtn.getStyleClass().add("answer-btn");
        quitBtn.getStyleClass().add("answer-btn");
        backBtn.getStyleClass().add("answer-btn");
        
        VBox centerBox = new VBox(20, newGameBtn, loadGameBtn, quitBtn, backBtn);
        centerBox.setAlignment(Pos.CENTER);
        this.setCenter(centerBox);
    }

    public Button getNewGameBtn() { return newGameBtn; }
    public Button getLoadGameBtn() { return loadGameBtn; }
    public Button getQuitBtn() { return quitBtn; }
    public Button getBackBtn() { return backBtn; }

}
