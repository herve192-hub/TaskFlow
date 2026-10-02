import axios from "axios";
import { createCurrentUser, getCurrentUser, lookupUsers, searchUsers } from "../../../api/userApi";
import type { AuthUser } from "../../auth/types/auth.types";

export const userService = {
  searchUsers,
  lookupUsers,

  async ensureCurrentProfile(user: AuthUser): Promise<void> {
    try {
      await getCurrentUser();
      return;
    } catch (error) {
      if (!axios.isAxiosError(error) || error.response?.status !== 404) throw error;
    }

    try {
      await createCurrentUser({ firstName: user.firstname, lastName: user.lastname, email: user.email });
    } catch (error) {
      // Another sign-in may have created the same profile while this request was in flight.
      if (!axios.isAxiosError(error) || ![400, 409].includes(error.response?.status ?? 0)) throw error;
      await getCurrentUser();
    }
  },
};
