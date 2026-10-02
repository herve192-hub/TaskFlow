import api from "./axios";

import type {
  AddProjectMemberRequest,
  CreateProjectRequest,
  PageResponse,
  ProjectAccessResponse,
  ProjectMemberResponse,
  ProjectResponse,
  ProjectSummaryResponse,
  UpdateProjectMemberRequest,
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

const projectPath = (projectId: string) => `/api/v1/projects/${encodeURIComponent(projectId)}`;

export const getProject = async (projectId: string): Promise<ProjectResponse> => {
  return (await api.get<ProjectResponse>(projectPath(projectId))).data;
};

export const getProjectAccess = async (projectId: string): Promise<ProjectAccessResponse> => {
  return (await api.get<ProjectAccessResponse>(`${projectPath(projectId)}/access`)).data;
};

export const getProjectMembers = async (
  projectId: string, page = 0, size = 20,
): Promise<PageResponse<ProjectMemberResponse>> => {
  return (await api.get<PageResponse<ProjectMemberResponse>>(`${projectPath(projectId)}/members`, {
    params: { page, size, sort: "joinedAt,asc", },
  })).data;
};

export const addProjectMember = async (
  projectId: string, request: AddProjectMemberRequest,
): Promise<ProjectMemberResponse> => {
  return (await api.post<ProjectMemberResponse>(`${projectPath(projectId)}/members`, request)).data;
};

export const updateProjectMember = async (
  projectId: string, memberId: string, request: UpdateProjectMemberRequest,
): Promise<ProjectMemberResponse> => {
  return (await api.put<ProjectMemberResponse>(
    `${projectPath(projectId)}/members/${encodeURIComponent(memberId)}`, request,
  )).data;
};

export const removeProjectMember = async (projectId: string, memberId: string): Promise<void> => {
  await api.delete(`${projectPath(projectId)}/members/${encodeURIComponent(memberId)}`);
};
