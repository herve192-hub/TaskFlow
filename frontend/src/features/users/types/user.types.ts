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

export interface UserSearchPage {
  content: UserSummary[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
