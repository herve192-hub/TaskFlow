import { changeTaskStatus, createTask, getProjectTasks } from "../../../api/taskApi";

/** Task feature API shared by project views, workspace aggregation, and creation/status controls. */
export const taskService = {
  getProjectTasks,
  changeTaskStatus,
  createTask,
};
