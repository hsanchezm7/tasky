package es.um.pc.tasky.exception;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo de errores centralizado para toda la API. Garantiza que un recurso inexistente devuelva
 * 404 (y no una traza de excepción), que las violaciones de reglas de negocio devuelvan 400, y que
 * los errores de validación de Bean Validation se traduzcan a una respuesta clara por campo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            extractPath(request));
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InvalidTaskException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTask(
      InvalidTaskException ex, WebRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            extractPath(request));
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation Failed");

    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    body.put("errors", fieldErrors);
    body.put("path", extractPath(request));

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Parámetro con un valor que no se puede convertir al tipo esperado (p. ej. {@code ?status=FOO}).
   * Devuelve 400 indicando los valores permitidos.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    String message =
        "Valor inválido '" + ex.getValue() + "' para el parámetro '" + ex.getName() + "'";
    Class<?> requiredType = ex.getRequiredType();
    if (requiredType != null && requiredType.isEnum()) {
      String allowed =
          Arrays.stream(requiredType.getEnumConstants())
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      message += ". Valores permitidos: " + allowed;
    }

    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            extractPath(request));
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Ha ocurrido un error inesperado",
            extractPath(request));
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private String extractPath(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
  }
}
