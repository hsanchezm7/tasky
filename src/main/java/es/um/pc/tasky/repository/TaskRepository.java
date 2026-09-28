package es.um.pc.tasky.repository;

import es.um.pc.tasky.model.Task;
import es.um.pc.tasky.model.TaskPriority;
import es.um.pc.tasky.model.TaskStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByStatus(TaskStatus status);

  long countByStatus(TaskStatus status);

  long countByPriority(TaskPriority priority);

  long countByDueDateBeforeAndStatusNotIn(LocalDate date, Collection<TaskStatus> statuses);
}
