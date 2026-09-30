package es.um.pc.tasky.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.um.pc.tasky.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class TaskAuditTimestampTest {

  @Autowired private TaskRepository taskRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("Al crear una tarea, se asignan createdAt y updatedAt automáticamente")
  void createTask_setsAuditTimestamps() {
    Task task = buildTask();

    Task savedTask = taskRepository.saveAndFlush(task);

    assertNotNull(savedTask.getCreatedAt());
    assertNotNull(savedTask.getUpdatedAt());
    assertFalse(savedTask.getUpdatedAt().isBefore(savedTask.getCreatedAt()));
  }

  @Test
  @DisplayName("Al modificar una tarea, updatedAt cambia y createdAt permanece igual")
  void updateTask_updatesUpdatedAtWithoutChangingCreatedAt() throws InterruptedException {
    Task savedTask = taskRepository.saveAndFlush(buildTask());
    LocalDateTime createdAt = savedTask.getCreatedAt();
    LocalDateTime initialUpdatedAt = savedTask.getUpdatedAt();

    entityManager.clear();
    Thread.sleep(10);

    Task taskToUpdate = taskRepository.findById(savedTask.getId()).orElseThrow();
    taskToUpdate.setTitle("Título actualizado");
    taskRepository.saveAndFlush(taskToUpdate);

    entityManager.clear();
    Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

    assertEquals(createdAt, updatedTask.getCreatedAt());
    assertNotNull(updatedTask.getUpdatedAt());
    assertTrue(updatedTask.getUpdatedAt().isAfter(initialUpdatedAt));
  }

  private Task buildTask() {
    Task task = new Task();
    task.setTitle("Tarea de auditoría");
    task.setDescription("Tarea para comprobar las marcas temporales");
    task.setStatus(TaskStatus.PENDING);
    task.setPriority(TaskPriority.MEDIUM);
    task.setDueDate(LocalDate.now().plusDays(1));
    return task;
  }
}
