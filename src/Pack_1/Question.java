package Pack_1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

public class Question {
    private String id;
    private int tier;
    
    @JsonProperty("question") // Maps the field 'questionText' to 'question' in JSON
    private String questionText;
    
    @JsonProperty("answers") // Maps 'answerList' to 'answers' in JSON
    private List<Answer> answerList;

    public Question() {}

    public Question(String id, int tier, String questionText, List<Answer> answers) {
        this.id = id;
        this.tier = tier;
        this.questionText = questionText;
        this.answerList = answers;
    }

    // --- LOGIC HELPERS (IGNORE THESE IN JSON) ---

    @JsonIgnore // This prevents "wrongAnswers" from appearing in the JSON file
    public List<Answer> getWrongAnswers() {
        return answerList.stream()
                .filter(a -> !a.isCorrect())
                .collect(Collectors.toList());
    }

    @JsonIgnore // This prevents "correctAnswer" from appearing in the JSON file
    public Answer getCorrectAnswer() {
        return answerList.stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElse(null);
    }

    // --- GETTERS AND SETTERS ---
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }

    @JsonProperty("question")
    public String getQuestionText() { return questionText; }
    @JsonProperty("question")
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    @JsonProperty("answers")
    public List<Answer> getAnswers() { return answerList; }
    @JsonProperty("answers")
    public void setAnswers(List<Answer> answers) { this.answerList = answers; }
}