package es.um.pc.tasky.exception;

/**
 * Se lanza cuando se solicita, actualiza o elimina un recurso que no existe.
 * El GlobalExceptionHandler la traduce a un HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
