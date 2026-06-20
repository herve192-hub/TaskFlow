// 
// 
export interface LoginRequest {
    email: string;
    password: string;
    rememberMe: boolean;
}

export interface SignupRequest {
    fullName: string;
    email: string;
    password: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    email: string;
}

export const authStorage = {
    saveTokens( 
        accessToken: string,
        refreshToken: string
    ) {
        localStorage.setItem(
            "accessToken",
            accessToken
        );

        localStorage.setItem(
            "refreshToken",
            refreshToken
        );
    },

    clearToken() {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
    },

    getAccessToken() {
        return localStorage.getItem("accessToken");
    },
    isAuthenticated() {
        return !!localStorage.getItem(
            "accessToken"
        );
    },
};

// export const isAuthenticated = () =>
//     !!localStorage.getItem("accessToken");

// export const logout = () => {
//         localStorage.removeItem("accessToken");
//         localStorage.removeItem("refreshToken");
// }