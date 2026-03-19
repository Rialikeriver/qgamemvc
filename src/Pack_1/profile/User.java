package Pack_1.profile;

import java.time.LocalDateTime;

public class User {
    private String username;
    private String passwordHash;
    private int currentTier;
    private int lifelinesUsed;
    private LocalDateTime lastPlayed;
    private int totalMoneyEarned;
    private int lastGameMoney;
    private int gamesWon;
    private int gamesLost;
    private int highestTierReached;
    // Lifeline variables
    private boolean superpositionUsed;			
    private boolean entanglementUsed;
    private int totalLifelinesUsed;

    public User() {}

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.currentTier = 0;
        this.lifelinesUsed = 0;
        this.lastPlayed = LocalDateTime.now();
    }
    
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public int getCurrentTier() { return currentTier; }
    public int getLifelinesUsed() { return lifelinesUsed; }
    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public int getTotalMoneyEarned() { return totalMoneyEarned; }
    public int getLastGameMoney() { return lastGameMoney; }
    public int getGamesWon() { return gamesWon; }
    public int getGamesLost() { return gamesLost; }
    public int getHighestTierReached() { return highestTierReached; }
    public boolean isSuperpositionUsed() { return superpositionUsed; }
    public boolean isEntanglementUsed() { return entanglementUsed; }
    //public boolean isThirdLifeLineUsed() { return thirdLLUsed; }
    public int getTotalLifelinesUsed() { return totalLifelinesUsed; }

    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCurrentTier(int currentTier) { this.currentTier = currentTier; }
    public void setLifelinesUsed(int lifelinesUsed) { this.lifelinesUsed = lifelinesUsed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }
    public void setTotalMoneyEarned(int totalMoneyEarned) { this.totalMoneyEarned = totalMoneyEarned; }
    public void setLastGameMoney(int lastGameMoney) { this.lastGameMoney = lastGameMoney; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }
    public void setHighestTierReached(int highestTierReached) { this.highestTierReached = highestTierReached; }
    public void setSuperpositionUsed(boolean used) { this.superpositionUsed = used; }
    public void setEntanglementUsed(boolean used) { this.entanglementUsed = used; }
    //public void setThirdLifeLineUsed(boolean used) { this.thirdLLUsed = used; }
    public void setTotalLifelinesUsed(int totalLifelinesUsed) { this.totalLifelinesUsed = totalLifelinesUsed; }
}
