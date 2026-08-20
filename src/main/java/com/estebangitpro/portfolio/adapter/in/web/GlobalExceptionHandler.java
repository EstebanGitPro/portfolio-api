package com.estebangitpro.portfolio.adapter.in.web;

import com.estebangitpro.portfolio.core.application.exception.ProjectNotFoundException;
import com.estebangitpro.portfolio.core.domain.DuplicateProjectSlugException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateProjectSlugException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateSlug(DuplicateProjectSlugException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage(), "slug", ex.getSlug()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> detail(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(errorBody("Validation failed", fieldErrors));
    }

    /**
     * A body Jackson cannot turn into the target type is the caller's mistake, not ours —
     * an unknown enum constant, a string where a number belongs, plain broken JSON. Without
     * this handler these land in {@link #handleGeneric} and get reported as a 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        List<Map<String, String>> details = (ex.getCause() instanceof MismatchedInputException cause)
                ? List.of(detail(fieldPath(cause), describe(cause)))
                : List.of();

        return ResponseEntity.badRequest().body(errorBody("Malformed request body", details));
    }

    /**
     * Last resort. Spring's own exceptions already carry the right status — an unknown
     * route, an unsupported method, an unreadable body — so they are rethrown instead of
     * being flattened into a 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) throws Exception {
        if (ex instanceof ErrorResponse) {
            throw ex;
        }
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }

    private Map<String, Object> errorBody(String message, List<Map<String, String>> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("details", details);
        return body;
    }

    private Map<String, String> detail(String field, String message) {
        Map<String, String> detail = new LinkedHashMap<>();
        detail.put("field", field);
        detail.put("message", message);
        return detail;
    }

    /**
     * Rebuilds the dotted path Jackson walked before failing, so the caller is told which
     * field to fix instead of being handed the whole body back.
     */
    private String fieldPath(MismatchedInputException ex) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference reference : ex.getPath()) {
            if (reference.getPropertyName() != null) {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(reference.getPropertyName());
            } else if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.isEmpty() ? "body" : path.toString();
    }

    private String describe(MismatchedInputException ex) {
        Class<?> targetType = ex.getTargetType();

        if (ex instanceof InvalidFormatException invalid && targetType != null && targetType.isEnum()) {
            String accepted = Arrays.stream(targetType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return "Invalid value '%s'. Accepted values: %s".formatted(invalid.getValue(), accepted);
        }
        if (targetType != null) {
            return "Expected a value of type %s".formatted(targetType.getSimpleName());
        }
        return ex.getOriginalMessage();
    }
}
