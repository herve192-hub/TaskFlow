// 
import {    toast } from "react-toastify";


/** Shared notification helpers; the single ToastContainer is mounted in main.tsx. */
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
