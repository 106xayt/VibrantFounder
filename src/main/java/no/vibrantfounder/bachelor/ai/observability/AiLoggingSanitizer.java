package no.vibrantfounder.bachelor.ai.observability;

import java.util.regex.Pattern;

/**
 * Utility for sanitizing AI-related logs to avoid leaking sensitive information.
 *
 * The goal is to reduce the risk of logging:
 * - API keys
 * - secrets
 * - personal data
 * - large raw prompts/responses
 *
 * This implementation is intentionally conservative and can be improved iteratively.
 */
public final class AiLoggingSanitizer {

    private static final int MAX_LEN = 500;

    // Basic "looks like a key" pattern (conservative, not perfect)
    private static final Pattern KEY_LIKE = Pattern.compile(
            "(?i)(api[-_ ]?key|secret|token)\\s*[:=]\\s*[^\\s\"']+"
    );

    private AiLoggingSanitizer() {
        // Utility class
    }

    public static String sanitize(String input) {
        if (input == null) return null;

        String trimmed = input.strip();
        if (trimmed.length() > MAX_LEN) {
            trimmed = trimmed.substring(0, MAX_LEN) + "...";
        }

        // Replace key-like fragments
        return KEY_LIKE.matcher(trimmed).replaceAll("[REDACTED]");
    }
}
