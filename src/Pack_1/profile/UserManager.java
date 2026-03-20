package Pack_1.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Pack_1.QModel;

/**
 * Coordinates all user‑related operations including creation, lookup,
 * progression updates, and persistence. Also manages global statistics
 * through the associated {@link StatsStore}.
 *
 * <p>This class acts as the central profile subsystem controller. It loads
 * all users and global stats on construction, exposes lookup and mutation
 * operations, and ensures that all changes are persisted through the
 * configured {@link UserStore} and {@link StatsStore} implementations.</p>
 *
 * <p>All methods that modify user or stats data call the appropriate save
 * routines to keep the persistent state synchronized with in‑memory state.</p>
 */
public class UserManager {

    // Persistence backends
    private final UserStore userStore;
    private final StatsStore statsStore;

    // In‑memory state
    private final List<User> users;
    private final GlobalStats stats;

    /**
     * Loads all users and global statistics from the provided stores.
     */
    public UserManager(UserStore userStore, StatsStore statsStore) {
        this.userStore = userStore;
        this.statsStore = statsStore;
        this.users = new ArrayList<>(userStore.loadUsers());
        this.stats = statsStore.loadStats();
    }

    /**
     * Returns a defensive copy of all known users.
     */
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Finds a user by username (case-insensitive).
     */
    public Optional<User> findUser(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Creates and persists a new user.
     */
    public User createUser(String username, String passwordHash) {
        User user = new User(username, passwordHash);
        users.add(user);
        saveUsers();
        return user;
    }

    /**
     * Deletes a user and persists the updated list.
     */
    public void deleteUser(User user) {
        users.remove(user);
        saveUsers();
    }

    /**
     * Updates mid‑game progression fields for the given user.
     *
     * <p>This method updates:</p>
     * <ul>
     *   <li>current tier</li>
     *   <li>lifeline usage (session)</li>
     *   <li>last played timestamp</li>
     *   <li>current game money</li>
     *   <li>highest tier reached (dynamic)</li>
     * </ul>
     *
     * <p>Existing inline comments are preserved because they clarify
     * intentional mid‑game update behavior.</p>
     */
    public void updateUserProgress(
            User user,
            int currentTier,
            boolean superUsed,
            boolean entUsed,
            boolean interfUsed,
            int currentGameMoney) {

        // Always update these mid‑game
        user.setCurrentTier(currentTier);
        user.setSuperpositionUsed(superUsed);
        user.setEntanglementUsed(entUsed);
        user.setInterferenceUsed(interfUsed);
        user.setLastPlayed(LocalDateTime.now());

        // Running lifeline count
        int count = 0;
        if (superUsed) count++;
        if (entUsed) count++;
        if (interfUsed) count++;
        user.setLifelinesUsed(count);

        // Dynamic money
        user.setLastGameMoney(currentGameMoney);

        // Dynamic highest tier
        if (currentTier > user.getHighestTierReached()) {
            user.setHighestTierReached(currentTier);
        }

        saveUsers();
    }

    /**
     * Returns global cumulative statistics.
     */
    public GlobalStats getStats() {
        return stats;
    }

    /**
     * Increments global win count and persists the change.
     */
    public void incrementGamesWon() {
        stats.incrementGamesWon();
        saveStats();
    }

    /**
     * Persists all users through the configured store.
     */
    private void saveUsers() {
        userStore.saveUsers(users);
    }

    /**
     * Public wrapper for saving all users.
     */
    public void saveAllUsers() {
        saveUsers();
    }

    /**
     * Persists global statistics.
     */
    private void saveStats() {
        statsStore.saveStats(stats);
    }

    /**
     * Records the final result of a completed game, updating lifetime
     * statistics, money, tier history, lifeline usage, and win/loss counts.
     *
     * <p>Existing inline comments are preserved because they clarify which
     * fields are updated only at end‑of‑game versus mid‑game.</p>
     */
    public void recordGameResult(User user, QModel model) {
        if (user == null || model == null) return;

        user.setLastPlayed(LocalDateTime.now());

        int finalTier = model.getCurrentQuestionIndex();
        int finalMoney = Math.max(model.getLastEarnedMoney(), model.getGuaranteedMoney());

        user.setCurrentTier(0);
        user.setLastGameMoney(0);

        // Lifetime money only updated here
        user.setTotalMoneyEarned(user.getTotalMoneyEarned() + finalMoney);

        // Highest tier reached
        user.setHighestTierReached(Math.max(user.getHighestTierReached(), finalTier));

        // Lifelines used this game (derived from booleans)
        int used = 0;
        if (model.isSuperpositionUsed()) used++;
        if (model.isEntanglementUsed()) used++;
        if (model.isInterferenceUsed()) used++;

        // Lifetime lifelines only updated here
        user.setTotalLifelinesUsed(user.getTotalLifelinesUsed() + used);

        // Wins/losses only updated here
        if (model.isPlayerWon()) {
            user.setGamesWon(user.getGamesWon() + 1);
        } else {
            user.setGamesLost(user.getGamesLost() + 1);
        }

        // Reset lifelines for next run
        user.setSuperpositionUsed(false);
        user.setEntanglementUsed(false);
        user.setInterferenceUsed(false);
        user.setLifelinesUsed(0);

        saveUsers();
    }

    /**
     * Resets mid‑game fields after a game ends without updating lifetime
     * statistics. Used when the game is aborted or exited early.
     *
     * <p>Inline comments preserved because they clarify intentional reset
     * behavior.</p>
     */
    public void finalizeGame(User user, QModel model) {
        if (user == null || model == null) return;

        // Reset mid-game fields ONLY
        user.setCurrentTier(0);
        user.setLastGameMoney(0);

        user.setSuperpositionUsed(false);
        user.setEntanglementUsed(false);
        user.setInterferenceUsed(false);
        user.setLifelinesUsed(0);

        user.setLastPlayed(LocalDateTime.now());
        saveUsers();
    }
}
