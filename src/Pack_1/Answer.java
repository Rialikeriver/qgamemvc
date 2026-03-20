package Pack_1;

/**
 * Represents a single answer option for a question, including its label
 * (e.g., "A", "B", "C"), the displayed text, and whether it is the correct
 * answer. This model is designed for simple JSON serialization and is used
 * throughout the question/answer subsystem.
 *
 * <p>The {@code correct} flag directly matches the boolean field stored in
 * the question JSON files.</p>
 */
public class Answer {

    // Basic answer fields (label, text, correctness flag)
    private String label;
    private String text;
    private boolean correct; // Matches "correct" in JSON

    /** Default constructor for JSON deserialization. */
    public Answer() {}

    // Getters
    public String getLabel() { return label; }
    public String getText() { return text; }
    public boolean isCorrect() { return correct; }

    // Setters
    public void setLabel(String label) { this.label = label; }
    public void setText(String text) { this.text = text; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
