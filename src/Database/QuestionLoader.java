package Database;

import Pack_1.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class QuestionLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Question> loadQuestions() {
        return loadQuestions("BeMillionaireQuestions.json");
    }

    public static List<Question> loadQuestions(String filename) {
        try {
            // Log exactly what we are looking for
            System.out.println("DEBUG: Loading resource from path: /" + filename);
            
            InputStream is = QuestionLoader.class.getResourceAsStream("/" + filename);
            
            if (is == null) {
                // If "/" doesn't work, try without it as a fallback
                System.out.println("DEBUG: Root load failed. Trying relative load...");
                is = QuestionLoader.class.getClassLoader().getResourceAsStream(filename);
            }

            if (is == null) {
                System.err.println("CRITICAL: File not found: " + filename);
                return new ArrayList<>();
            }

            // This line actually converts the JSON text into Java Objects
            List<Question> questions = mapper.readValue(is, new TypeReference<List<Question>>() {});
            
            System.out.println("Successfully loaded " + questions.size() + " questions.");
            return questions;

        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}