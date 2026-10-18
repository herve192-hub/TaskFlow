import { act, cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { ThemeProvider } from "@mui/material/styles";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { projectService } from "../features/projects/services/projectService";
import { ProjectStatus, ProjectVisibility } from "../features/projects/types/project.types";
import { theme } from "../theme/theme";
import Projects from "./Projects";

vi.mock("../features/projects/services/projectService", () => ({
  projectService: { getProjects: vi.fn(), createProject: vi.fn() },
}));

const project = {
  id: "project-1", name: "Portfolio demo", description: "Keep this description",
  status: ProjectStatus.PLANNING, visibility: ProjectVisibility.PRIVATE,
  ownerId: "account-1", memberCount: 1,
  createdAt: "2026-01-01T00:00:00Z", updatedAt: "2026-01-01T00:00:00Z",
};
const emptyPage = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true };

async function openCreateForm() {
  render(<MemoryRouter><ThemeProvider theme={theme}><Projects /></ThemeProvider></MemoryRouter>);
  await screen.findByText("No projects yet");
  fireEvent.click(screen.getAllByRole("button", { name: "Create Project" })[0]);
  const dialog = within(screen.getByRole("dialog", { name: "Create Project" }));
  fireEvent.change(dialog.getByLabelText("Project name", { exact: false }), { target: { value: project.name } });
  fireEvent.change(dialog.getByLabelText("Description"), { target: { value: project.description } });
  return dialog;
}

describe("project creation", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(projectService.getProjects).mockResolvedValue(emptyPage);
  });
  afterEach(cleanup);

  it("preserves the form after a rejected request and refreshes the list after a successful retry", async () => {
    vi.mocked(projectService.createProject).mockRejectedValueOnce(new Error("offline")).mockResolvedValue(project);
    const dialog = await openCreateForm();
    fireEvent.click(dialog.getByRole("button", { name: "Create Project" }));
    expect((await dialog.findByRole("alert")).textContent).toContain("Unable to create project");
    expect((dialog.getByLabelText("Project name", { exact: false }) as HTMLInputElement).value).toBe(project.name);
    expect((dialog.getByLabelText("Description") as HTMLInputElement).value).toBe(project.description);

    vi.mocked(projectService.getProjects).mockResolvedValue({ ...emptyPage, content: [project], totalElements: 1, totalPages: 1 });
    fireEvent.click(dialog.getByRole("button", { name: "Create Project" }));
    expect(await screen.findByRole("heading", { name: project.name })).toBeTruthy();
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    expect(projectService.createProject).toHaveBeenCalledTimes(2);
    expect(projectService.createProject).toHaveBeenLastCalledWith({
      name: project.name, description: project.description, visibility: ProjectVisibility.PRIVATE,
    });
    expect(projectService.getProjects).toHaveBeenCalledTimes(2);
  });

  it("allows only one pending creation and keeps the form open until the request settles", async () => {
    let complete!: (value: typeof project) => void;
    vi.mocked(projectService.createProject).mockReturnValue(new Promise(resolve => { complete = resolve; }));
    const dialog = await openCreateForm();
    const submit = dialog.getByRole("button", { name: "Create Project" });
    fireEvent.click(submit);
    fireEvent.click(submit);
    fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });
    expect(projectService.createProject).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("dialog")).toBeTruthy();
    expect((dialog.getByRole("button", { name: "Cancel" }) as HTMLButtonElement).disabled).toBe(true);
    await act(async () => { complete(project); });
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    fireEvent.click(screen.getAllByRole("button", { name: "Create Project" })[0]);
    expect((screen.getByLabelText("Project name", { exact: false }) as HTMLInputElement).value).toBe("");
  });
});
