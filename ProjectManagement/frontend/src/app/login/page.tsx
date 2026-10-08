"use client";

import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Lock, User, Mail, AlertCircle, CheckCircle2, ArrowRight, Eye, EyeOff } from "lucide-react";

const rawBaseUrl = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/\/$/, "");

export default function LoginPage() {
    const { login } = useAuth();

    // Login Form State
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const res = await fetch(`${rawBaseUrl}/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            const text = await res.text();
            let data: any = null;

            try {
                data = text ? JSON.parse(text) : null;
            } catch {
                data = text;
            }

            if (!res.ok) {
                const errorMsg =
                    (data && typeof data === "object" && data.message) ||
                    (typeof data === "string" && data.trim()) ||
                    "Invalid username or password";
                throw new Error(errorMsg);
            }

            if (data && typeof data === "object" && data.success === false) {
                throw new Error(data.message || "Invalid username or password");
            }

            const token =
                typeof data === "string"
                    ? data
                    : data && typeof data === "object"
                        ? data.data
                        : null;

            if (!token || token === "fail") {
                throw new Error("Invalid username or password");
            }

            login(token, username);
        } catch (err: any) {
            if (err.name === "TypeError" && err.message.includes("fetch")) {
                setError(
                    `Cannot connect to backend server at ${rawBaseUrl}. Please ensure your backend is running and CORS allows this domain.`
                );
            } else {
                setError(err.message || "Failed to login. Please check your credentials.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#0f1115] text-zinc-200 flex items-center justify-center p-4">
            <div className="w-full max-w-md bg-[#16181d] border border-zinc-800/80 p-8 rounded-2xl shadow-xl">
                <div className="text-center mb-6">
                    <div className="w-10 h-10 mx-auto rounded-xl bg-zinc-800 border border-zinc-700/60 flex items-center justify-center text-zinc-200 font-semibold text-sm mb-3">
                        P
                    </div>
                    <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">
                        Welcome back
                    </h1>
                    <p className="text-xs text-zinc-400 mt-1">
                        Sign in to your team workspace
                    </p>
                </div>

                {error && (
                    <div className="mb-6 p-3 bg-rose-950/30 border border-rose-900/50 rounded-xl flex items-center gap-3 text-rose-300 text-xs">
                        <AlertCircle className="w-4 h-4 shrink-0" />
                        <span>{error}</span>
                    </div>
                )}

                <form onSubmit={handleLogin} className="space-y-4">
                    <div>
                        <label className="block text-xs font-medium text-zinc-400 mb-1.5">Username</label>
                        <div className="relative">
                            <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                            <input
                                type="text"
                                required
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Enter username"
                                className="w-full bg-[#111317] border border-zinc-800 rounded-xl py-2.5 pl-10 pr-4 text-sm text-zinc-200 placeholder:text-zinc-600 focus:outline-none focus:border-zinc-600 transition-colors"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-medium text-zinc-400 mb-1.5">Password</label>
                        <div className="relative">
                            <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                            <input
                                type={showPassword ? "text" : "password"}
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                className="w-full bg-[#111317] border border-zinc-800 rounded-xl py-2.5 pl-10 pr-10 text-sm text-zinc-200 placeholder:text-zinc-600 focus:outline-none focus:border-zinc-600 transition-colors"
                            />
                            <button
                                type="button"
                                onClick={() => setShowPassword(!showPassword)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 p-1 cursor-pointer"
                            >
                                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                            </button>
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full mt-2 bg-zinc-200 hover:bg-white text-zinc-900 font-medium py-2.5 px-4 rounded-xl flex items-center justify-center gap-2 shadow-sm transition-all disabled:opacity-50 cursor-pointer text-sm"
                    >
                        {loading ? "Signing in..." : "Sign In"}
                        {!loading && <ArrowRight className="w-4 h-4" />}
                    </button>
                </form>
            </div>
        </div>
    );
}
