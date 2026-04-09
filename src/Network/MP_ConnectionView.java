package Network;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * The initial dialogue for Network Play.
 * Allows user to choose between Hosting a game or Joining one.
 */
public class MP_ConnectionView extends VBox {

    private TextField ipField;
    private TextField portField;
    private Button hostBtn;
    private Button joinBtn;
    private Button backBtn;
    private TextArea statusArea;
    
    public MP_ConnectionView() {
        this.setSpacing(20);
        this.setPadding(new Insets(40));
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");

        Label title = new Label("NETWORK SETUP");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");

        // Input Grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        Label ipLabel = new Label("Server IP:");
        ipLabel.setStyle("-fx-text-fill: white;");
        ipField = new TextField("127.0.0.1"); // Default to localhost
        
        Label portLabel = new Label("Port:");
        portLabel.setStyle("-fx-text-fill: white;");
        portField = new TextField("5555");

        grid.add(ipLabel, 0, 0);
        grid.add(ipField, 1, 0);
        grid.add(portLabel, 0, 1);
        grid.add(portField, 1, 1);

        // Buttons
        hostBtn = new Button("HOST GAME");
        joinBtn = new Button("JOIN GAME");
        backBtn = new Button("BACK TO MAIN MENU");

        // Apply your existing CSS style
        hostBtn.getStyleClass().add("answer-btn");
        joinBtn.getStyleClass().add("answer-btn");
        backBtn.getStyleClass().add("answer-btn");
        
        hostBtn.setPrefWidth(200);
        joinBtn.setPrefWidth(200);
        backBtn.setPrefWidth(200);

        // Status log (to see connection progress)
        statusArea = new TextArea("Ready...");
        statusArea.setEditable(false);
        statusArea.setPrefHeight(100);
        statusArea.setMaxWidth(400);
        statusArea.setStyle("-fx-control-inner-background: #000011; -fx-text-fill: #00ff00;");

        this.getChildren().addAll(title, grid, hostBtn, joinBtn, backBtn, statusArea);
    }

    // Getters for the Controller
    public String getIp() { return ipField.getText(); }
    public int getPort() { 
        try {
            return Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            return 5555; 
        }
    }

    public void log(String message) {
        statusArea.appendText("\n" + message);
    }

    public Button getHostBtn() { return hostBtn; }
    public Button getJoinBtn() { return joinBtn; }
    public Button getBackBtn() { return backBtn; }
}