package Pack_1.profile;

public class Session {
    private User currentUser;

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public boolean hasUser() { return currentUser != null; }
}
