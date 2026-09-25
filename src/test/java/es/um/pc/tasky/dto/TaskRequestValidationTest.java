package es.um.pc.tasky.dto;

import es.um.pc.tasky.model.TaskPriority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios PUROS de Bean Validation: se usa directamente un
 * jakarta.validation.Validator, sin levantar ningún contexto de Spring.
 */
class TaskRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    @DisplayName("Un título en blanco es inválido")
    void request_withBlankTitle_isInvalid() {
        TaskRequest request = new TaskRequest();
        request.setTitle("   ");
        request.setPriority(TaskPriority.MEDIUM);
        request.setDueDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    @DisplayName("Una fecha límite pasada es inválida")
    void request_withPastDueDate_isInvalid() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea");
        request.setPriority(TaskPriority.LOW);
        request.setDueDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dueDate")));
    }

    @Test
    @DisplayName("No informar la prioridad es inválido")
    void request_withoutPriority_isInvalid() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea");
        request.setDueDate(LocalDate.now());

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("priority")));
    }

    @Test
    @DisplayName("Un título que supera los 100 caracteres es inválido")
    void request_withTitleTooLong_isInvalid() {
        TaskRequest request = new TaskRequest();
        request.setTitle("a".repeat(101));
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    @DisplayName("Una petición con todos los campos válidos no genera violaciones")
    void request_valid_hasNoViolations() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea válida");
        request.setDescription("Descripción de la tarea");
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(3));

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
