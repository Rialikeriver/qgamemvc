package Pack_1.profile;

/**
 * Defines the contract for loading and saving global gameplay statistics.
 *
 * <p>Implementations of this interface provide persistence mechanisms for
 * {@link GlobalStats}, allowing the application to store cumulative data such
 * as total wins across all sessions. Different backends (e.g., JSON files,
 * databases, in‑memory stores) can be supplied by implementing this interface.</p>
 *
 * <p>The interface is intentionally minimal: it exposes only the operations
 * required by higher‑level components such as {@code UserManager} or game
 * controllers that need to read or update global statistics.</p>
 */
public interface StatsStore {

    /**
     * Loads global statistics from the underlying storage mechanism.
     *
     * <p>Implementations should guarantee that a non-null {@link GlobalStats}
     * instance is always returned, even if the underlying data source is missing
     * or corrupted.</p>
     *
     * @return the loaded global statistics
     */
    GlobalStats loadStats();

    /**
     * Persists the given global statistics to the underlying storage mechanism.
     *
     * <p>Implementations should ensure that the data is written atomically or
     * safely, and should avoid throwing exceptions to callers whenever possible.</p>
     *
     * @param stats the statistics object to save
     */
    void saveStats(GlobalStats stats);
}
