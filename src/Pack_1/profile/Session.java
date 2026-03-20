package Pack_1.profile;

/**
 * Represents the active application session and tracks which user is currently logged in.
 *
 * <p>This class acts as a lightweight session container used by controllers and
 * views that need to reference the active {@link User}. It does not manage
 * authentication, persistence, or lifecycle events—its sole responsibility is
 * holding and exposing the current user for the duration of a session.</p>
 *
 * <p>A session is considered active when a non-null user is assigned. This is
 * typically set when a player selects a profile and cleared when returning to
 * the main menu or switching users.</p>
 */
public class Session {

    /**
     * The user currently associated with this session, or {@code null} if no user
     * is logged in.
     */
    private User currentUser;

    /**
     * Returns the user currently associated with this session.
     *
     * @return the active user, or {@code null} if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the active user for this session.
     *
     * <p>Assigning {@code null} effectively clears the session.</p>
     *
     * @param currentUser the user to associate with this session
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Indicates whether a user is currently logged in.
     *
     * @return {@code true} if a user is assigned, {@code false} otherwise
     */
    public boolean hasUser() {
        return currentUser != null;
    }
}
