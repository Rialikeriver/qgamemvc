package Pack_1.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonUserStore implements UserStore {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final File file;

    public JsonUserStore() {
        this.file = new File("Database/users.json");
    }

    @Override
    public List<User> loadUsers() {
        try {
            if (!file.exists()) return new ArrayList<>();
            return mapper.readValue(file, new TypeReference<List<User>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void saveUsers(List<User> users) {
        try {
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
