package Pack_1.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides JSON-based persistence for all user profiles.
 *
 * <p>This implementation of {@link UserStore} manages a list of {@link User}
 * objects stored in a single {@code users.json} file under the
 * {@code Database/} directory. It uses Jackson for serialization and
 * deserialization, including support for Java time types via
 * {@link JavaTimeModule}.</p>
 *
 * <p>The store is designed to be fault-tolerant: if the file does not exist or
 * cannot be parsed, an empty list is returned rather than throwing exceptions.
 * This ensures the application can always start with a valid user list even if
 * the underlying file is missing or corrupted.</p>
 */
public class JsonUserStore implements UserStore {

    /**
     * Shared Jackson mapper configured with Java time support.
     */
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * The file where user profiles are persisted.
     */
    private final File file;

    /**
     * Creates a new JSON-backed user store using the default
     * {@code Database/users.json} file path.
     */
    public JsonUserStore() {
        this.file = new File("Database/users.json");
    }

    /**
     * Loads all user profiles from disk.
     *
     * <p>If the file does not exist or an error occurs during deserialization,
     * an empty list is returned. This method never propagates exceptions to
     * callers.</p>
     *
     * @return a list of loaded users, or an empty list if loading fails
     */
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

    /**
     * Saves the given list of users to disk in JSON format.
     *
     * <p>The parent directory is created automatically if it does not exist.
     * Errors during saving are logged but not propagated, ensuring that
     * persistence failures do not interrupt application flow.</p>
     *
     * @param users the list of users to persist
     */
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
