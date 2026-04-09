package Network;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * A copy of the game view modified for Multiplayer.
 * Includes a Chat Box on the right and space for the shared timer.
 */
public class MP_QView extends BorderPane {

    private TextArea chatLog;
    private TextField chatInput;
    private Label timerLabel;
    private VBox centerGameArea; 

    public MP_QView() {
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #000022;");

        // --- RIGHT SIDE: CHAT SYSTEM ---
        VBox chatContainer = new VBox(10);
        chatContainer.setPrefWidth(300);
        chatContainer.setPadding(new Insets(0, 0, 0, 20));

        Label chatTitle = new Label("PLAYER CHAT");
        chatTitle.setStyle("-fx-text-fill: #d4af37; -fx-font-weight: bold;");

        chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.setWrapText(true);
        chatLog.setPrefHeight(500);
        chatLog.getStyleClass().add("ladder-container"); // Reuse your CSS for the dark box look
        chatLog.setStyle("-fx-control-inner-background: rgba(0,0,0,0.6); -fx-text-fill: white;");

        chatInput = new TextField();
        chatInput.setPromptText("Type message and press Enter...");
        chatInput.getStyleClass().add("answer-btn"); // Reuse button styling for the input box

        chatContainer.getChildren().addAll(chatTitle, chatLog, chatInput);
        this.setRight(chatContainer);

        // --- BOTTOM: SHARED TIMER ---
        timerLabel = new Label("30");
        timerLabel.getStyleClass().add("timer-base");
        HBox bottomBox = new HBox(timerLabel);
        bottomBox.setAlignment(Pos.CENTER);
        this.setBottom(bottomBox);

        // --- CENTER: GAME AREA ---
        // For now, this is a placeholder where your QuestionBox and AnswerButtons go
        centerGameArea = new VBox(20);
        centerGameArea.setAlignment(Pos.CENTER);
        Label placeholder = new Label("Questions will appear here...");
        placeholder.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        centerGameArea.getChildren().add(placeholder);
        this.setCenter(centerGameArea);
    }

    public TextArea getChatLog() { return chatLog; }
    public TextField getChatInput() { return chatInput; }
    public void appendMessage(String msg) { chatLog.appendText(msg + "\n"); }
}