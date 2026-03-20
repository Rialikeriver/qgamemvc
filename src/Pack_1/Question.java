package Pack_1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single multiple‑choice question in Quantum Millionaire.
 * Each question belongs to a tier, contains four answers, and identifies
 * exactly one correct answer. The class is fully JSON‑serializable using
 * Jackson, with explicit property mappings to ensure compatibility with
 * the question bank format.
 *
 * <p>Helper methods such as {@link #getWrongAnswers()} and
 * {@link #getCorrectAnswer()} are excluded from JSON output and used only
 * by gameplay logic. Labels (A–D) are assigned dynamically by the model
 * when questions are presented to the player.</p>
 */
public class Question {

    private String id;
    private int tier;

    @JsonProperty("question")
    private String questionText;

    @JsonProperty("answers")
    private List<Answer> answerList;

    /**
     * Default constructor required for JSON deserialization.
     */
    public Question() {}

    /**
     * Creates a fully specified question with ID, tier, text, and answers.
     */
    public Question(String id, int tier, String questionText, List<Answer> answers) {
        this.id = id;
        this.tier = tier;
        this.questionText = questionText;
        this.answerList = answers;
    }

    /**
     * Returns all incorrect answers. Ignored during JSON serialization.
     */
    @JsonIgnore
    public List<Answer> getWrongAnswers() {
        return answerList.stream()
                .filter(a -> !a.isCorrect())
                .collect(Collectors.toList());
    }

    /**
     * Returns the correct answer, or null if none is marked. Ignored in JSON.
     */
    @JsonIgnore
    public Answer getCorrectAnswer() {
        return answerList.stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElse(null);
    }

    // --- Getters and Setters ---

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
