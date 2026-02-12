package no.vibrantfounder.bachelor.ai.observability;

/**
 * Utility class for handling correlation IDs across AI-related operations.
 *
 * Correlation IDs are used to trace a single request across multiple
 * layers of the system, especially during AI orchestration.
 */
public final class CorrelationId {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private CorrelationId() {
        // Utility class
    }

    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static String get() {
        return CORRELATION_ID.get();
    }

    public static void clear() {
        CORRELATION_ID.remove();
    }
}
