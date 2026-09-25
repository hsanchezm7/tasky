package es.um.pc.tasky.controller;

import es.um.pc.tasky.dto.TaskRequest;
import es.um.pc.tasky.dto.TaskResponse;
import es.um.pc.tasky.model.Task;
import es.um.pc.tasky.model.TaskStatus;
import es.um.pc.tasky.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping
  public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
    Task created = taskService.createTask(toEntity(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  @GetMapping
  public ResponseEntity<List<TaskResponse>> getAllTasks(
      @RequestParam(required = false) TaskStatus status) {
    List<TaskResponse> tasks =
        taskService.getAllTasks(status).stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
    return ResponseEntity.ok(toResponse(taskService.getTaskById(id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TaskResponse> updateTask(
      @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
    Task updated = taskService.updateTask(id, toEntity(request));
    return ResponseEntity.ok(toResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }

  private Task toEntity(TaskRequest request) {
    Task task = new Task();
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setPriority(request.getPriority());
    task.setDueDate(request.getDueDate());
    return task;
  }

  private TaskResponse toResponse(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getStatus(),
        task.getPriority(),
        task.getDueDate());
  }
}
