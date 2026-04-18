package Network;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MP_WaitingRoomView extends BorderPane {

    private final ListView<String> playerList;
    private final Button readyBtn;
    private final Button startBtn;
    private final Label statusLabel;

    public MP_WaitingRoomView(boolean isHost) {

        setPadding(new Insets(40));
        setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");

        Label title = new Label("WAITING ROOM");
        title.setStyle("-fx-font-size: 36px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.TOP_CENTER);
        setTop(title);

        playerList = new ListView<>();
        playerList.setPrefWidth(300);
        playerList.setStyle("-fx-control-inner-background: rgba(0,0,0,0.6); -fx-text-fill: white;");

        VBox centerBox = new VBox(20, new Label("Players:"), playerList);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        ((Label) centerBox.getChildren().get(0)).setStyle("-fx-text-fill: #d4af37; -fx-font-size: 20px;");
        setCenter(centerBox);

        readyBtn = new Button("READY");
        readyBtn.getStyleClass().add("answer-btn");

        startBtn = new Button("START GAME");
        startBtn.getStyleClass().add("answer-btn");
        startBtn.setVisible(isHost);
        if (isHost) {
            startBtn.setDisable(true); // enabled when all ready
        }

        statusLabel = new Label("Waiting for players...");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        VBox bottomBox = new VBox(15, statusLabel, readyBtn, startBtn);
        bottomBox.setAlignment(Pos.CENTER);
        setBottom(bottomBox);
    }

    public ListView<String> getPlayerList() { return playerList; }
    public Button getReadyBtn() { return readyBtn; }
    public Button getStartBtn() { return startBtn; }
    public Label getStatusLabel() { return statusLabel; }
}
