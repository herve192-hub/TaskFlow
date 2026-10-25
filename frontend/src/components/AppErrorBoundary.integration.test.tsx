/**
 * Verify normal children and safe reload recovery for render failures. Runs React
 * in jsdom with a deliberately throwing child; checks private exception details
 * are not rendered. Does not test network retry or deployed chunk distribution.
 */

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import AppErrorBoundary from "./AppErrorBoundary";

afterEach(() => { cleanup(); vi.restoreAllMocks(); });

it("keeps working screens visible", () => {
  render(<AppErrorBoundary><p>Project workspace</p></AppErrorBoundary>);
  expect(screen.getByText("Project workspace")).toBeTruthy();
  expect(screen.queryByRole("alert")).toBeNull();
});

it("offers recovery after a page failure without showing internal exception details", () => {
  vi.spyOn(console, "error").mockImplementation(() => {});
  function BrokenPage(): never { throw new Error("synthetic-private-internal-detail"); }
  render(<AppErrorBoundary><BrokenPage /></AppErrorBoundary>);
  expect(screen.getByRole("heading", { name: "Unable to display TaskFlow" })).toBeTruthy();
  expect(screen.getByRole("button", { name: "Reload application" })).toBeTruthy();
  expect(screen.getByRole("alert").textContent).not.toContain("synthetic-private-internal-detail");
  expect(screen.queryByText("synthetic-private-internal-detail")).toBeNull();
});
