package Pack_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Player-facing main menu shown after selecting a profile. This screen provides
 * access to starting a new game, loading a saved game (if supported), quitting,
 * or returning to the previous screen. The layout uses a centered vertical
 * button stack styled consistently with the application's theme.
 *
 * <p>The controller layer attaches behavior to the exposed buttons, while this
 * view focuses solely on presentation and layout.</p>
 */
public class PlayerMenuView extends BorderPane {

    // Action buttons
    private Button newGameBtn;
    private Button loadGameBtn;
    private Button quitBtn;
    private Button backBtn;

    /**
     * Builds the player menu UI and initializes all controls.
     */
    public PlayerMenuView() {
        setupUI();
    }

    /**
     * Configures the layout, styling, and button arrangement for the player menu.
     * The menu includes New Game, Load Game, Quit, and Back actions.
     */
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

        newGameBtn.getStyleClass().add("answer-btn");
        loadGameBtn.getStyleClass().add("answer-btn");
        quitBtn.getStyleClass().add("answer-btn");
        backBtn.getStyleClass().add("answer-btn");

        VBox centerBox = new VBox(20, newGameBtn, loadGameBtn, quitBtn, backBtn);
        centerBox.setAlignment(Pos.CENTER);
        this.setCenter(centerBox);
    }

    // Accessors for controller wiring
    public Button getNewGameBtn() { return newGameBtn; }
    public Button getLoadGameBtn() { return loadGameBtn; }
    public Button getQuitBtn() { return quitBtn; }
    public Button getBackBtn() { return backBtn; }
}
