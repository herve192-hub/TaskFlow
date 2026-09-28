export type { PageResponse } from "../../projects/types/project.types";

export enum TaskStatus {
  TODO = "TODO",
  IN_PROGRESS = "IN_PROGRESS",
  IN_REVIEW = "IN_REVIEW",
  BLOCKED = "BLOCKED",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED",
}

export enum TaskPriority {
  LOW = "LOW",
  MEDIUM = "MEDIUM",
  HIGH = "HIGH",
  CRITICAL = "CRITICAL",
}

export enum TaskType {
  TASK = "TASK",
  BUG = "BUG",
  FEATURE = "FEATURE",
  IMPROVEMENT = "IMPROVEMENT",
  EPIC = "EPIC",
}

export interface CreateTaskRequest {
  projectId: string;
  title: string;
  description?: string | null;
  priority: TaskPriority;
  type: TaskType;
  assigneeId?: string | null;
  parentTaskId?: string | null;
  sprintId?: string | null;
  dueDate?: string | null;
  estimatedHours?: number | null;
  tags?: string[] | null;
}

export interface TaskSummaryResponse {
  id: string;
  projectId: string;
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  type: TaskType;
  assigneeId: string | null;
  dueDate: string | null;
  createdAt: string;
}

export interface TaskResponse extends TaskSummaryResponse {
  createdBy: string;
  description: string | null;
  parentTaskId: string | null;
  sprintId: string | null;
  estimatedHours: number | null;
  actualHours: number | null;
  tags: string[] | null;
  archived: boolean;
  updatedAt: string;
  completedAt: string | null;
}
