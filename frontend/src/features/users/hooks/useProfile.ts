import { useEffect, useState } from "react";
import { userService } from "../services/userService";
import type { UserProfile } from "../types/user.types";
import { apiErrorMessage } from "../../../utils/apiError";

/** Load private profile data on demand, with an explicit retry and no late view updates. */
export function useProfile() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    let active = true;
    userService.getCurrentProfile().then(result => {
      if (active) setProfile(result);
    }).catch(error => {
      if (active) setError(apiErrorMessage(error, "Unable to load your profile. Please try again."));
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [revision]);
  return { profile, loading, error, reload: () => { setError(null); setLoading(true); setRevision(value => value + 1); } };
}
