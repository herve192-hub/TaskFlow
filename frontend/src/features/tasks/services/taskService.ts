import { changeTaskStatus, createTask, getProjectTasks } from "../../../api/taskApi";

export const taskService = {
  getProjectTasks,
  changeTaskStatus,
  createTask,
};
