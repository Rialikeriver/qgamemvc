package Pack_1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;

/**
 * Utility for loading question data from JSON files packaged in the application's
 * resources. This class provides a single static method that reads a JSON array
 * of {@link Question} objects and converts it into a Java list using Jackson.
 *
 * <p>The parser expects the JSON file to be located on the classpath (typically
 * under {@code resources/}). If the file cannot be found or parsed, the method
 * logs an error and returns {@code null}.</p>
 */
public class JsonParser {

    // Shared mapper for simple JSON deserialization
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads a list of {@link Question} objects from a JSON file located in the
     * application's resources directory.
     *
     * @param filename the name of the JSON file (relative to the classpath root)
     * @return a list of questions, or {@code null} if the file is missing or invalid
     */
    public static List<Question> loadQuestions(String filename) {
        try {
            InputStream is = JsonParser.class.getResourceAsStream("/" + filename);
            if (is == null) {
                System.err.println("Could not find file: " + filename);
                return null;
            }
            return mapper.readValue(is, new TypeReference<List<Question>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
