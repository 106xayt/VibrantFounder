package no.vibrantfounder.bachelor.ai.orchestration;

/**
 * Exception type for AI-related failures, such as:
 * - provider errors
 * - timeouts
 * - rate limits
 * - invalid AI output (parsing/validation failures)
 */
public class AiException extends RuntimeException {

    public enum Type {
        PROVIDER_ERROR,
        TIMEOUT,
        RATE_LIMITED,
        BAD_OUTPUT
    }

    private final Type type;

    public AiException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public AiException(Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
