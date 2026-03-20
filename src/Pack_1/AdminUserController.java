package Pack_1;

import Pack_1.profile.User;
import Pack_1.profile.UserManager;
import javafx.collections.FXCollections;

import java.util.function.Consumer;

/**
 * Controller for the admin-facing user management screen. This class wires
 * a {@link ProfileSelectionView} into admin behaviors such as editing,
 * deleting, and creating users. It adapts the standard profile selection UI
 * for administrative workflows by repurposing button labels and actions.
 *
 * <p>The controller does not perform business logic itself; all persistence
 * and mutation operations are delegated to the associated {@link UserManager}.
 * Its responsibility is strictly UI wiring and event handling.</p>
 */
public class AdminUserController {

    // Backend manager for all user operations
    private final UserManager userManager;

    /**
     * Creates a controller bound to the given user manager.
     */
    public AdminUserController(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Wires the admin user list view with edit, delete, create, and back actions.
     *
     * <p>This method adapts the standard {@link ProfileSelectionView} for admin
     * usage by:</p>
     *
     * <ul>
     *   <li>loading all users into the list</li>
     *   <li>renaming the "Load" button to "Edit User"</li>
     *   <li>binding edit/delete/new/back actions to the provided callbacks</li>
     *   <li>refreshing the list after deletions</li>
     * </ul>
     *
     * <p>Existing inline comments are preserved because they clarify intentional
     * UI behavior changes in admin mode.</p>
     *
     * @param view the profile selection UI to wire
     * @param onEditUser callback invoked when an admin chooses to edit a user
     * @param onBack callback invoked when returning to the previous screen
     * @param onNewUser callback invoked when creating a new user
     */
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
