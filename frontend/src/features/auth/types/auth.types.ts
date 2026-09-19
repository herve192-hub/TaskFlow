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
    createdAt: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType?: string;
    email?: string;
    user?: AuthUser;
}

export const authStorage = {
    saveTokens(accessToken: string, refreshToken: string) {
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);
    },

    clearToken() {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
    },

    getAccessToken() {
        return localStorage.getItem("accessToken");
    },

    isAuthenticated() {
        return !!localStorage.getItem("accessToken");
    },
};

export const isAuthenticated = () => !!localStorage.getItem("accessToken");

export const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
};