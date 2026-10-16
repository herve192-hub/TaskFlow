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

/** Roles scoped to a single project, distinct from account-level ROLE_USER/ROLE_ADMIN authorities. */
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

/** id targets membership edits; userId is the canonical auth account used for directory lookups. */
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

/** Project/task service pagination uses zero-based page; user-directory responses instead use number. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Server-calculated permissions guide visible controls; the backend rechecks each requested operation. */
export interface ProjectAccessResponse {
  projectId: string;
  userId: string;
  member: boolean;
  role: ProjectRole;
  canView: boolean;
  canEdit: boolean;
  canManageMembers: boolean;
}
