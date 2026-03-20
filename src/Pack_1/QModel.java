package Pack_1;

import java.util.*;

/**
 * Core game-state model for Quantum Millionaire. This class tracks the active
 * question list, current tier, prize progression, lifeline usage, guaranteed
 * thresholds, and win/loss conditions. It contains all deterministic game
 * logic, while controllers and views handle UI and interaction.
 *
 * <p>The model exposes methods for advancing questions, applying lifelines,
 * computing earnings, and resetting the game. It also manages shuffling of
 * answers per question to ensure randomized label assignments.</p>
 */
public class QModel {

    private List<Question> questions;
    private int currentQuestionIndex = 0;

    // Tracks which questions have had their answers shuffled
    private Set<Integer> shuffledQuestions = new HashSet<>();

    /** Display ladder values for UI. */
    public static final String[] LADDER_VALUES = {
        "$100", "$200", "$300", "$500", "$1,000",
        "$2,000", "$4,000", "$8,000", "$16,000", "$32,000",
        "$64,000", "$125,000", "$250,000", "$500,000", "$1,000,000"
    };

    /** Internal numeric prize values for logic. */
    private static final int[] PRIZE_VALUES = {
        100, 200, 300, 500, 1000,
        2000, 4000, 8000, 16000, 32000,
        64000, 125000, 250000, 500000, 1000000
    };

    private boolean gameOver = false;
    private boolean playerWon = false;
    private int moneyEarned = 0;
    private int lastEarnedMoney = 0;
    private int guaranteedMoney = 0;

    // Lifeline state
    private boolean superpositionUsed = false;
    private boolean entanglementUsed = false;
    private boolean interferenceUsed = false;

    private Answer entangledAnswer = null;

    /**
     * Creates a new model with the given question list.
     */
    public QModel(List<Question> questions) {
        this.questions = questions;
    }

    /**
     * Replaces the question set, trimming to 15 if necessary, and clears shuffle state.
     */
    public void setQuestions(List<Question> newQuestions) {
        if (newQuestions.size() > 15) {
            this.questions = newQuestions.subList(0, 15);
        } else {
            this.questions = newQuestions;
        }
        this.shuffledQuestions.clear();
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    /**
     * Returns the current question, shuffling its answers on first access.
     */
    public Question getCurrentQuestion() {
        if (questions != null && currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);

            if (!shuffledQuestions.contains(currentQuestionIndex)) {
                List<Answer> answers = q.getAnswers();
                Collections.shuffle(answers);

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

    /**
     * Advances to the next question, awarding money for the current tier and
     * checking for guaranteed thresholds or win conditions.
     */
    public void nextQuestion() {
        if (gameOver) return;

        lastEarnedMoney = PRIZE_VALUES[currentQuestionIndex];
        updateMoneyAndCheckWin();
        if (gameOver) return;

        currentQuestionIndex++;
        if (currentQuestionIndex >= PRIZE_VALUES.length) {
            currentQuestionIndex = PRIZE_VALUES.length - 1;
        }
    }

    public int getCurrentCashMoney() {
        int tier = getCurrentQuestionIndex();
        if (tier < 0 || tier >= PRIZE_VALUES.length) return 0;
        return PRIZE_VALUES[tier];
    }

    /**
     * Updates earned money, guaranteed thresholds, and win state.
     */
    private void updateMoneyAndCheckWin() {
        if (currentQuestionIndex < 0) return;

        moneyEarned = PRIZE_VALUES[currentQuestionIndex];

        if (currentQuestionIndex == 4 || currentQuestionIndex == 9) {
            guaranteedMoney = moneyEarned;
        }

        if (currentQuestionIndex == 14) {
            gameOver = true;
            playerWon = true;
        }
    }

    /**
     * Applies failure logic: player receives only guaranteed money.
     */
    public void handleWrongAnswer() {
        moneyEarned = guaranteedMoney;
        gameOver = true;
        playerWon = false;
    }

    /**
     * Resets all game state, including lifelines and shuffle tracking.
     */
    public void resetGame() {
        this.currentQuestionIndex = 0;
        this.shuffledQuestions.clear();
        this.gameOver = false;
        this.playerWon = false;
        this.moneyEarned = 0;
        this.lastEarnedMoney = 0;
        this.guaranteedMoney = 0;
        this.superpositionUsed = false;
        this.entanglementUsed = false;
        this.interferenceUsed = false;
        this.entangledAnswer = null;
    }

    /**
     * Applies the Superposition lifeline by collapsing 1–3 random wrong answers.
     */
    public List<Answer> applySuperposition() {
        Question q = getCurrentQuestion();
        if (q == null) return null;

        List<Answer> wrongOnes = q.getWrongAnswers();
        Collections.shuffle(wrongOnes);

        int numToCollapse = new Random().nextInt(3) + 1;
        return wrongOnes.subList(0, Math.min(numToCollapse, wrongOnes.size()));
    }

    /**
     * Applies the Entanglement lifeline by marking one wrong answer as linked.
     */
    public void applyEntanglement() {
        Question q = getCurrentQuestion();
        if (q == null) return;

        List<Answer> wrongOnes = q.getWrongAnswers();
        entangledAnswer = wrongOnes.get(new Random().nextInt(wrongOnes.size()));
    }

    /**
     * Returns true if the selected answer matches the entangled wrong answer.
     */
    public boolean isEntangled(Answer selected) {
        return selected != null && selected.equals(entangledAnswer);
    }

    /**
     * Applies the Interference lifeline, returning either the correct answer
     * or a random wrong answer with a 50% probability.
     */
    public Answer applyInterference() {
        Question q = getCurrentQuestion();
        if (q == null) return null;

        double roll = new Random().nextDouble();
        if (roll < 0.50) {
            return q.getCorrectAnswer();
        } else {
            List<Answer> wrongOnes = q.getWrongAnswers();
            if (wrongOnes.isEmpty()) return q.getCorrectAnswer();
            return wrongOnes.get(new Random().nextInt(wrongOnes.size()));
        }
    }

    public int getTotalQuestions() {
        return (questions != null) ? questions.size() : 0;
    }

    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerWon() { return playerWon; }

    public int getMoneyEarned() { return moneyEarned; }
    public int getLastEarnedMoney() { return lastEarnedMoney; }
    public int getGuaranteedMoney() { return guaranteedMoney; }

    public boolean isSuperpositionUsed() { return superpositionUsed; }
    public void setSuperpositionUsed(boolean used) { superpositionUsed = used; }

    public boolean isEntanglementUsed() { return entanglementUsed; }
    public void setEntanglementUsed(boolean used) { entanglementUsed = used; }

    public boolean isInterferenceUsed() { return interferenceUsed; }
    public void setInterferenceUsed(boolean used) { interferenceUsed = used; }

    /**
     * Returns the total number of lifelines used so far.
     */
    public int getLifelinesUsed() {
        int used = 0;
        if (superpositionUsed) used++;
        if (entanglementUsed) used++;
        if (interferenceUsed) used++;
        return used;
    }
}
