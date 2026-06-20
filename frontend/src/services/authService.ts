import {
    LoginRequest,
    SignupRequest,
    AuthResponse,
} from "../types/auth"


const delay = (ms: number) =>
    new Promise((resolve) =>setTimeout(resolve, ms));


export const authService = {
    async login(
        data: LoginRequest
    ): Promise<AuthResponse> {
        await delay(1500);

        if (
            data.email ==="admin@taskflow.com" && 
            data.password ==="password123"
        ) {
            return {
                accessToken: "mock-access-token",
                refreshToken: "mock-refresh-token",
                email: data.email,
            };
        }
        throw new Error(
            "Invalid email or password"
        );
    },

    async signup(
        data: SignupRequest
    ): Promise<void> {
        await delay(1500);

        console.log("New User", data);
    },

    async forgotPassword(
        email: string
    ): Promise<void> {
        await delay(1000);

        console.log(
            "Reset email sent to:", email
        );
    },
};