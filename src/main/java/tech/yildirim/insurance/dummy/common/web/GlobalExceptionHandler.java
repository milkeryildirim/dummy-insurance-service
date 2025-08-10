package tech.yildirim.insurance.dummy.common.web;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

/** Centralized exception handling for all @RestController instances. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles validation errors (@Valid).
   *
   * @return A response entity with a structured error message.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return new ResponseEntity<>(
        ErrorResponse.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Validation failed for requests.")
            .details(errors)
            .path(request.getDescription(false).replace("uri=", ""))
            .build(),
        HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles ResourceNotFound errors.
   *
   * @return A response entity with a 404 status and structured error message.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, WebRequest request) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build(),
        HttpStatus.NOT_FOUND);
  }

  /**
   * Handles data integrity violations, such as unique constraint failures.
   *
   * @return A response entity with a 409 Conflict status.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, WebRequest request) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error(HttpStatus.CONFLICT.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleIllegalExceptions(
      RuntimeException ex, WebRequest request) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
