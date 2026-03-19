package Pack_1.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserManager {
    private final UserStore userStore;
    private final StatsStore statsStore;

    private final List<User> users;
    private final GlobalStats stats;

    public UserManager(UserStore userStore, StatsStore statsStore) {
        this.userStore = userStore;
        this.statsStore = statsStore;
        this.users = new ArrayList<>(userStore.loadUsers());
        this.stats = statsStore.loadStats();
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public Optional<User> findUser(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public User createUser(String username, String passwordHash) {
        User user = new User(username, passwordHash);
        users.add(user);
        saveUsers();
        return user;
    }

    public void deleteUser(User user) {
        users.remove(user);
        saveUsers();
    }

    public void updateUserProgress(User user,
    		int currentTier,
    		boolean superUsed,
    		boolean entUsed,
    		int currentGameMoney) {

    	// Always update these mid‑game
    	user.setCurrentTier(currentTier);
    	user.setSuperpositionUsed(superUsed);
    	user.setEntanglementUsed(entUsed);
    	user.setLastPlayed(LocalDateTime.now());

    	// Running lifeline count
    	int count = 0;
    	if (superUsed) count++;
    	if (entUsed) count++;
    	user.setLifelinesUsed(count);

    	// Dynamic money
    	user.setLastGameMoney(currentGameMoney);

    	// Dynamic highest tier
    	if (currentTier > user.getHighestTierReached()) {
    		user.setHighestTierReached(currentTier);
    	}

    	saveUsers();
    }



    public GlobalStats getStats() {
        return stats;
    }

    public void incrementGamesWon() {
        stats.incrementGamesWon();
        saveStats();
    }

    private void saveUsers() {
        userStore.saveUsers(users);
    }

    private void saveStats() {
        statsStore.saveStats(stats);
    }
    
    public void recordGameResult(User user, Pack_1.QModel model) {
        if (user == null || model == null) return;

        user.setLastPlayed(LocalDateTime.now());

        int finalTier = model.getCurrentQuestionIndex();
        int finalMoney = model.getMoneyEarned();

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
        user.setLifelinesUsed(0);

        saveUsers();
    }

}
