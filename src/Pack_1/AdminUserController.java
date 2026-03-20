package Pack_1;

import Pack_1.profile.User;
import Pack_1.profile.UserManager;
import javafx.collections.FXCollections;

import java.util.function.Consumer;

public class AdminUserController {

    private final UserManager userManager;

    public AdminUserController(UserManager userManager) {
        this.userManager = userManager;
    }

    public void wireUserList(ProfileSelectionView view,
                             Consumer<User> onEditUser,
                             Runnable onBack,
                             Runnable onNewUser) {

        view.getProfileListView().setItems(
                FXCollections.observableArrayList(userManager.getUsers())
        );

        // Rename buttons for clarity in admin mode (optional)
        view.getLoadBtn().setText("Edit User");

        view.getLoadBtn().setOnAction(e -> {
            User selected = view.getProfileListView().getSelectionModel().getSelectedItem();
            if (selected != null) onEditUser.accept(selected);
        });

        view.getDeleteBtn().setOnAction(e -> {
            User selected = view.getProfileListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                userManager.deleteUser(selected);
                view.getProfileListView().setItems(
                        FXCollections.observableArrayList(userManager.getUsers())
                );
            }
        });

        view.getNewProfileBtn().setOnAction(e -> onNewUser.run());
        view.getBackBtn().setOnAction(e -> onBack.run());
    }
}
