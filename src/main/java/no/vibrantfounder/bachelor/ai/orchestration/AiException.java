package no.vibrantfounder.bachelor.ai.orchestration;

/**
 * Exception type for AI-related failures, such as:
 * - provider errors/timeouts
 * - invalid output that cannot be repaired
 * - parsing/validation failures
 */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
