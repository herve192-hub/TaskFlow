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
