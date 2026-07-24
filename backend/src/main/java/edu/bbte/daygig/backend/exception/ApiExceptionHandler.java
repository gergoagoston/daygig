package edu.bbte.daygig.backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthException ex,
                                                                HttpServletRequest request) {
        log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed",
                ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                   HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found",
                ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException ex,
                                                                    HttpServletRequest request) {
        log.warn("Resource conflict at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.CONFLICT, "Resource Conflict",
                ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex,
                                                           HttpServletRequest request) {
        log.warn("Forbidden request at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.FORBIDDEN, "Forbidden",
                ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        Map<String, String> validationDetails = new HashMap<>();
        ex
                .getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = getValidationFieldName(error);
                    String message = error.getDefaultMessage();
                    validationDetails.put(fieldName, message);
                });

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Invalid fields in request", request.getRequestURI(), validationDetails);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                         HttpServletRequest request) {
        log.warn("Malformed request body at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.BAD_REQUEST, "Malformed Request",
                "Request body is missing or malformed", request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                             HttpServletRequest request) {
        Map<String, String> details = Map.of(ex.getName(), "Invalid value: " + ex.getValue());

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request Parameter",
                "Invalid request parameter or path variable", request.getRequestURI(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                         HttpServletRequest request) {
        log.error("Database integrity violation occurred at path: {}", request.getRequestURI(), ex);

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.CONFLICT, "Database Conflict",
                "A database constraint was violated (e.g., duplicate data or missing relation).",
                request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(DataAccessException ex,
                                                                      HttpServletRequest request) {
        log.error("Database error occurred at path: {}", request.getRequestURI(), ex);

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error",
                "A server-side database error occurred: " + ex
                        .getMostSpecificCause()
                        .getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceException(ServiceException ex,
                                                                   HttpServletRequest request) {
        log.warn("Business logic error at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse errorResponse = buildBaseErrorResponse(HttpStatus.BAD_REQUEST, "Business Logic Error",
                ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ApiErrorResponse buildBaseErrorResponse(HttpStatus status,
                                                    String errorKeyword,
                                                    String message,
                                                    String path,
                                                    Map<String, String> details) {
        return new ApiErrorResponse(LocalDateTime.now(), status.value(), errorKeyword, message, path, details);
    }

    private String getValidationFieldName(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField();
        }

        return error.getObjectName();
    }
}
