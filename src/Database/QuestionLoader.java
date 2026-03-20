package Database;

import Pack_1.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class responsible for loading question data from JSON files.
 * This class forms the persistence layer for the question bank used by
 * both gameplay and admin tools. It supports loading the default question
 * set as well as alternate language files.
 *
 * <p>The loader attempts two resource‑resolution strategies:</p>
 * <ul>
 *   <li>Classpath root lookup using <code>/filename</code></li>
 *   <li>Fallback lookup using the class loader directly</li>
 * </ul>
 *
 * <p>If the file cannot be found or parsed, the loader returns an empty list
 * and logs diagnostic messages to assist with debugging resource placement.</p>
 */
public class QuestionLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads the default question file <code>BeMillionaireQuestions.json</code>.
     *
     * @return a list of parsed {@link Question} objects, or an empty list on failure
     */
    public static List<Question> loadQuestions() {
        return loadQuestions("BeMillionaireQuestions.json");
    }

    /**
     * Loads a question file from the classpath and parses it into a list of
     * {@link Question} objects. Supports alternate language files and admin‑edited
     * question sets.
     *
     * @param filename the JSON file to load
     * @return a list of parsed questions, or an empty list if the file cannot be found or parsed
     */
    public static List<Question> loadQuestions(String filename) {
        try {
            System.out.println("DEBUG: Loading resource from path: /" + filename);

            InputStream is = QuestionLoader.class.getResourceAsStream("/" + filename);

            if (is == null) {
                System.out.println("DEBUG: Root load failed. Trying relative load...");
                is = QuestionLoader.class.getClassLoader().getResourceAsStream(filename);
            }

            if (is == null) {
                System.err.println("CRITICAL: File not found: " + filename);
                return new ArrayList<>();
            }

            List<Question> questions = mapper.readValue(
                    is,
                    new TypeReference<List<Question>>() {}
            );

            System.out.println("Successfully loaded " + questions.size() + " questions.");
            return questions;

        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
