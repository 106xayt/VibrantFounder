package no.vibrantfounder.bachelor.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ai.anthropic")
public record AnthropicProperties(
        String baseUrl,
        String apiKey,
        String model,
        Integer maxTokens,
        Double temperature,
        Duration timeout
) {
    public AnthropicProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.anthropic.com";
        }
        if (timeout == null) {
            timeout = Duration.ofMinutes(5);
        }
        if (model == null || model.isBlank()) {
            model = "claude-3-5-sonnet-20241022";
        }
        if (maxTokens == null || maxTokens <= 0) {
            maxTokens = 4096;
        }
        if (temperature == null) {
            temperature = 0.2;
        }
    }
}