package Pack_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * View for creating a new player profile. This screen provides fields for
 * entering a username and an optional password, along with Create/Cancel
 * actions and an error label for validation feedback. The layout uses a
 * centered vertical form styled consistently with the rest of the UI theme.
 *
 * <p>The controller layer is responsible for validating input, displaying
 * errors through {@link #getErrorLabel()}, and invoking profile creation
 * through the exposed buttons.</p>
 */
public class NewProfileView extends BorderPane {

    // Form fields
    private TextField usernameField;
    private PasswordField passwordField;

    // Action buttons
    private Button createBtn;
    private Button cancelBtn;

    // Validation feedback
    private Label errorLabel;

    /**
     * Builds the new profile creation UI and initializes all form controls.
     */
    public NewProfileView() {
        setupUI();
    }

    /**
     * Configures the layout, styling, and form components for the profile
     * creation screen. The form includes username, optional password, an
     * error label, and Create/Cancel buttons.
     */
    private void setupUI() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        this.setPadding(new Insets(20));

        Label title = new Label("CREATE NEW PROFILE");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        title.setPadding(new Insets(20));
        this.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password (optional)");

        createBtn = new Button("Create");
        cancelBtn = new Button("Cancel");
        createBtn.getStyleClass().add("answer-btn");
        cancelBtn.getStyleClass().add("answer-btn");

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        HBox buttonBox = new HBox(15, createBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(15, usernameField, passwordField, errorLabel, buttonBox);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        this.setCenter(centerBox);
    }

    // Accessors for controller wiring
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public Button getCreateBtn() { return createBtn; }
    public Button getCancelBtn() { return cancelBtn; }
    public Label getErrorLabel() { return errorLabel; }
}
