package Pack_1;

import Database.QuestionLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QModeSelectionModel {
    private List<Question> questions;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String FILE_NAME = "BeMillionaireQuestions.json";

    public QModeSelectionModel() {
        this.questions = new ArrayList<>(QuestionLoader.loadQuestions());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<Question> getQuestions() { 
        return questions; 
    }

    public void saveAll() {
        try {
            // 1. Locate the file where the app is actually running (the bin/target folder)
            URL resource = getClass().getClassLoader().getResource(FILE_NAME);
            
            if (resource != null) {
                // Convert the URL to a physical File object
                File file = new File(resource.toURI());
                mapper.writeValue(file, questions);
                System.out.println("✅ Success! Updated live file at: " + file.getAbsolutePath());
            } else {
                // Fallback: If it's not in the bin folder yet, save to project root
                File fallbackFile = new File(FILE_NAME);
                mapper.writeValue(fallbackFile, questions);
                System.out.println("⚠️ Resource not found in bin. Saved to project root: " + fallbackFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("❌ Critical Error saving JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}