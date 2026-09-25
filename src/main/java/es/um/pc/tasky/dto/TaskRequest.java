package es.um.pc.tasky.dto;

import es.um.pc.tasky.model.TaskPriority;
import es.um.pc.tasky.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de entrada usado por el controlador para crear o actualizar una tarea.
 * El estado (status) es opcional: si no se informa al crear, el servicio
 * asigna PENDING por defecto.
 */
public class TaskRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String title;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String description;

    private TaskStatus status;

    @NotNull(message = "La prioridad es obligatoria")
    private TaskPriority priority;

    @NotNull(message = "La fecha límite es obligatoria")
    @FutureOrPresent(message = "La fecha límite no puede ser una fecha pasada")
    private LocalDate dueDate;

    public TaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
