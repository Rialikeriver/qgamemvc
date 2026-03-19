package Pack_1.profile;

public interface StatsStore {
    GlobalStats loadStats();
    void saveStats(GlobalStats stats);
}
