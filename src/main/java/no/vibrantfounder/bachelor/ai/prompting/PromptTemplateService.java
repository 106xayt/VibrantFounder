package no.vibrantfounder.bachelor.ai.prompting;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads prompt templates from classpath resources (src/main/resources/prompts).
 *
 * Each PromptId maps to:
 *  - prompts/<base>.system.txt
 *  - prompts/<base>.user.txt
 *
 * This keeps prompt text out of code and makes prompts easy to version and document.
 */
@Service
public class PromptTemplateService {

    private final ResourceLoader resourceLoader;

    public PromptTemplateService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String loadSystem(PromptId id) {
        return load("prompts/" + baseName(id) + ".system.txt");
    }

    public String loadUser(PromptId id) {
        return load("prompts/" + baseName(id) + ".user.txt");
    }

    private String baseName(PromptId id) {
        return switch (id) {
            case MARKETING_PLAN_V1 -> "marketing_plan_v1";
            case FORMAT_REPAIR_V1 -> "format_repair_v1";
        };
    }

    private String load(String classpathLocation) {
        try (var in = resourceLoader.getResource("classpath:" + classpathLocation).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Missing prompt template: " + classpathLocation, e);
        }
    }
}
