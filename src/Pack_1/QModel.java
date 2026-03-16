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

    public QModel(List<Question> questions) {
        this.questions = questions;
    }

    public void setQuestions(List<Question> newQuestions) {
        this.questions = newQuestions;
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
        if (currentQuestionIndex < questions.size()) {
            currentQuestionIndex++;
        }
    }

    public void resetGame() {
        this.currentQuestionIndex = 0;
        this.shuffledQuestions.clear();
    }

    public List<Answer> applySuperposition() {
        Question q = getCurrentQuestion();
        if (q == null) return null;

        List<Answer> wrongOnes = q.getWrongAnswers();
        Collections.shuffle(wrongOnes);
        
        int numToCollapse = new Random().nextInt(3) + 1; 
        return wrongOnes.subList(0, Math.min(numToCollapse, wrongOnes.size()));
    }

    private Answer entangledAnswer = null;

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
}