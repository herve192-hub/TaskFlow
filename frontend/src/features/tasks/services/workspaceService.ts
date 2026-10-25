/**
 * Assemble a complete workspace snapshot from all project pages and all unarchived
 * task pages. Project task lists run in batches of four to bound parallel traffic;
 * the combined result is sorted newest first for dashboard and Tasks consumers.
 * Any failed page rejects the snapshot so totals never silently reflect partial
 * data. This in-browser aggregation grows with workspace size; server aggregates
 * would be needed before using it for very large accounts.
 */

import { projectService } from "../../projects/services/projectService";
import { taskService } from "./taskService";
import type { PageResponse, ProjectSummary } from "../../projects/types/project.types";
import type { TaskSummaryResponse } from "../types/task.types";

export interface WorkspaceData {
  projects: ProjectSummary[];
  tasks: TaskSummaryResponse[];
}

/** Read every zero-based server page so aggregate counts are not limited to the first result page. */
async function allPages<T>(fetchPage: (page: number) => Promise<PageResponse<T>>): Promise<T[]> {
  const first = await fetchPage(0);
  const items = [...first.content];
  for (let page = 1; page < first.totalPages; page++) {
    items.push(...(await fetchPage(page)).content);
  }
  return items;
}

/** Build a complete project/task snapshot; any failed request rejects the aggregate rather than hiding it. */
export async function loadWorkspace(): Promise<WorkspaceData> {
  const projects = await allPages((page) => projectService.getProjects(page, 100));
  const tasks: TaskSummaryResponse[] = [];
  // Limit concurrent requests when an account belongs to many projects.
  for (let start = 0; start < projects.length; start += 4) {
    const results = await Promise.all(projects.slice(start, start + 4).map((project) =>
      allPages((page) => taskService.getProjectTasks(project.id, page, 100))));
    tasks.push(...results.flat());
  }
  // Consumers can take the first items for recent-task lists regardless of project fetch order.
  tasks.sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt));
  return { projects, tasks };
}
