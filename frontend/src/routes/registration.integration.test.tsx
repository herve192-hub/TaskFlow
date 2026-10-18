import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import AppRoutes from "./AppRoutes";

afterEach(() => {
  cleanup();
  vi.unstubAllEnvs();
  window.history.replaceState(null, "", "/");
});

it("redirects direct signup navigation and omits public onboarding links in a production build", async () => {
  vi.stubEnv("VITE_PUBLIC_REGISTRATION_ENABLED", "false");
  window.history.replaceState(null, "", "/signup");
  render(<AppRoutes />);
  expect(await screen.findByRole("button", { name: "Sign In" })).toBeTruthy();
  await waitFor(() => expect(window.location.pathname).toBe("/login"));
  expect(screen.getByText("Contact your administrator for access.")).toBeTruthy();
  expect(screen.queryByRole("link", { name: "Create one" })).toBeNull();
  expect(screen.queryByLabelText("First Name", { exact: true })).toBeNull();
});
