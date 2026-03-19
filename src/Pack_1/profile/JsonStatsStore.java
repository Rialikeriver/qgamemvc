package Pack_1.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;

public class JsonStatsStore implements StatsStore {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final File file;

    public JsonStatsStore() {
        this.file = new File("Database/stats.json");
    }

    @Override
    public GlobalStats loadStats() {
        try {
            if (!file.exists()) return new GlobalStats();
            return mapper.readValue(file, GlobalStats.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new GlobalStats();
        }
    }

    @Override
    public void saveStats(GlobalStats stats) {
        try {
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, stats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
