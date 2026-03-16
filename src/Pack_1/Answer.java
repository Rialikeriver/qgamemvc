package Pack_1;

public class Answer {
    private String label;
    private String text;
    private boolean correct; // Matches "correct" in JSON

    public Answer() {}

    public String getLabel() { return label; }
    public String getText() { return text; }
    public boolean isCorrect() { return correct; }

    public void setLabel(String label) { this.label = label; }
    public void setText(String text) { this.text = text; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}