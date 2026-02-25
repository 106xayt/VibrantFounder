package no.vibrantfounder.bachelor.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        Instant timestamp,
        List<ApiFieldViolation> violations
) {
}
