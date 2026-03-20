package Pack_1;

import Pack_1.profile.User;
import Pack_1.profile.UserManager;
import javafx.collections.FXCollections;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Controller responsible for all profile‑related workflows, including creating
 * new profiles, selecting existing profiles, deleting profiles, and hashing
 * passwords. This class wires UI views to the underlying {@link UserManager}
 * and exposes callbacks so higher‑level controllers can react to profile
 * selection or creation.
 *
 * <p>The controller does not store UI state; it simply connects views to
 * business logic and ensures that profile lists refresh after mutations.</p>
 */
public class ProfileController {

    // Backend manager for all user operations
    private final UserManager userManager;

    /**
     * Callback interface used when a profile is created or selected.
     */
    public interface UserCallback {
        void handle(User user);
    }

    /**
     * Creates a controller bound to the given user manager.
     */
    public ProfileController(UserManager userManager) {
        this.userManager = userManager;
    }

    // -------------------------
    // NEW PROFILE LOGIC
    // -------------------------

    /**
     * Wires the new‑profile creation screen with validation, creation logic,
     * and navigation callbacks. This includes:
     * <ul>
     *   <li>validating username presence</li>
     *   <li>checking for duplicates</li>
     *   <li>hashing the password if provided</li>
     *   <li>creating the user through {@link UserManager}</li>
     *   <li>invoking the provided callback on success</li>
     * </ul>
     */
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

    /**
     * Wires the profile selection screen with load, delete, new‑profile, and
     * back actions. The list is refreshed after deletions to ensure the UI
     * reflects the current state of the user store.
     */
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

    /**
     * Hashes a password using SHA‑256 and returns the hex‑encoded string.
     * This is used only for optional password protection on profiles.
     */
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
