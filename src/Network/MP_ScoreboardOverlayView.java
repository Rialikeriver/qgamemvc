package Network;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MP_ScoreboardOverlayView extends StackPane {

    private final VBox panel;
    private final Button continueBtn;
    private final Label titleLabel;
    private final VBox playerListBox;
    private final Label countdownLabel;

    public MP_ScoreboardOverlayView(boolean isHost) {

        Rectangle dim = new Rectangle(1280, 720);
        dim.setFill(Color.rgb(0, 0, 0, 0.6));

        panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #111122; -fx-background-radius: 20;");

        titleLabel = new Label("Question Results");
        titleLabel.setStyle("-fx-text-fill: #d4af37; -fx-font-size: 32px; -fx-font-weight: bold;");

        playerListBox = new VBox(10);
        playerListBox.setAlignment(Pos.CENTER);

        countdownLabel = new Label("Next question in 30s");
        countdownLabel.setStyle("-fx-text-fill: #ffdf00; -fx-font-size: 22px; -fx-font-weight: bold;");

        continueBtn = new Button("Continue");
        continueBtn.getStyleClass().add("answer-btn");
        continueBtn.setVisible(true);

        // Centered layout, no earningsLabel
        panel.getChildren().addAll(
                titleLabel,
                playerListBox,
                countdownLabel,
                continueBtn
        );

        getChildren().addAll(dim, panel);
    }

    public Button getContinueBtn() { return continueBtn; }
    public VBox getPlayerListBox() { return playerListBox; }
    public Label getCountdownLabel() { return countdownLabel; }
}
