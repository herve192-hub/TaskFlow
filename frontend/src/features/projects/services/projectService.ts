import api from "../../../api/axios";

import type {
  AddProjectMemberRequest,
  CreateProjectRequest,
  PageResponse,
  ProjectAccessResponse,
  ProjectMemberResponse,
  ProjectResponse,
  ProjectSummaryResponse,
  UpdateProjectMemberRequest,
} from "../types/project.types";

/** Fetch one zero-based page of the signed-in account's project memberships. */
const getProjects = async (
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

const createProject = async (
  request: CreateProjectRequest
): Promise<ProjectSummaryResponse> => {
  const response = await api.post<ProjectSummaryResponse>(
    "/api/v1/projects",
    request
  );

  return response.data;
};

// Encode identifiers as individual path segments rather than allowing them to alter route structure.
const projectPath = (projectId: string) => `/api/v1/projects/${encodeURIComponent(projectId)}`;

const getProject = async (projectId: string): Promise<ProjectResponse> => {
  return (await api.get<ProjectResponse>(projectPath(projectId))).data;
};

const getProjectAccess = async (projectId: string): Promise<ProjectAccessResponse> => {
  return (await api.get<ProjectAccessResponse>(`${projectPath(projectId)}/access`)).data;
};

const getProjectMembers = async (
  projectId: string, page = 0, size = 20,
): Promise<PageResponse<ProjectMemberResponse>> => {
  return (await api.get<PageResponse<ProjectMemberResponse>>(`${projectPath(projectId)}/members`, {
    params: { page, size, sort: "joinedAt,asc", },
  })).data;
};

const addProjectMember = async (
  projectId: string, request: AddProjectMemberRequest,
): Promise<ProjectMemberResponse> => {
  return (await api.post<ProjectMemberResponse>(`${projectPath(projectId)}/members`, request)).data;
};

/** memberId identifies the membership document, not the profile or auth account. */
const updateProjectMember = async (
  projectId: string, memberId: string, request: UpdateProjectMemberRequest,
): Promise<ProjectMemberResponse> => {
  return (await api.put<ProjectMemberResponse>(
    `${projectPath(projectId)}/members/${encodeURIComponent(memberId)}`, request,
  )).data;
};

const removeProjectMember = async (projectId: string, memberId: string): Promise<void> => {
  await api.delete(`${projectPath(projectId)}/members/${encodeURIComponent(memberId)}`);
};

/** Project HTTP operations share one feature boundary for all callers. */
export const projectService = {
  getProjects, createProject, getProject, getProjectAccess,
  getProjectMembers, addProjectMember, updateProjectMember, removeProjectMember,

  /** Exhaust all pages so the add-member dialog excludes existing users beyond the first page. */
  async getAllProjectMembers(projectId: string): Promise<ProjectMemberResponse[]> {
    const first = await getProjectMembers(projectId, 0, 100);
    const members = [...first.content];
    for (let page = 1; page < first.totalPages; page++) {
      members.push(...(await getProjectMembers(projectId, page, 100)).content);
    }
    return members;
  },
};
