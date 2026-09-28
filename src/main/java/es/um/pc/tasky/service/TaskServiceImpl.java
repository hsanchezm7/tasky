package es.um.pc.tasky.service;

import es.um.pc.tasky.dto.TaskStatsResponse;
import es.um.pc.tasky.exception.InvalidTaskException;
import es.um.pc.tasky.exception.ResourceNotFoundException;
import es.um.pc.tasky.model.Task;
import es.um.pc.tasky.model.TaskPriority;
import es.um.pc.tasky.model.TaskStatus;
import es.um.pc.tasky.repository.TaskRepository;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;

  public TaskServiceImpl(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public Task createTask(Task task) {
    validateDueDateNotInPast(task.getDueDate());

    if (task.getStatus() == null) {
      task.setStatus(TaskStatus.PENDING);
    }

    return taskRepository.save(task);
  }

  @Override
  public Task getTaskById(Long id) {
    return taskRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con id: " + id));
  }

  @Override
  public List<Task> getAllTasks(TaskStatus status) {
    if (status == null) {
      return taskRepository.findAll();
    }
    return taskRepository.findByStatus(status);
  }

  @Override
  public Task updateTask(Long id, Task updatedTask) {
    Task existing = getTaskById(id);

    // Regla de negocio: una tarea completada no puede volver a modificarse.
    if (existing.getStatus() == TaskStatus.COMPLETED) {
      throw new InvalidTaskException("No se puede modificar una tarea que ya está completada");
    }

    validateDueDateNotInPast(updatedTask.getDueDate());

    existing.setTitle(updatedTask.getTitle());
    existing.setDescription(updatedTask.getDescription());
    existing.setPriority(updatedTask.getPriority());
    existing.setDueDate(updatedTask.getDueDate());

    if (updatedTask.getStatus() != null) {
      existing.setStatus(updatedTask.getStatus());
    }

    return taskRepository.save(existing);
  }

  @Override
  public void deleteTask(Long id) {
    Task existing = getTaskById(id);
    taskRepository.delete(existing);
  }

  @Override
  public TaskStatsResponse getStats() {
    Map<TaskStatus, Long> byStatus = new EnumMap<>(TaskStatus.class);
    for (TaskStatus status : TaskStatus.values()) {
      byStatus.put(status, taskRepository.countByStatus(status));
    }

    Map<TaskPriority, Long> byPriority = new EnumMap<>(TaskPriority.class);
    for (TaskPriority priority : TaskPriority.values()) {
      byPriority.put(priority, taskRepository.countByPriority(priority));
    }

    // Una tarea vencida sigue abierta y su fecha límite ya pasó; la que vence hoy no cuenta.
    long overdue = taskRepository.countByDueDateBeforeAndStatusNotIn(
        LocalDate.now(), EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED));

    return new TaskStatsResponse(taskRepository.count(), byStatus, byPriority, overdue);
  }
  
  @Override
  public List<Task> getOverdueTasks() {
    return taskRepository.findByDueDateBeforeAndStatusNotIn(
        LocalDate.now(), EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED));
  }

  /** Regla de negocio: no se puede crear ni dejar una tarea con una fecha límite pasada. */
  private void validateDueDateNotInPast(LocalDate dueDate) {
    if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
      throw new InvalidTaskException("La fecha límite no puede ser una fecha pasada");
    }
  }
  
}
