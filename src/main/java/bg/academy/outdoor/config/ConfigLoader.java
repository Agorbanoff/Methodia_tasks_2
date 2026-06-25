package bg.academy.outdoor.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {
    private static final String CONFIG_RESOURCE = "config/sports.json";

    private final ObjectMapper objectMapper;

    public ConfigLoader() {
        this(new ObjectMapper());
    }

    public ConfigLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AppConfig loadConfig() {
        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(CONFIG_RESOURCE)) {
            if (inputStream == null) {
                throw new RuntimeException("Missing resource: " + CONFIG_RESOURCE);
            }

            return objectMapper.readValue(inputStream, AppConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + CONFIG_RESOURCE, e);
        }
    }
}
