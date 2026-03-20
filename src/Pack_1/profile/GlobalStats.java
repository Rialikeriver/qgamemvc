package Pack_1.profile;

/**
 * Tracks global, cross-session statistics for a user profile.
 * 
 * <p>This class currently maintains the total number of games won across all
 * sessions. It acts as a simple data container used by persistence layers
 * such as {@code JsonStatsStore} and higher-level managers like
 * {@code UserManager}.</p>
 *
 * <p>The class is intentionally minimal and mutable, allowing callers to
 * increment or overwrite the win count as part of profile updates.</p>
 */
public class GlobalStats {

    /** 
     * Total number of games the user has won across all sessions.
     */
    private int gamesWon;

    /**
     * Creates a new {@code GlobalStats} instance with all counters initialized
     * to zero.
     */
    public GlobalStats() {}

    /**
     * Returns the total number of games won.
     *
     * @return the number of games won
     */
    public int getGamesWon() { 
        return gamesWon; 
    }

    /**
     * Sets the total number of games won.
     *
     * <p>This method overwrites the existing value and should typically be used
     * only by persistence or synchronization logic.</p>
     *
     * @param gamesWon the new total number of games won
     */
    public void setGamesWon(int gamesWon) { 
        this.gamesWon = gamesWon; 
    }

    /**
     * Increments the total number of games won by one.
     *
     * <p>This is the primary method used by game logic to update global
     * statistics after a win event.</p>
     */
    public void incrementGamesWon() { 
        this.gamesWon++; 
    }
}
