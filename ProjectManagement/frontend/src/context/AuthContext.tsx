"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";

interface AuthContextType {
    token: string | null;
    username: string | null;
    login: (token: string, username: string) => void;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [token, setToken] = useState<string | null>(null);
    const [username, setUsername] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
        const savedToken = localStorage.getItem("token");
        const savedUsername = localStorage.getItem("username");
        if (savedToken && savedUsername) {
            setToken(savedToken);
            setUsername(savedUsername);
        }
        setLoading(false);
    }, []);

    const login = (newToken: string, newUsername: string) => {
        localStorage.setItem("token", newToken);
        localStorage.setItem("username", newUsername);
        setToken(newToken);
        setUsername(newUsername);
        router.push("/dashboard");
    };

    const logout = async () => {
        try {
            await apiFetch("/api/logout", { method: "POST" });
        } catch {
            // Ignore errors on logout
        }
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        setToken(null);
        setUsername(null);
        router.push("/login");
    };

    if (loading) {
        return <div className="h-screen flex items-center justify-center bg-slate-950 text-white">Loading...</div>;
    }

    return (
        <AuthContext.Provider value={{ token, username, login, logout, isAuthenticated: !!token }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error("useAuth must be used within an AuthProvider");
    return context;
}
