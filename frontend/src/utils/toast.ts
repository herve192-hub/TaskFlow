// 
import {    toast } from "react-toastify";


export const notify = {
    success: (message: string) =>
        toast.success(message),

    error: (message: string) =>
        toast.error(message),

    warning: (message: string) =>
        toast.warning(message),

    info: (message: string) =>
        toast.info(message),
};

export const isAuthenticated = () =>
    !!localStorage.getItem("accessToken");


export const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
}