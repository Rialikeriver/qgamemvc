package Pack_1;

import Database.QuestionLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the admin question‑management screen. This class loads the full
 * question set from disk, exposes it for editing, and persists all changes
 * back to the JSON file. It acts as the data layer for {@link QModeSelectionController}
 * and {@link QModeSelectionView}.
 *
 * <p>The model keeps questions in a mutable list so the controller can add,
 * edit, and delete entries. All changes are saved using Jackson with pretty
 * printing enabled. The save logic attempts to write back to the actual
 * runtime resource location (e.g., bin/target), with a fallback to the
 * project root if the resource cannot be resolved.</p>
 */
public class QModeSelectionModel {

    private List<Question> questions;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String FILE_NAME = "BeMillionaireQuestions.json";

    /**
     * Loads all questions from the JSON file and prepares the mapper for
     * pretty‑printed output.
     */
    public QModeSelectionModel() {
        this.questions = new ArrayList<>(QuestionLoader.loadQuestions());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Returns the live list of questions for editing.
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Saves the current question list back to disk. The method attempts to
     * locate the JSON file in the runtime classpath; if found, it writes
     * directly to that file. If not found (e.g., running from an IDE without
     * copied resources), it writes to the project root as a fallback.
     */
    public void saveAll() {
        try {
            URL resource = getClass().getClassLoader().getResource(FILE_NAME);

            if (resource != null) {
                File file = new File(resource.toURI());
                mapper.writeValue(file, questions);
                System.out.println("Success! Updated live file at: " + file.getAbsolutePath());
            } else {
                File fallbackFile = new File(FILE_NAME);
                mapper.writeValue(fallbackFile, questions);
                System.out.println("Resource not found in bin. Saved to project root: " + fallbackFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Critical Error saving JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
