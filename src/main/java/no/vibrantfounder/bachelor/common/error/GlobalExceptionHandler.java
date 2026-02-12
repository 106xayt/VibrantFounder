package no.vibrantfounder.bachelor.common.error;

import no.vibrantfounder.bachelor.ai.observability.CorrelationId;
import no.vibrantfounder.bachelor.ai.orchestration.AiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

/**
 * Global exception handler that returns consistent error responses
 * and always includes the correlationId.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AiException.class)
    public ResponseEntity<ErrorResponse> handleAiException(AiException ex) {
        String correlationId = CorrelationId.get();

        // Log full stacktrace + root cause (needed to debug provider failures)
        log.error("AI_PROVIDER_ERROR correlationId={}", correlationId, ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                new ErrorResponse(
                        502,
                        correlationId,
                        "AI_PROVIDER_ERROR",
                        OffsetDateTime.now().toString(),
                        ex.getMessage() + (ex.getCause() != null ? " | cause: " + ex.getCause().getMessage() : "")
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        String correlationId = CorrelationId.get();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        400,
                        correlationId,
                        "BAD_REQUEST",
                        OffsetDateTime.now().toString(),
                        ex.getMessage()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex) {
        String correlationId = CorrelationId.get();

        log.error("UNHANDLED_ERROR correlationId={}", correlationId, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        500,
                        correlationId,
                        "INTERNAL_SERVER_ERROR",
                        OffsetDateTime.now().toString(),
                        "An unexpected error occurred."
                )
        );
    }

    /**
     * Simple error response contract.
     * If you already have an ErrorResponse type, keep yours and remove this.
     */
    public record ErrorResponse(
            int status,
            String correlationId,
            String error,
            String timestamp,
            String message
    ) {}
}
