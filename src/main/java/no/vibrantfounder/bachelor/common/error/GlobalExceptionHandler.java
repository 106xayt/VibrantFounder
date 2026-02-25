package no.vibrantfounder.bachelor.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import no.vibrantfounder.bachelor.ai.observability.CorrelationId;
import no.vibrantfounder.bachelor.ai.orchestration.AiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------
    // AI errors (mapped by type)
    // ---------------------------
    @ExceptionHandler(AiException.class)
    public ResponseEntity<ApiErrorResponse> handleAiException(AiException ex, HttpServletRequest request) {
        String correlationId = CorrelationId.get();

        HttpStatus status = mapStatus(ex.getType());
        String code = mapCode(ex.getType());

        // Log stacktrace for provider/timeouts/rate limits, but keep noise down for BAD_OUTPUT.
        if (ex.getType() == AiException.Type.BAD_OUTPUT) {
            log.warn("{} correlationId={} path={} message={}", code, correlationId, request.getRequestURI(), ex.getMessage());
        } else {
            log.error("{} correlationId={} path={}", code, correlationId, request.getRequestURI(), ex);
        }

        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                code,
                safeMessage(ex),
                request.getRequestURI(),
                correlationId,
                Instant.now(),
                null
        );

        return ResponseEntity.status(status).body(body);
    }

    // ---------------------------
    // Validation errors (JSON body -> @Valid)
    // ---------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String correlationId = CorrelationId.get();

        List<ApiFieldViolation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed.",
                request.getRequestURI(),
                correlationId,
                Instant.now(),
                violations
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ---------------------------
    // Validation errors (query params/path vars -> @Validated)
    // ---------------------------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String correlationId = CorrelationId.get();

        List<ApiFieldViolation> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> new ApiFieldViolation(
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : null,
                        v.getMessage()
                ))
                .toList();

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed.",
                request.getRequestURI(),
                correlationId,
                Instant.now(),
                violations
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ---------------------------
    // Bad request (business preconditions)
    // ---------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        String correlationId = CorrelationId.get();

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId,
                Instant.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ---------------------------
    // Fallback
    // ---------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        String correlationId = CorrelationId.get();

        log.error("INTERNAL_SERVER_ERROR correlationId={} path={}", correlationId, request.getRequestURI(), ex);

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                request.getRequestURI(),
                correlationId,
                Instant.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiFieldViolation toViolation(FieldError fe) {
        return new ApiFieldViolation(fe.getField(), fe.getDefaultMessage());
    }

    private HttpStatus mapStatus(AiException.Type type) {
        if (type == null) return HttpStatus.BAD_GATEWAY;

        return switch (type) {
            case BAD_OUTPUT -> HttpStatus.UNPROCESSABLE_ENTITY; // 422
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;  // 429
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;         // 504
            case PROVIDER_ERROR -> HttpStatus.BAD_GATEWAY;      // 502
        };
    }

    private String mapCode(AiException.Type type) {
        if (type == null) return "AI_PROVIDER_ERROR";

        return switch (type) {
            case BAD_OUTPUT -> "AI_BAD_OUTPUT";
            case RATE_LIMITED -> "AI_RATE_LIMITED";
            case TIMEOUT -> "AI_TIMEOUT";
            case PROVIDER_ERROR -> "AI_PROVIDER_ERROR";
        };
    }

    private String safeMessage(AiException ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            return "AI request failed.";
        }
        if (msg.length() <= 500) {
            return msg;
        }
        return msg.substring(0, 500) + "...";
    }
}
