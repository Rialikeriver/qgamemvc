package Pack_1;

import java.util.*;

public class QModel {
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    
    // Set to keep track of which questions have already been shuffled
    private Set<Integer> shuffledQuestions = new HashSet<>();

    public static final String[] LADDER_VALUES = {
        "$100", "$200", "$300", "$500", "$1,000", 
        "$2,000", "$4,000", "$8,000", "$16,000", "$32,000", 
        "$64,000", "$125,000", "$250,000", "$500,000", "$1,000,000"
    };

    // Numeric prize values aligned with LADDER_VALUES
    private static final int[] PRIZE_VALUES = {
        100, 200, 300, 500, 1000,
        2000, 4000, 8000, 16000, 32000,
        64000, 125000, 250000, 500000, 1000000
    };

 // Game state
    private boolean gameOver = false;			
    private boolean playerWon = false;
    private int moneyEarned = 0;
    private int guaranteedMoney = 0;

    // Lifeline state
    private boolean superpositionUsed = false;
    private boolean entanglementUsed = false;
    // private boolean thirdLifeLife = false;

    // Entanglement
    private Answer entangledAnswer = null;
    
    public QModel(List<Question> questions) {
        this.questions = questions;
    }

    public void setQuestions(List<Question> newQuestions) {
        if (newQuestions.size() > 15) {			// HEAVY BUG WORKAROUND. BETTER SOL NEEDED
            this.questions = newQuestions.subList(0, 15);
        } else {
            this.questions = newQuestions;
        }
        this.shuffledQuestions.clear(); // Clear shuffle history for new language
    }

    // ADDED: This getter fixes the "undefined" error in QController
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public Question getCurrentQuestion() {
        if (questions != null && currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            
            if (!shuffledQuestions.contains(currentQuestionIndex)) {
                List<Answer> answers = q.getAnswers();
                
                // 1. Shuffle the physical order
                Collections.shuffle(answers); 
                
                // 2. Re-assign labels so the first item in the list is now "A", etc.
                String[] labels = {"A", "B", "C", "D"};
                for (int i = 0; i < answers.size(); i++) {
                    answers.get(i).setLabel(labels[i]);
                }
                
                shuffledQuestions.add(currentQuestionIndex);
            }
            
            return q;
        }
        return null;
    }

    public void nextQuestion() {
        if (gameOver) return;

        // Check win BEFORE incrementing
        updateMoneyAndCheckWin();
        if (gameOver) return;

        // Now increment
        currentQuestionIndex++;

        // Prevent crash
        if (currentQuestionIndex >= PRIZE_VALUES.length) {
            currentQuestionIndex = PRIZE_VALUES.length - 1;
        }
    }

    
    public int getCurrentCashMoney() {
        int[] ladder = {
            100, 200, 300, 500, 1000,
            2000, 4000, 8000, 16000, 32000,
            64000, 125000, 250000, 500000, 1000000
        };
        int tier = getCurrentQuestionIndex();
        if (tier < 0 || tier >= ladder.length) return 0;
        return ladder[tier];
    }
    
    private void updateMoneyAndCheckWin() {
        if (currentQuestionIndex < 0)
            return;
        
        moneyEarned = PRIZE_VALUES[currentQuestionIndex];

        if (currentQuestionIndex == 4 || currentQuestionIndex == 9) {
            guaranteedMoney = moneyEarned;
        }
        
        if (currentQuestionIndex == 14) {
            gameOver = true;
            playerWon = true;
        }
        
    }

    public void handleWrongAnswer() {		// Gives the guaranteed money instead of highest.
        moneyEarned = guaranteedMoney;
        gameOver = true;
        playerWon = false;
    }

    public void resetGame() {				// Resets game values.
        this.currentQuestionIndex = 0;
        this.shuffledQuestions.clear();
        this.gameOver = false;
        this.playerWon = false;
        this.moneyEarned = 0;
        this.guaranteedMoney = 0;
        this.superpositionUsed = false;
        this.entanglementUsed = false;
        this.entangledAnswer = null;
    }

    public List<Answer> applySuperposition() {
        Question q = getCurrentQuestion();
        if (q == null) return null;

        List<Answer> wrongOnes = q.getWrongAnswers();
        Collections.shuffle(wrongOnes);
        
        int numToCollapse = new Random().nextInt(3) + 1; 
        return wrongOnes.subList(0, Math.min(numToCollapse, wrongOnes.size()));
    }

    // Ria. Entangled answer declaration moved uptop. 
    
    public void applyEntanglement() {
        Question q = getCurrentQuestion();
        if (q == null) return;
        List<Answer> wrongOnes = q.getWrongAnswers();
        entangledAnswer = wrongOnes.get(new Random().nextInt(wrongOnes.size()));
    }

    public boolean isEntangled(Answer selected) {
        return selected != null && selected.equals(entangledAnswer);
    }
    
    public Answer applyInterference() {
        Question q = getCurrentQuestion();
        if (q == null) return null;

        double roll = new Random().nextDouble(); 
        if (roll < 0.50) {
            return q.getCorrectAnswer();
        } else {
            List<Answer> wrongOnes = q.getWrongAnswers();
            return wrongOnes.get(new Random().nextInt(wrongOnes.size()));
        }
    }
    
    public int getTotalQuestions() {
        return (questions != null) ? questions.size() : 0;
    }
    
    // --- New game state getters ---
    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerWon() { return playerWon; }
    public int getMoneyEarned() { return moneyEarned; }
    public int getGuaranteedMoney() { return guaranteedMoney; }

    // --- Lifeline state ---
    public boolean isSuperpositionUsed() { return superpositionUsed; }
    public void setSuperpositionUsed(boolean used) { superpositionUsed = used; }

    public boolean isEntanglementUsed() { return entanglementUsed; }
    public void setEntanglementUsed(boolean used) { entanglementUsed = used; }

    public int getLifelinesUsed() {
        int used = 0;
        if (superpositionUsed) used++;
        if (entanglementUsed) used++;
        return used;
    }
}