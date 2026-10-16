import api from "./axios";
import type {
  CreateTaskRequest, PageResponse, TaskResponse, TaskSummaryResponse, TaskStatus,
} from "../features/tasks/types/task.types";

/** Read one server-paginated page of unarchived tasks for a project the caller can access. */
export const getProjectTasks = async (
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

export const createTask = async (request: CreateTaskRequest): Promise<TaskResponse> => {
  const response = await api.post<TaskResponse>("/api/v1/tasks", request);
  return response.data;
};

/** The backend validates both the lifecycle edge and the caller's project/task permissions. */
export const changeTaskStatus = async (taskId: string, status: TaskStatus): Promise<TaskResponse> => {
  const response = await api.patch<TaskResponse>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/status`, { status },
  );
  return response.data;
};
