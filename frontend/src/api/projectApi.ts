import api from "./axios";

import type {
  CreateProjectRequest,
  PageResponse,
  ProjectSummaryResponse,
} from "../features/projects/types/project.types";

export const getProjects = async (
  page = 0,
  size = 20
): Promise<PageResponse<ProjectSummaryResponse>> => {
  const response = await api.get<PageResponse<ProjectSummaryResponse>>(
    "/api/v1/projects",
    {
      params: {
        page,
        size,
      },
    }
  );

  return response.data;
};

export const createProject = async (
  request: CreateProjectRequest
): Promise<ProjectSummaryResponse> => {
  const response = await api.post<ProjectSummaryResponse>(
    "/api/v1/projects",
    request
  );

  return response.data;
};