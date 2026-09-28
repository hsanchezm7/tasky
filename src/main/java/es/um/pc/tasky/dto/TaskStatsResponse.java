package es.um.pc.tasky.dto;

import es.um.pc.tasky.model.TaskPriority;
import es.um.pc.tasky.model.TaskStatus;
import java.util.Map;

/**
 * DTO de salida con el resumen agregado de las tareas. Los mapas incluyen todos los valores de cada
 * enum, también los que tienen 0 tareas.
 */
public class TaskStatsResponse {

  private long total;
  private Map<TaskStatus, Long> byStatus;
  private Map<TaskPriority, Long> byPriority;
  private long overdue;

  public TaskStatsResponse() {}

  public TaskStatsResponse(
      long total,
      Map<TaskStatus, Long> byStatus,
      Map<TaskPriority, Long> byPriority,
      long overdue) {
    this.total = total;
    this.byStatus = byStatus;
    this.byPriority = byPriority;
    this.overdue = overdue;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public Map<TaskStatus, Long> getByStatus() {
    return byStatus;
  }

  public void setByStatus(Map<TaskStatus, Long> byStatus) {
    this.byStatus = byStatus;
  }

  public Map<TaskPriority, Long> getByPriority() {
    return byPriority;
  }

  public void setByPriority(Map<TaskPriority, Long> byPriority) {
    this.byPriority = byPriority;
  }

  public long getOverdue() {
    return overdue;
  }

  public void setOverdue(long overdue) {
    this.overdue = overdue;
  }
}
