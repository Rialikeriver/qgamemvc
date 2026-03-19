package Pack_1.profile;

import java.util.List;

public interface UserStore {
    List<User> loadUsers();
    void saveUsers(List<User> users);
}
