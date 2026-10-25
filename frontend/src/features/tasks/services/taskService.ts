/**
 * Task API adapter used by project pages and workspace aggregation. Encodes path
 * IDs, requests server-paginated project tasks in creation order, and sends explicit
 * status patches rather than embedding lifecycle changes in generic updates.
 * Backend project membership and TaskLifecyclePolicy decide which writes succeed.
 */

import api from "../../../api/axios";
import type {
  CreateTaskRequest, PageResponse, TaskResponse, TaskSummaryResponse, TaskStatus,
} from "../types/task.types";

/** Read one server-paginated page of unarchived tasks for a project the caller can access. */
const getProjectTasks = async (
  projectId: string,
  page = 0,
  size = 20,
): Promise<PageResponse<TaskSummaryResponse>> => {
  const response = await api.get<PageResponse<TaskSummaryResponse>>(
    `/api/v1/tasks/project/${encodeURIComponent(projectId)}`,
    { params: { page, size, sort: "createdAt,desc" } },
  );
  return response.data;
};

const createTask = async (request: CreateTaskRequest): Promise<TaskResponse> => {
  const response = await api.post<TaskResponse>("/api/v1/tasks", request);
  return response.data;
};

/** The backend validates both the lifecycle edge and the caller's project/task permissions. */
const changeTaskStatus = async (taskId: string, status: TaskStatus): Promise<TaskResponse> => {
  const response = await api.patch<TaskResponse>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/status`, { status },
  );
  return response.data;
};

/** Task operations shared by project views, workspace aggregation, and creation/status controls. */
export const taskService = { getProjectTasks, changeTaskStatus, createTask };
