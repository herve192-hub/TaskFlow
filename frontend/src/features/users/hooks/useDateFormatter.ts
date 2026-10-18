import { useCurrentUser } from "../../auth/hooks/useCurrentUser";
import { formatAccountDate } from "../utils/preferences";

export function useDateFormatter() {
  const preferences = useCurrentUser()?.preferences;
  return (value: string, includeTime = false) => formatAccountDate(value, preferences, includeTime);
}
