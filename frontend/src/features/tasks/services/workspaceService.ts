import { projectService } from "../../projects/services/projectService";
import { taskService } from "./taskService";
import type { PageResponse, ProjectSummary } from "../../projects/types/project.types";
import type { TaskSummaryResponse } from "../types/task.types";

export interface WorkspaceData {
  projects: ProjectSummary[];
  tasks: TaskSummaryResponse[];
}

async function allPages<T>(fetchPage: (page: number) => Promise<PageResponse<T>>): Promise<T[]> {
  const first = await fetchPage(0);
  const items = [...first.content];
  for (let page = 1; page < first.totalPages; page++) {
    items.push(...(await fetchPage(page)).content);
  }
  return items;
}

export async function loadWorkspace(): Promise<WorkspaceData> {
  const projects = await allPages((page) => projectService.getProjects(page, 100));
  const tasks: TaskSummaryResponse[] = [];
  // Limit concurrent requests when an account belongs to many projects.
  for (let start = 0; start < projects.length; start += 4) {
    const results = await Promise.all(projects.slice(start, start + 4).map((project) =>
      allPages((page) => taskService.getProjectTasks(project.id, page, 100))));
    tasks.push(...results.flat());
  }
  tasks.sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt));
  return { projects, tasks };
}
