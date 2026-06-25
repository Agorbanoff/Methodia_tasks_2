package bg.academy.outdoor.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class NotificationHistoryService {
    private static final Path HISTORY_FILE = Path.of("last-notified.json");
    private static final TypeReference<Set<String>> STRING_SET_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private Set<String> history;

    public NotificationHistoryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean wasNotified(String key) {
        return history().contains(key);
    }

    public void markNotified(String key) {
        if (history().add(key)) {
            saveHistory();
        }
    }

    private Set<String> history() {
        if (history == null) {
            history = loadHistory();
        }

        return history;
    }

    private Set<String> loadHistory() {
        if (!Files.exists(HISTORY_FILE)) {
            return new LinkedHashSet<>();
        }

        try {
            return objectMapper.readValue(HISTORY_FILE.toFile(), STRING_SET_TYPE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load notification history from " + HISTORY_FILE, e);
        }
    }

    private void saveHistory() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(HISTORY_FILE.toFile(), history);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save notification history to " + HISTORY_FILE, e);
        }
    }
}
