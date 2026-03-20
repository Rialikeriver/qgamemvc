package Pack_1.profile;

import java.util.List;

/**
 * Defines the persistence contract for storing and retrieving {@link User}
 * profiles. Implementations may use JSON, databases, or other backends, but
 * must always return a non-null list and persist all provided users.
 *
 * <p>This interface is intentionally minimal: higher-level components such as
 * {@code UserManager} handle all business logic, while the store focuses solely
 * on reading and writing user data.</p>
 */
public interface UserStore {

    /**
     * Loads all user profiles from the underlying storage system.
     *
     * @return a non-null list of users (empty if none exist or loading fails)
     */
    List<User> loadUsers();

    /**
     * Persists the given list of users to the underlying storage system.
     *
     * @param users the list of users to save
     */
    void saveUsers(List<User> users);
}
