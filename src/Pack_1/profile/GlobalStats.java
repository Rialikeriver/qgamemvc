package Pack_1.profile;

public class GlobalStats {
    private int gamesWon;

    public GlobalStats() {}

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public void incrementGamesWon() { this.gamesWon++; }
}
