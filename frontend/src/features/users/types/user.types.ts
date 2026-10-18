/** id is the profile document ID; authUserId joins profiles to project members and signed-in accounts. */
export interface UserSummary {
  id: string;
  authUserId: string;
  firstName?: string | null;
  lastName?: string | null;
  fullName: string;
  email: string;
  avatarUrl?: string | null;
  status: "ACTIVE" | "INACTIVE" | "PENDING" | "SUSPENDED";
}

/** Spring Data's directory response uses number for its zero-based page index. */
export interface UserSearchPage {
  content: UserSummary[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface UserPreferences {
  timeZone: string;
  dateFormat: "MONTH_DAY_YEAR" | "DAY_MONTH_YEAR" | "YEAR_MONTH_DAY";
  startPage: "DASHBOARD" | "PROJECTS" | "TASKS" | "TEAMS";
  use24HourTime: boolean;
}

export interface UserProfile extends UserSummary {
  phone: string | null;
  bio: string | null;
  createdAt: string;
  updatedAt: string;
  preferences: UserPreferences;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string;
  bio: string;
}
