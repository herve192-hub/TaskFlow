export enum ProjectStatus {
  PLANNING = "PLANNING",
  ACTIVE = "ACTIVE",
  ON_HOLD = "ON_HOLD",
  COMPLETED = "COMPLETED",
  ARCHIVED = "ARCHIVED",
}

export enum ProjectVisibility {
  PUBLIC = "PUBLIC",
  TEAM = "TEAM",
  ORGANIZATION = "ORGANIZATION",
  PRIVATE = "PRIVATE",
}

export enum ProjectRole {
  OWNER = "OWNER",
  ADMIN = "ADMIN",
  MANAGER = "MANAGER",
  MEMBER = "MEMBER",
  GUEST = "GUEST",
}

export interface CreateProjectRequest {
  name: string;
  description?: string | null;
  visibility?: ProjectVisibility | null;
}

export interface UpdateProjectRequest {
  name?: string | null;
  description?: string | null;
  status?: ProjectStatus | null;
  visibility?: ProjectVisibility | null;
}

export interface AddProjectMemberRequest {
  userId: string;
  role: ProjectRole;
}

export interface UpdateProjectMemberRequest {
  role: ProjectRole;
}

export interface ProjectMemberResponse {
  id: string;
  userId: string;
  role: ProjectRole;
  joinedAt: string;
}

export interface ProjectSummaryResponse {
  id: string;
  name: string;
  description?: string | null;
  status: ProjectStatus;
  visibility: ProjectVisibility;
  ownerId: string;
  memberCount: number;
  createdAt: string;
  updatedAt: string;
}

export type ProjectSummary = ProjectSummaryResponse;

export interface ProjectResponse {
  id: string;
  name: string;
  description?: string | null;
  status: ProjectStatus;
  visibility: ProjectVisibility;
  ownerId: string;
  members: ProjectMemberResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ProjectAccessResponse {
  projectId: string;
  userId: string;
  member: boolean;
  role: ProjectRole;
  canView: boolean;
  canEdit: boolean;
  canManageMembers: boolean;
}
