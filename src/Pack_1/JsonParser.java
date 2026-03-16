package Pack_1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;

public class JsonParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Question> loadQuestions(String filename) {
        try {
            // This looks for the file in your 'resources' or 'assets' folder
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