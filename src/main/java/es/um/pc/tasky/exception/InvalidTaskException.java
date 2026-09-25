package es.um.pc.tasky.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio de la aplicación
 * (por ejemplo: fecha límite pasada, o intentar modificar una tarea completada).
 * El GlobalExceptionHandler la traduce a un HTTP 400.
 */
public class InvalidTaskException extends RuntimeException {

    public InvalidTaskException(String message) {
        super(message);
    }
}
