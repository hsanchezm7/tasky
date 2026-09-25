package es.um.pc.tasky.service;

import es.um.pc.tasky.model.Task;
import es.um.pc.tasky.model.TaskStatus;
import java.util.List;

public interface TaskService {

  Task createTask(Task task);

  Task getTaskById(Long id);

  /**
   * Devuelve las tareas, opcionalmente filtradas por estado.
   *
   * @param status estado por el que filtrar; si es {@code null} se devuelven todas
   */
  List<Task> getAllTasks(TaskStatus status);

  Task updateTask(Long id, Task updatedTask);

  void deleteTask(Long id);
}
