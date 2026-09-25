package es.um.pc.tasky.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.um.pc.tasky.exception.InvalidTaskException;
import es.um.pc.tasky.exception.ResourceNotFoundException;
import es.um.pc.tasky.model.Task;
import es.um.pc.tasky.model.TaskPriority;
import es.um.pc.tasky.model.TaskStatus;
import es.um.pc.tasky.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios PUROS de la capa de servicio: el repositorio se mockea con Mockito y no se
 * levanta ningún contexto de Spring ni base de datos. Se comprueban reglas de negocio reales, no
 * simples assertNotNull.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

  @Mock private TaskRepository taskRepository;

  private TaskServiceImpl taskService;

  private Task sampleTask;

  @BeforeEach
  void setUp() {
    taskService = new TaskServiceImpl(taskRepository);

    sampleTask = new Task();
    sampleTask.setId(1L);
    sampleTask.setTitle("Tarea de prueba");
    sampleTask.setDescription("Descripción de prueba");
    sampleTask.setStatus(TaskStatus.PENDING);
    sampleTask.setPriority(TaskPriority.MEDIUM);
    sampleTask.setDueDate(LocalDate.now().plusDays(5));
  }

  @Test
  @DisplayName("No se puede crear una tarea con fecha límite pasada")
  void createTask_withPastDueDate_throwsInvalidTaskException() {
    Task task = new Task();
    task.setTitle("Tarea vencida");
    task.setPriority(TaskPriority.HIGH);
    task.setDueDate(LocalDate.now().minusDays(1));

    InvalidTaskException ex =
        assertThrows(InvalidTaskException.class, () -> taskService.createTask(task));

    assertTrue(ex.getMessage().contains("fecha límite"));
    verify(taskRepository, never()).save(any());
  }

  @Test
  @DisplayName("Al crear una tarea sin estado, se asigna PENDING por defecto")
  void createTask_withoutStatus_defaultsToPending() {
    Task task = new Task();
    task.setTitle("Nueva tarea");
    task.setPriority(TaskPriority.LOW);
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setStatus(null);

    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task created = taskService.createTask(task);

    assertEquals(TaskStatus.PENDING, created.getStatus());
    verify(taskRepository, times(1)).save(task);
  }

  @Test
  @DisplayName("Crear una tarea con fecha límite de hoy es válido (FutureOrPresent)")
  void createTask_withTodayAsDueDate_isAllowed() {
    Task task = new Task();
    task.setTitle("Tarea para hoy");
    task.setPriority(TaskPriority.MEDIUM);
    task.setDueDate(LocalDate.now());

    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task created = taskService.createTask(task);

    assertEquals(LocalDate.now(), created.getDueDate());
    verify(taskRepository, times(1)).save(task);
  }

  @Test
  @DisplayName("Obtener una tarea inexistente lanza ResourceNotFoundException")
  void getTaskById_notFound_throwsResourceNotFoundException() {
    when(taskRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
  }

  @Test
  @DisplayName("Actualizar una tarea inexistente lanza ResourceNotFoundException")
  void updateTask_notFound_throwsResourceNotFoundException() {
    when(taskRepository.findById(42L)).thenReturn(Optional.empty());

    Task updated = new Task();
    updated.setTitle("Título nuevo");
    updated.setPriority(TaskPriority.LOW);
    updated.setDueDate(LocalDate.now().plusDays(1));

    assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(42L, updated));
    verify(taskRepository, never()).save(any());
  }

  @Test
  @DisplayName("No se puede modificar una tarea que ya está completada")
  void updateTask_alreadyCompleted_throwsInvalidTaskException() {
    sampleTask.setStatus(TaskStatus.COMPLETED);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

    Task updated = new Task();
    updated.setTitle("Intento de cambio");
    updated.setPriority(TaskPriority.HIGH);
    updated.setDueDate(LocalDate.now().plusDays(3));

    InvalidTaskException ex =
        assertThrows(InvalidTaskException.class, () -> taskService.updateTask(1L, updated));

    assertTrue(ex.getMessage().contains("completada"));
    verify(taskRepository, never()).save(any());
  }

  @Test
  @DisplayName("Actualizar una tarea con fecha límite pasada lanza InvalidTaskException")
  void updateTask_withPastDueDate_throwsInvalidTaskException() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

    Task updated = new Task();
    updated.setTitle("Título nuevo");
    updated.setPriority(TaskPriority.HIGH);
    updated.setDueDate(LocalDate.now().minusDays(1));

    assertThrows(InvalidTaskException.class, () -> taskService.updateTask(1L, updated));
    verify(taskRepository, never()).save(any());
  }

  @Test
  @DisplayName("Actualizar una tarea válida modifica sus campos correctamente")
  void updateTask_valid_updatesFieldsCorrectly() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task updated = new Task();
    updated.setTitle("Título actualizado");
    updated.setDescription("Nueva descripción");
    updated.setPriority(TaskPriority.HIGH);
    updated.setDueDate(LocalDate.now().plusDays(10));
    updated.setStatus(TaskStatus.IN_PROGRESS);

    Task result = taskService.updateTask(1L, updated);

    assertEquals("Título actualizado", result.getTitle());
    assertEquals("Nueva descripción", result.getDescription());
    assertEquals(TaskPriority.HIGH, result.getPriority());
    assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
  }

  @Test
  @DisplayName("Eliminar una tarea inexistente lanza ResourceNotFoundException")
  void deleteTask_notFound_throwsResourceNotFoundException() {
    when(taskRepository.findById(7L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(7L));
    verify(taskRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Eliminar una tarea existente la borra del repositorio")
  void deleteTask_existing_deletesSuccessfully() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

    taskService.deleteTask(1L);

    verify(taskRepository, times(1)).delete(sampleTask);
  }

  @Test
  @DisplayName("Sin filtro de estado se devuelven todas las tareas")
  void getAllTasks_withoutStatus_returnsAllTasks() {
    Task other = new Task();
    other.setId(2L);
    other.setStatus(TaskStatus.IN_PROGRESS);
    when(taskRepository.findAll()).thenReturn(List.of(sampleTask, other));

    List<Task> result = taskService.getAllTasks(null);

    assertEquals(2, result.size());
    verify(taskRepository, never()).findByStatus(any());
  }

  @Test
  @DisplayName("Con filtro de estado solo se devuelven las tareas de ese estado")
  void getAllTasks_withStatus_returnsOnlyThatStatus() {
    when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(List.of(sampleTask));

    List<Task> result = taskService.getAllTasks(TaskStatus.PENDING);

    assertEquals(1, result.size());
    assertEquals(TaskStatus.PENDING, result.get(0).getStatus());
    verify(taskRepository, never()).findAll();
  }

  @Test
  @DisplayName("Filtrar por un estado sin tareas devuelve una lista vacía")
  void getAllTasks_withStatusWithoutMatches_returnsEmptyList() {
    when(taskRepository.findByStatus(TaskStatus.CANCELLED)).thenReturn(List.of());

    List<Task> result = taskService.getAllTasks(TaskStatus.CANCELLED);

    assertTrue(result.isEmpty());
  }
}
