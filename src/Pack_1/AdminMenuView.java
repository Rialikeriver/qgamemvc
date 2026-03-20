package Pack_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * JavaFX view for the administrator main menu. This screen provides access
 * to question management, user management, and navigation back to the main
 * application flow. The layout uses a centered vertical button group and a
 * styled title banner at the top.
 *
 * <p>Buttons are styled using the shared {@code answer-btn} CSS class so they
 * visually match the rest of the application's UI theme.</p>
 */
public class AdminMenuView extends BorderPane {

    // UI controls
    private Button manageQuestionsBtn;
    private Button manageUsersBtn;
    private Button backBtn;

    /**
     * Builds the admin menu layout, including title styling and the
     * vertically stacked action buttons.
     */
    public AdminMenuView() {
        setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        setPadding(new Insets(20));

        Label title = new Label("ADMIN PANEL");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        manageQuestionsBtn = new Button("Manage Questions");
        manageUsersBtn = new Button("Manage Users");
        backBtn = new Button("Back");

        manageQuestionsBtn.getStyleClass().add("answer-btn");
        manageUsersBtn.getStyleClass().add("answer-btn");
        backBtn.getStyleClass().add("answer-btn");

        VBox box = new VBox(20, manageQuestionsBtn, manageUsersBtn, backBtn);
        box.setAlignment(Pos.CENTER);
        setCenter(box);
    }

    // Accessors for controller wiring
    public Button getManageQuestionsBtn() { return manageQuestionsBtn; }
    public Button getManageUsersBtn() { return manageUsersBtn; }
    public Button getBackBtn() { return backBtn; }
}
