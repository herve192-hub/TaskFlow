/**
 * UI labels, descriptions, and roles assignable through membership dialogs.
 * OWNER is deliberately excluded: ordinary member edits do not transfer ownership.
 * Keep enum spellings aligned with project.types.ts and backend permission policy;
 * these options describe controls and cannot grant server-side privileges.
 */

import { ProjectRole } from "../types/project.types";

// Ownership is established at project creation and cannot be assigned through generic membership edits.
export const assignableProjectRoles = [
  ProjectRole.ADMIN, ProjectRole.MANAGER, ProjectRole.MEMBER, ProjectRole.GUEST,
];

export const projectRoleLabels: Record<ProjectRole, string> = {
  OWNER: "Owner", ADMIN: "Admin", MANAGER: "Manager", MEMBER: "Member", GUEST: "Guest",
};

export const projectRoleDescriptions: Record<ProjectRole, string> = {
  OWNER: "Owns the project and manages its team.",
  ADMIN: "Manages the team, project details, and tasks.",
  MANAGER: "Manages project details and tasks.",
  MEMBER: "Contributes to project tasks.",
  GUEST: "Views the project and its tasks.",
};
