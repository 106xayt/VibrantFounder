package no.vibrantfounder.bachelor.ai.orchestration;

/**
 * Configuration options for an AI call.
 *
 * This class makes model behavior explicit and configurable,
 * rather than relying on hidden defaults.
 */
public record AiCallOptions(
        String model,
        int maxTokens,
        double temperature
) {
}
