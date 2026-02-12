package no.vibrantfounder.bachelor.ai.orchestration;

/**
 * Standard wrapper for results returned from the AI layer.
 *
 * @param value       Parsed and validated output mapped to a domain or DTO type
 * @param rawText     Raw text output returned by the AI model
 * @param inputTokens Number of input tokens used in the request
 * @param outputTokens Number of output tokens produced by the model
 * @param stopReason  Reason why the model stopped generating output
 */
public record AiResult<T>(
        T value,
        String rawText,
        Integer inputTokens,
        Integer outputTokens,
        String stopReason
) {
}
