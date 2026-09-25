package es.um.pc.tasky.service;

import es.um.pc.tasky.model.Task;

import java.util.List;

public interface TaskService {

    Task createTask(Task task);

    Task getTaskById(Long id);

    List<Task> getAllTasks();

    Task updateTask(Long id, Task updatedTask);

    void deleteTask(Long id);
}
