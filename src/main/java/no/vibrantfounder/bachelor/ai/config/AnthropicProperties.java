package no.vibrantfounder.bachelor.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the Anthropic AI provider.
 *
 * Values are expected to be provided via application properties
 * or environment variables.
 */
@ConfigurationProperties(prefix = "ai.anthropic")
public record AnthropicProperties(
        String apiKey,
        String baseUrl,
        String model,
        int maxTokens,
        double temperature,
        Duration timeout
) {
}
