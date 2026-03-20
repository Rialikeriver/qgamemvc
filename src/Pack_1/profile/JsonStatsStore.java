package Pack_1.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;

/**
 * Provides JSON-based persistence for global profile statistics.
 *
 * <p>This implementation of {@link StatsStore} reads and writes a single
 * {@code stats.json} file located under the {@code Database/} directory.
 * It uses Jackson for serialization and deserialization, including support
 * for Java time types via {@link JavaTimeModule}.</p>
 *
 * <p>The store is intentionally resilient: if the file does not exist or
 * cannot be parsed, a new {@link GlobalStats} instance is returned rather
 * than propagating exceptions. This ensures the game can always start with
 * valid statistics even in the presence of corrupted or missing data.</p>
 */
public class JsonStatsStore implements StatsStore {

    /**
     * Shared Jackson mapper configured with Java time support.
     */
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * The file where global statistics are persisted.
     */
    private final File file;

    /**
     * Creates a new JSON-backed statistics store using the default
     * {@code Database/stats.json} file path.
     */
    public JsonStatsStore() {
        this.file = new File("Database/stats.json");
    }

    /**
     * Loads global statistics from disk.
     *
     * <p>If the file does not exist, or if an error occurs during
     * deserialization, a new {@link GlobalStats} instance is returned.
     * This method never throws exceptions to callers.</p>
     *
     * @return the loaded statistics, or a new empty instance if loading fails
     */
    @Override
    public GlobalStats loadStats() {
        try {
            if (!file.exists()) return new GlobalStats();
            return mapper.readValue(file, GlobalStats.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new GlobalStats();
        }
    }

    /**
     * Saves the given statistics object to disk in JSON format.
     *
     * <p>The parent directory is created automatically if it does not exist.
     * Errors during saving are logged but not propagated, ensuring that
     * persistence failures do not interrupt gameplay.</p>
     *
     * @param stats the statistics object to persist
     */
    @Override
    public void saveStats(GlobalStats stats) {
        try {
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, stats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
