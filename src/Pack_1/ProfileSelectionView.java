package Pack_1;

import Pack_1.profile.User;
import Pack_1.profile.ProfileCell;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ProfileSelectionView extends BorderPane {
    private ListView<User> profileListView;
    private Button loadBtn;
    private Button deleteBtn;
    private Button newProfileBtn;
    private Button backBtn;

    public ProfileSelectionView() {
        setupUI();
    }

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

    public ListView<User> getProfileListView() { return profileListView; }
    public Button getLoadBtn() { return loadBtn; }
    public Button getDeleteBtn() { return deleteBtn; }
    public Button getNewProfileBtn() { return newProfileBtn; }
    public Button getBackBtn() { return backBtn; }
}
