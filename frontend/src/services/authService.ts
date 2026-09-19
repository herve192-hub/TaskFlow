import {
    LoginRequest,
    SignupRequest,
    AuthResponse,
} from "../features/auth/types/auth";

const delay = (ms: number) =>
    new Promise((resolve) => setTimeout(resolve, ms));

export const authService = {
    async login(data: LoginRequest): Promise<AuthResponse> {
        await delay(1500);

        if (
            data.email === "admin@taskflow.com" &&
            data.password === "password123"
        ) {
            return {
                accessToken: "mock-access-token",
                refreshToken: "mock-refresh-token",
                tokenType: "Bearer",
                email: data.email,
                user: {
                    id: "1",
                    firstname: "Admin",
                    lastname: "User",
                    username: "admin",
                    email: data.email,
                    roles: ["ADMIN"],
                    provider: "local",
                    emailVerified: true,
                    createdAt: new Date().toISOString(),
                },
            };
        }

        throw new Error("Invalid email or password");
    },

    async signup(data: SignupRequest): Promise<void> {
        await delay(1500);
        console.log("New User", data);
    },

    async forgotPassword(email: string): Promise<void> {
        await delay(1000);
        console.log("Reset email sent to:", email);
    },
};