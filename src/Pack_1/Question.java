package Pack_1;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors; // Added this import
import com.fasterxml.jackson.annotation.JsonProperty;

public class Question {

    private String id;
    private int tier;
    
    @JsonProperty("question") 
    private String questionText;
    
    @JsonProperty("answers") 
    private List<Answer> answerList;

    public Question() {}

    public Question(String id, int tier, String questionText, List<Answer> answers) {
        this.id = id;
        this.tier = tier;
        this.questionText = questionText;
        this.answerList = answers;
    }

    // Helper to get wrong answers for Quantum logic
    public List<Answer> getWrongAnswers() {
        return answerList.stream()
                .filter(a -> !a.isCorrect())
                .collect(Collectors.toList());
    }

    public Answer getCorrectAnswer() {
        return answerList.stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElse(null);
    }

    // Getters and Setters
    public String getId() { return id; }
    public int getTier() { return tier; }
    public String getQuestionText() { return questionText; }
    public List<Answer> getAnswers() { return answerList; }
}