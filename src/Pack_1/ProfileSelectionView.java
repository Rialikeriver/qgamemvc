package Pack_1;

import Pack_1.profile.User;
import Pack_1.profile.ProfileCell;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * View for selecting, loading, deleting, or creating player profiles. This
 * screen displays all existing profiles in a styled {@link ListView} and
 * provides action buttons for profile management. The layout uses a centered
 * vertical stack with a button row beneath the list.
 *
 * <p>The list uses {@link ProfileCell} to render each user entry with a
 * consistent visual style. Controllers attach behavior to the exposed
 * buttons to handle loading, deleting, creating, or navigating back.</p>
 */
public class ProfileSelectionView extends BorderPane {

    // Profile list and action buttons
    private ListView<User> profileListView;
    private Button loadBtn;
    private Button deleteBtn;
    private Button newProfileBtn;
    private Button backBtn;

    /**
     * Builds the profile selection UI and initializes all controls.
     */
    public ProfileSelectionView() {
        setupUI();
    }

    /**
     * Configures the layout, styling, and button arrangement for the profile
     * selection screen. The list is styled with a custom cell factory and
     * placed above a horizontal action bar.
     */
    private void setupUI() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        this.setPadding(new Insets(20));

        Label title = new Label("LOAD GAME / PROFILE");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        title.setPadding(new Insets(20));
        this.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        profileListView = new ListView<>();
        profileListView.setCellFactory(list -> new ProfileCell());
        profileListView.setPrefHeight(500);

        loadBtn = new Button("Load");
        deleteBtn = new Button("Delete");
        newProfileBtn = new Button("New Profile");
        backBtn = new Button("Back");

        loadBtn.getStyleClass().add("answer-btn");
        deleteBtn.getStyleClass().add("answer-btn");
        newProfileBtn.getStyleClass().add("answer-btn");
        backBtn.getStyleClass().add("answer-btn");

        HBox buttonBox = new HBox(15, backBtn, deleteBtn, newProfileBtn, loadBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        VBox centerBox = new VBox(15, profileListView, buttonBox);
        centerBox.setAlignment(Pos.CENTER);
        this.setCenter(centerBox);
    }

    // Accessors for controller wiring
    public ListView<User> getProfileListView() { return profileListView; }
    public Button getLoadBtn() { return loadBtn; }
    public Button getDeleteBtn() { return deleteBtn; }
    public Button getNewProfileBtn() { return newProfileBtn; }
    public Button getBackBtn() { return backBtn; }
}
