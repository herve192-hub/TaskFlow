import api from "./axios";
import type {
  CreateTaskRequest, PageResponse, TaskResponse, TaskSummaryResponse,
} from "../features/tasks/types/task.types";

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
