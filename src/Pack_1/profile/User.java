package Pack_1.profile;

import java.time.LocalDateTime;

/**
 * Represents a persistent user profile containing identity, progression,
 * money statistics, win/loss records, and lifeline usage. This model is
 * mutable and designed for JSON serialization through {@link UserStore}
 * implementations.
 *
 * <p>Fields are grouped by purpose for clarity. Getters and setters are
 * intentionally undocumented unless behavior differs from simple access.</p>
 */
public class User {

    // Identity
    private String username;
    private String passwordHash;

    // Progression
    private int currentTier;
    private int highestTierReached;
    private LocalDateTime lastPlayed;

    // Money statistics
    private int totalMoneyEarned;
    private int lastGameMoney;

    // Win/loss record
    private int gamesWon;
    private int gamesLost;

    // Lifeline usage (session + total)
    private int lifelinesUsed;
    private boolean superpositionUsed;
    private boolean entanglementUsed;
    private boolean interferenceUsed;
    private int totalLifelinesUsed;

    /** Default constructor for deserialization. */
    public User() {}

    /**
     * Creates a new user with the given identity fields.
     * New users start at tier 0 with no lifelines used.
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.currentTier = 0;
        this.lifelinesUsed = 0;
        this.lastPlayed = LocalDateTime.now();
    }

    // Getters
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public int getCurrentTier() { return currentTier; }
    public int getHighestTierReached() { return highestTierReached; }
    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public int getTotalMoneyEarned() { return totalMoneyEarned; }
    public int getLastGameMoney() { return lastGameMoney; }
    public int getGamesWon() { return gamesWon; }
    public int getGamesLost() { return gamesLost; }
    public int getLifelinesUsed() { return lifelinesUsed; }
    public boolean isSuperpositionUsed() { return superpositionUsed; }
    public boolean isEntanglementUsed() { return entanglementUsed; }
    public boolean isInterferenceUsed() { return interferenceUsed; }
    public int getTotalLifelinesUsed() { return totalLifelinesUsed; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCurrentTier(int currentTier) { this.currentTier = currentTier; }
    public void setHighestTierReached(int highestTierReached) { this.highestTierReached = highestTierReached; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }
    public void setTotalMoneyEarned(int totalMoneyEarned) { this.totalMoneyEarned = totalMoneyEarned; }
    public void setLastGameMoney(int lastGameMoney) { this.lastGameMoney = lastGameMoney; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }
    public void setLifelinesUsed(int lifelinesUsed) { this.lifelinesUsed = lifelinesUsed; }
    public void setSuperpositionUsed(boolean used) { this.superpositionUsed = used; }
    public void setEntanglementUsed(boolean used) { this.entanglementUsed = used; }
    public void setInterferenceUsed(boolean used) { this.interferenceUsed = used; }
    public void setTotalLifelinesUsed(int totalLifelinesUsed) { this.totalLifelinesUsed = totalLifelinesUsed; }
}
