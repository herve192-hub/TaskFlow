import {
  addProjectMember,
  createProject as createProjectApi,
  getProject,
  getProjectAccess,
  getProjectMembers,
  getProjects as getProjectsApi,
  removeProjectMember,
  updateProjectMember,
} from "../../../api/projectApi";

import type {
  CreateProjectRequest,
  PageResponse,
  ProjectSummaryResponse,
  ProjectMemberResponse,
} from "../types/project.types";

export const projectService = {
  getProject,
  getProjectAccess,
  getProjectMembers,
  addProjectMember,
  updateProjectMember,
  removeProjectMember,

  async getAllProjectMembers(projectId: string): Promise<ProjectMemberResponse[]> {
    const first = await getProjectMembers(projectId, 0, 100);
    const members = [...first.content];
    for (let page = 1; page < first.totalPages; page++) {
      members.push(...(await getProjectMembers(projectId, page, 100)).content);
    }
    return members;
  },

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
