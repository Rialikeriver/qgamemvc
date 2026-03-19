package Pack_1;

import Pack_1.profile.User;
import Pack_1.profile.UserManager;
import javafx.collections.FXCollections;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ProfileController {

    private final UserManager userManager;

    public interface UserCallback {
        void handle(User user);
    }

    public ProfileController(UserManager userManager) {
        this.userManager = userManager;
    }

    // -------------------------
    // NEW PROFILE LOGIC
    // -------------------------
    public void wireNewProfileScreen(NewProfileView view,
                                     UserCallback onCreated,
                                     Runnable onCancel) {

        view.getCreateBtn().setOnAction(e -> {
            String username = view.getUsernameField().getText().trim();
            String password = view.getPasswordField().getText();

            if (username.isEmpty()) {
                view.getErrorLabel().setText("Username is required.");
                return;
            }

            if (userManager.findUser(username).isPresent()) {
                view.getErrorLabel().setText("Username already exists.");
                return;
            }

            String hash = password.isEmpty() ? null : hashPassword(password);
            User user = userManager.createUser(username, hash);
            onCreated.handle(user);
        });

        view.getCancelBtn().setOnAction(e -> onCancel.run());
    }

    // -------------------------
    // LOAD / DELETE PROFILE LOGIC
    // -------------------------
    public void wireProfileSelectionScreen(ProfileSelectionView view,
                                           UserCallback onSelected,
                                           Runnable onBack,
                                           Runnable onNewProfile) {

        view.getProfileListView().setItems(
                FXCollections.observableArrayList(userManager.getUsers())
        );

        view.getLoadBtn().setOnAction(e -> {
            User selected = view.getProfileListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                onSelected.handle(selected);
            }
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

        view.getNewProfileBtn().setOnAction(e -> onNewProfile.run());
        view.getBackBtn().setOnAction(e -> onBack.run());
    }

    // -------------------------
    // PASSWORD HASHING
    // -------------------------
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
