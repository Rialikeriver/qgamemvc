package Pack_1;

import Database.QuestionLoader;
import java.util.List;

public class QModeSelectionModel {
    private List<Question> questions;
    
    public QModeSelectionModel() {
        this.questions = QuestionLoader.loadQuestions();
        System.out.println("Admin loaded " + questions.size() + " questions from JSON");
    }
    
    public List<Question> getQuestions() { return questions; }
    public Question getQuestion(int index) { 
        return index >= 0 && index < questions.size() ? questions.get(index) : null; 
    }
    public int getQuestionCount() { return questions.size(); }
}