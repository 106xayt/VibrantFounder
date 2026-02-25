package no.vibrantfounder.bachelor.common.error;

public record ApiFieldViolation(
        String field,
        String message
) {
}
