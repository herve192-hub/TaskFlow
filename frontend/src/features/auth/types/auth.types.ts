/**
 * Browser contracts for auth-service requests, token envelopes, and display identity.
 * AuthUser.id is the canonical account ID shared with project/task authorization.
 * firstname/lastname reflect auth JSON spelling; user-service profiles use camelCase.
 * AuthResponse describes the wire format; session persistence must copy only user
 * metadata, never accessToken or refreshToken.
 */

import type { UserPreferences } from "../../users/types/user.types";

export interface LoginRequest {
    email: string;
    password: string;
}

export interface SignupRequest {
    firstname?: string;
    lastname?: string;
    username?: string;
    fullName?: string;
    email: string;
    password: string;
}

export type RegisterRequest = SignupRequest;

/** Credential-account metadata; id is the canonical JWT subject used for memberships and assignments. */
export interface AuthUser {
    id: string;
    firstname: string;
    lastname: string;
    username: string;
    email: string;
    roles: string[];
    provider: string;
    emailVerified: boolean;
    profileImage?: string | null;
    preferences?: UserPreferences;
    createdAt: string;
}

/** API token envelope; browser code uses HttpOnly authentication cookies and persists only user metadata. */
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType?: string;
    email?: string;
    user?: AuthUser;
}
