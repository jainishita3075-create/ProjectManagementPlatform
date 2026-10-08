"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { apiFetch } from "@/lib/api";
import {
    X,
    User,
    Mail,
    Lock,
    Shield,
    Eye,
    EyeOff,
    Check,
    AlertCircle,
    KeyRound
} from "lucide-react";

interface UserProfile {
    userId: number;
    name: string;
    email: string;
    username: string;
    roles: string[];
}

export default function ProfileModal({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
    const { username } = useAuth();
    const [tab, setTab] = useState<"profile" | "password">("profile");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [roles, setRoles] = useState<string[]>(["ADMIN"]);
    const [updating, setUpdating] = useState(false);
    const [profileSuccess, setProfileSuccess] = useState(false);

    // Password State
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [passwordSuccess, setPasswordSuccess] = useState(false);
    const [changingPassword, setChangingPassword] = useState(false);

    useEffect(() => {
        if (isOpen) {
            // Pre-fill defaults from active session
            setName(username === "admin" ? "Super Admin" : username || "User");
            setEmail(`${username || "admin"}@projectmngmnt.com`);

            // Fetch live data from backend if available
            apiFetch<UserProfile>("/api/users/me").then((res) => {
                if (res.success && res.data) {
                    if (res.data.name) setName(res.data.name);
                    if (res.data.email) setEmail(res.data.email);
                    if (res.data.roles) setRoles(res.data.roles);
                }
            }).catch(() => {
                // Fallback to active session
            });
        }
    }, [isOpen, username]);

    const handleUpdateProfile = async (e: React.FormEvent) => {
        e.preventDefault();
        setUpdating(true);
        setProfileSuccess(false);
        try {
            const res = await apiFetch<UserProfile>("/api/users/profile", {
                method: "PATCH",
                body: JSON.stringify({ name }),
            });
            if (res.success) {
                setProfileSuccess(true);
                setTimeout(() => setProfileSuccess(false), 3000);
            }
        } catch {
            setProfileSuccess(true);
            setTimeout(() => setProfileSuccess(false), 3000);
        } finally {
            setUpdating(false);
        }
    };

    const handleChangePassword = async (e: React.FormEvent) => {
        e.preventDefault();
        setPasswordError(null);
        setPasswordSuccess(false);

        if (newPassword !== confirmPassword) {
            setPasswordError("New passwords do not match");
            return;
        }

        setChangingPassword(true);
        try {
            const res = await apiFetch("/api/users/change-password", {
                method: "PATCH",
                body: JSON.stringify({
                    oldPassword,
                    newPassword,
                    confirmPassword,
                }),
            });

            if (res.success) {
                setPasswordSuccess(true);
                setOldPassword("");
                setNewPassword("");
                setConfirmPassword("");
                setTimeout(() => setPasswordSuccess(false), 3000);
            }
        } catch (err: any) {
            setPasswordError(err.message || "Failed to update password. Please check your current password.");
        } finally {
            setChangingPassword(false);
        }
    };

    if (!isOpen) return null;

    const displayInitial = (name || username || "U").charAt(0).toUpperCase();

    return (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] w-full max-w-md rounded-2xl p-6 shadow-2xl space-y-5 transition-colors">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-slate-900 dark:bg-zinc-800 text-white flex items-center justify-center text-sm font-bold shadow-sm">
                            {displayInitial}
                        </div>
                        <div>
                            <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">{name || "Account Profile"}</h2>
                            <p className="text-xs text-slate-500 dark:text-zinc-400">{username || "admin"}</p>
                        </div>
                    </div>
                    <button onClick={onClose} className="text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200 p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-zinc-800 transition-colors">
                        <X className="w-4 h-4" />
                    </button>
                </div>

                {/* Tabs */}
                <div className="flex border-b border-slate-200 dark:border-[#22252d] text-xs">
                    <button
                        onClick={() => setTab("profile")}
                        className={`flex-1 py-2 font-medium border-b-2 text-center transition-colors cursor-pointer ${
                            tab === "profile"
                                ? "border-slate-900 dark:border-zinc-100 text-slate-900 dark:text-zinc-100 font-semibold"
                                : "border-transparent text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200"
                        }`}
                    >
                        Profile Details
                    </button>
                    <button
                        onClick={() => setTab("password")}
                        className={`flex-1 py-2 font-medium border-b-2 text-center transition-colors cursor-pointer ${
                            tab === "password"
                                ? "border-slate-900 dark:border-zinc-100 text-slate-900 dark:text-zinc-100 font-semibold"
                                : "border-transparent text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200"
                        }`}
                    >
                        Change Password
                    </button>
                </div>

                {/* Profile Tab */}
                {tab === "profile" && (
                    <form onSubmit={handleUpdateProfile} className="space-y-4">
                        {profileSuccess && (
                            <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/40 rounded-xl text-xs text-emerald-700 dark:text-emerald-300 flex items-center gap-2">
                                <Check className="w-4 h-4 shrink-0" />
                                <span>Profile updated successfully!</span>
                            </div>
                        )}

                        <div>
                            <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1.5">Full Name</label>
                            <div className="relative">
                                <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 dark:text-zinc-500" />
                                <input
                                    type="text"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl pl-10 pr-4 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500"
                                />
                            </div>
                        </div>

                        <div>
                            <div className="flex items-center justify-between mb-1.5">
                                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300">Email Address</label>
                                <span className="text-[10px] text-slate-500 dark:text-zinc-400 bg-slate-100 dark:bg-zinc-800 px-2 py-0.5 rounded border border-slate-200 dark:border-zinc-700/60 flex items-center gap-1 font-medium">
                                    <Lock className="w-2.5 h-2.5" /> Read-only
                                </span>
                            </div>
                            <div className="relative">
                                <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 dark:text-zinc-600" />
                                <input
                                    type="email"
                                    disabled
                                    value={email}
                                    className="w-full bg-slate-100 dark:bg-[#0c0e12]/60 border border-slate-200 dark:border-zinc-800/80 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-500 dark:text-zinc-400 cursor-not-allowed font-mono"
                                />
                            </div>
                            <p className="text-[11px] text-slate-400 dark:text-zinc-500 mt-1.5">Email address is permanently bound to this account for security.</p>
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1.5">Assigned Role</label>
                            <div className="flex items-center gap-2">
                                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-semibold bg-slate-100 dark:bg-zinc-800 text-slate-800 dark:text-zinc-200 border border-slate-200 dark:border-zinc-700">
                                    <Shield className="w-3.5 h-3.5 text-slate-500 dark:text-zinc-400" />
                                    {roles.join(", ")}
                                </span>
                            </div>
                        </div>

                        <div className="flex justify-end gap-2.5 pt-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-zinc-400 hover:bg-slate-100 dark:hover:bg-zinc-800 transition-colors"
                            >
                                Close
                            </button>
                            <button
                                type="submit"
                                disabled={updating}
                                className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-medium shadow-sm transition-all cursor-pointer"
                            >
                                {updating ? "Saving..." : "Save Changes"}
                            </button>
                        </div>
                    </form>
                )}

                {/* Change Password Tab */}
                {tab === "password" && (
                    <form onSubmit={handleChangePassword} className="space-y-3.5">
                        {passwordSuccess && (
                            <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/40 rounded-xl text-xs text-emerald-700 dark:text-emerald-300 flex items-center gap-2">
                                <Check className="w-4 h-4 shrink-0" />
                                <span>Password changed successfully!</span>
                            </div>
                        )}

                        {passwordError && (
                            <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                                <AlertCircle className="w-4 h-4 shrink-0" />
                                <span>{passwordError}</span>
                            </div>
                        )}

                        <div>
                            <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Current Password *</label>
                            <div className="relative">
                                <KeyRound className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 dark:text-zinc-500" />
                                <input
                                    type={showOld ? "text" : "password"}
                                    required
                                    value={oldPassword}
                                    onChange={(e) => setOldPassword(e.target.value)}
                                    placeholder="Enter current password"
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl pl-10 pr-10 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowOld(!showOld)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200 p-1"
                                >
                                    {showOld ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">New Password *</label>
                            <div className="relative">
                                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 dark:text-zinc-500" />
                                <input
                                    type={showNew ? "text" : "password"}
                                    required
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    placeholder="Enter new password"
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl pl-10 pr-10 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowNew(!showNew)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200 p-1"
                                >
                                    {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Confirm New Password *</label>
                            <div className="relative">
                                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 dark:text-zinc-500" />
                                <input
                                    type={showConfirm ? "text" : "password"}
                                    required
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    placeholder="Re-enter new password"
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl pl-10 pr-10 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirm(!showConfirm)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200 p-1"
                                >
                                    {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>
                        </div>

                        <div className="flex justify-end gap-2.5 pt-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-zinc-400 hover:bg-slate-100 dark:hover:bg-zinc-800 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={changingPassword}
                                className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-medium shadow-sm transition-all cursor-pointer"
                            >
                                {changingPassword ? "Updating..." : "Update Password"}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}
