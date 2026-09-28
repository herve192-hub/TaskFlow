import { useEffect, useState } from "react";
import axios from "axios";
import { loadWorkspace } from "../services/workspaceService";
import type { WorkspaceData } from "../services/workspaceService";

export function useWorkspace() {
  const [data, setData] = useState<WorkspaceData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    let active = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const result = await loadWorkspace();
        if (active) setData(result);
      } catch (error) {
        if (active) setError(axios.isAxiosError(error) && typeof error.response?.data?.message === "string"
          ? error.response.data.message : "Unable to load your projects and tasks. Please try again.");
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [revision]);
  return { data, loading, error, refresh: () => { setLoading(true); setRevision((value) => value + 1); } };
}
