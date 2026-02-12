package no.vibrantfounder.bachelor.ai.orchestration;

/**
 * Validates parsed AI output before it is returned to the domain layer.
 *
 * This supports Intelligent Decision Support System (IDSS) principles by ensuring
 * that recommendations are structurally complete and safe to use.
 */
public interface OutputValidator<T> {

    /**
     * Validate the parsed AI output.
     *
     * Implementations should throw an exception (typically AiException or IllegalArgumentException)
     * if the output is incomplete, inconsistent, or violates required constraints.
     */
    void validate(T output);
}
