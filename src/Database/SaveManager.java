package Database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Pack_1.Question;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages persistent tracking of which questions have been used in gameplay.
 * This class maintains a JSON-backed map of question IDs to usage flags,
 * allowing the game to avoid repeating questions and ensuring each tier
 * always has at least one unused question available.
 *
 * <p>The save file is stored as <code>SaveFile.json</code> in the working
 * directory. The manager automatically loads existing data, repairs missing
 * or corrupted entries, and ensures all questions from the current dataset
 * are represented.</p>
 *
 * <p>Tier-level operations allow the game or admin tools to reset usage
 * for a specific tier, count unused questions, or verify whether a tier
 * still has available questions. A full reset is also supported.</p>
 */
public class SaveManager {

    private static final String SAVE_FILE = "SaveFile.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Boolean> usedMap = new LinkedHashMap<>();

    private final List<Question> allQuestions;

    /**
     * Creates a SaveManager for the given question set. Automatically loads
     * existing save data, validates tier integrity, and writes repaired data
     * back to disk.
     *
     * @param allQuestions the full list of questions used by the game
     */
    public SaveManager(List<Question> allQuestions) {
        this.allQuestions = allQuestions;
        load();
        validateTiers();
        save();
    }

    // ------------------------------------------------------------
    // LOAD / SAVE
    // ------------------------------------------------------------

    /**
     * Loads the save file if present, repairing missing entries and ensuring
     * all question IDs are represented.
     */
    private void load() {
        File file = new File(SAVE_FILE);

        if (file.exists()) {
            try {
                Map<String, Boolean> loaded =
                        mapper.readValue(file, new TypeReference<LinkedHashMap<String, Boolean>>() {});
                usedMap.putAll(loaded);
            } catch (Exception e) {
                System.out.println("Save file corrupted. Rebuilding...");
            }
        }

        for (Question q : allQuestions) {
            usedMap.putIfAbsent(q.getId(), false);
        }
    }

    /**
     * Writes the current usage map to disk in pretty-printed JSON format.
     */
    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVE_FILE), usedMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------
    // BASIC OPERATIONS
    // ------------------------------------------------------------

    /**
     * Returns whether the given question ID has been used.
     */
    public boolean isUsed(String id) {
        return usedMap.getOrDefault(id, false);
    }

    /**
     * Marks a question as used and immediately persists the change.
     */
    public void markUsed(String id) {
        usedMap.put(id, true);
        save();
    }

    /**
     * Resets all questions to unused and saves the result.
     */
    public void resetAll() {
        usedMap.replaceAll((k, v) -> false);
        save();
    }

    // ------------------------------------------------------------
    // TIER OPERATIONS
    // ------------------------------------------------------------

    /**
     * Counts how many unused questions remain in the given tier.
     *
     * @param tier the tier number (1–15)
     * @return number of unused questions in that tier
     */
    public long countUnusedInTier(int tier) {
        String prefix = String.format("T%02d", tier);

        return usedMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .filter(e -> !e.getValue())
                .count();
    }

    /**
     * Returns whether the given tier has at least one unused question.
     */
    public boolean hasUnusedInTier(int tier) {
        return countUnusedInTier(tier) > 0;
    }

    /**
     * Resets usage flags for all questions in the given tier.
     */
    public void resetTier(int tier) {
        String prefix = String.format("T%02d", tier);

        usedMap.replaceAll((id, used) -> id.startsWith(prefix) ? false : used);
        save();
    }

    // ------------------------------------------------------------
    // VALIDATION / AUTO-REPAIR
    // ------------------------------------------------------------

    /**
     * Ensures every tier has at least one unused question. If a tier is
     * exhausted or corrupted, it is automatically reset.
     */
    private void validateTiers() {
        for (int tier = 1; tier <= 15; tier++) {
            if (!hasUnusedInTier(tier)) {
                System.out.println("Tier " + tier + " exhausted or corrupted. Resetting tier...");
                resetTier(tier);
            }
        }
    }

    // ------------------------------------------------------------
    // DEBUGGING / UTILITIES
    // ------------------------------------------------------------

    /**
     * Counts how many questions have been marked as used across all tiers.
     */
    public int countUsedTotal() {
        return (int) usedMap.values().stream().filter(v -> v).count();
    }

    /**
     * Prints a summary of unused question counts per tier and total usage.
     */
    public void printStatus() {
        System.out.println("=== Save File Status ===");
        for (int tier = 1; tier <= 15; tier++) {
            System.out.println("Tier " + tier + ": " + countUnusedInTier(tier) + " unused");
        }
        System.out.println("Total used: " + countUsedTotal());
    }
}
