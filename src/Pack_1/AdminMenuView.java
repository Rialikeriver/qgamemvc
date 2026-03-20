package Pack_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class AdminMenuView extends BorderPane {
    private Button manageQuestionsBtn;
    private Button manageUsersBtn;
    private Button backBtn;

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

    public Button getManageQuestionsBtn() { return manageQuestionsBtn; }
    public Button getManageUsersBtn() { return manageUsersBtn; }
    public Button getBackBtn() { return backBtn; }
}
