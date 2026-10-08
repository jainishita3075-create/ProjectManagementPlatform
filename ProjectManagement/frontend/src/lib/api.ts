import { ApiResponse } from "@/types";

const rawBaseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
const BASE_URL = rawBaseUrl.replace(/\/$/, "");

export async function apiFetch<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<ApiResponse<T>> {
    const token = typeof window !== "undefined" ? localStorage.getItem("token") : null;

    const headers: Record<string, string> = {
        "Content-Type": "application/json",
        ...(options.headers as Record<string, string>),
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            ...options,
            headers,
        });

        // If session expired or unauthorized, clear token and redirect to login
        if (response.status === 401 && typeof window !== "undefined") {
            localStorage.removeItem("token");
            localStorage.removeItem("username");
            window.location.href = "/login";
            throw new Error("Session expired. Please log in again.");
        }

        const text = await response.text();
        let data: any = null;
        try {
            data = text ? JSON.parse(text) : null;
        } catch {
            data = text;
        }

        if (!response.ok || (data && typeof data === "object" && data.success === false)) {
            const msg = (data && typeof data === "object" && data.message) || `Server error (${response.status})`;
            throw new Error(msg);
        }

        return data as ApiResponse<T>;
    } catch (err: any) {
        if (err.name === "TypeError" && err.message.includes("fetch")) {
            throw new Error("Cannot connect to server. Please ensure Spring Boot backend is running on port 8080.");
        }
        throw err;
    }
}
