import {
  createProject as createProjectApi,
  getProjects as getProjectsApi,
} from "../../../api/projectApi";

import type {
  CreateProjectRequest,
  PageResponse,
  ProjectSummaryResponse,
} from "../types/project.types";

export const projectService = {
  async getProjects(
    page = 0,
    size = 20
  ): Promise<PageResponse<ProjectSummaryResponse>> {
    return getProjectsApi(page, size);
  },

  async createProject(
    request: CreateProjectRequest
  ): Promise<ProjectSummaryResponse> {
    return createProjectApi(request);
  },
};