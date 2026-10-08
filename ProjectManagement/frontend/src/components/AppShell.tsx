"use client";

import { useAuth } from "@/context/AuthContext";
import { useTheme } from "@/context/ThemeContext";
import { useRouter, usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import Link from "next/link";
import {
    LayoutDashboard,
    FolderKanban,
    CheckSquare,
    BarChart3,
    LogOut,
    Search,
    ChevronRight,
    Sun,
    Moon,
    UserPlus,
} from "lucide-react";
import NotificationBell from "./NotificationBell";
import ProfileModal from "./ProfileModal";
import RegisterUserModal from "./RegisterUserModal";
import { apiFetch } from "@/lib/api";
import { Project, PagedResponse } from "@/types";

const navigation = [
    { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
    { name: "Projects", href: "/projects", icon: FolderKanban },
    { name: "Tasks", href: "/tasks", icon: CheckSquare },
    { name: "Reports", href: "/reports", icon: BarChart3 },
];

export default function AppShell({ children }: { children: React.ReactNode }) {
    const { username, logout, isAuthenticated } = useAuth();
    const { theme, toggleTheme } = useTheme();
    const router = useRouter();
    const pathname = usePathname();

    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [isRegisterUserOpen, setIsRegisterUserOpen] = useState(false);
    const [globalQuery, setGlobalQuery] = useState("");
    const [searchResults, setSearchResults] = useState<Project[]>([]);
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        if (!isAuthenticated) {
            router.push("/login");
        }
    }, [isAuthenticated, router]);

    useEffect(() => {
        if (!globalQuery.trim()) {
            setSearchResults([]);
            setIsSearching(false);
            return;
        }

        setIsSearching(true);
        const timer = setTimeout(async () => {
            try {
                const res = await apiFetch<PagedResponse<Project>>("/api/projects?size=20");
                if (res.success && res.data) {
                    const matches = res.data.content.filter((p) =>
                        p.name.toLowerCase().includes(globalQuery.toLowerCase()) ||
                        (p.description && p.description.toLowerCase().includes(globalQuery.toLowerCase()))
                    );
                    setSearchResults(matches);
                }
            } catch {
                // Ignore error
            } finally {
                setIsSearching(false);
            }
        }, 250);

        return () => clearTimeout(timer);
    }, [globalQuery]);

    if (!isAuthenticated) return null;

    return (
        <div className="h-screen w-screen overflow-hidden flex bg-slate-50 dark:bg-[#0c0e12] text-slate-800 dark:text-zinc-200 transition-colors duration-200">
            {/* Pinned Left Sidebar */}
            <aside className="w-60 h-full bg-white dark:bg-[#12151b] border-r border-slate-200 dark:border-zinc-800/80 flex flex-col justify-between p-4 shrink-0 select-none">
                <div className="space-y-6">
                    {/* Brand */}
                    <div className="flex items-center gap-3 px-2 pt-1">
                        <div className="w-8 h-8 rounded-xl bg-slate-900 dark:bg-zinc-800 text-white flex items-center justify-center font-bold text-xs shadow-sm">
                            P
                        </div>
                        <div>
                            <h1 className="font-semibold text-sm text-slate-900 dark:text-zinc-100 tracking-tight leading-none">ProjectHub</h1>
                            <span className="text-[10px] text-slate-400 dark:text-zinc-500 font-medium">Enterprise Suite</span>
                        </div>
                    </div>

                    {/* Nav Items */}
                    <nav className="space-y-1">
                        {navigation.map((item) => {
                            const Icon = item.icon;
                            const isActive = pathname.startsWith(item.href);
                            return (
                                <Link
                                    key={item.name}
                                    href={item.href}
                                    className={`flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium transition-all ${
                                        isActive
                                            ? "bg-slate-100 dark:bg-zinc-800/90 text-slate-900 dark:text-white shadow-sm border border-slate-200 dark:border-zinc-700/60"
                                            : "text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200 hover:bg-slate-50 dark:hover:bg-zinc-800/40"
                                    }`}
                                >
                                    <Icon className="w-4 h-4" />
                                    <span>{item.name}</span>
                                </Link>
                            );
                        })}
                    </nav>
                </div>

                {/* Bottom User Profile */}
                <div className="pt-3 border-t border-slate-200 dark:border-zinc-800/80">
                    <div className="flex items-center justify-between p-1.5 rounded-xl hover:bg-slate-100 dark:hover:bg-zinc-800/40 transition-colors">
                        <button
                            onClick={() => setIsProfileOpen(true)}
                            className="flex items-center gap-2.5 min-w-0 text-left cursor-pointer flex-1"
                            title="Profile & Settings"
                        >
                            <div className="w-7 h-7 rounded-full bg-slate-200 dark:bg-zinc-800 border border-slate-300 dark:border-zinc-700/60 flex items-center justify-center text-slate-700 dark:text-zinc-300 shrink-0 text-xs font-semibold">
                                {username?.charAt(0).toUpperCase() || "A"}
                            </div>
                            <div className="truncate">
                                <p className="text-xs font-medium text-slate-900 dark:text-zinc-200 truncate">{username || "admin"}</p>
                                <p className="text-[10px] text-slate-400 dark:text-zinc-500">Settings & Security</p>
                            </div>
                        </button>
                        <button
                            onClick={logout}
                            title="Logout"
                            className="p-1.5 rounded-lg text-slate-400 dark:text-zinc-500 hover:text-rose-600 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/20 transition-colors cursor-pointer"
                        >
                            <LogOut className="w-3.5 h-3.5" />
                        </button>
                    </div>
                </div>
            </aside>

            {/* Main Content Pane */}
            <div className="flex-1 h-full flex flex-col min-w-0 overflow-hidden">
                {/* Fixed Topbar */}
                <header className="h-14 border-b border-slate-200 dark:border-zinc-800/80 bg-white/80 dark:bg-[#12151b]/80 backdrop-blur-md px-8 flex items-center justify-between gap-6 shrink-0 z-10">
                    {/* Global Quick Search */}
                    <div className="relative w-72">
                        <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-zinc-500" />
                        <input
                            type="text"
                            placeholder="Search projects & milestones..."
                            value={globalQuery}
                            onChange={(e) => setGlobalQuery(e.target.value)}
                            className="w-full bg-slate-100 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl pl-8 pr-3 py-1.5 text-xs text-slate-800 dark:text-zinc-200 placeholder:text-slate-400 dark:placeholder:text-zinc-600 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500"
                        />

                        {globalQuery.trim() && (
                            <div className="absolute left-0 top-full mt-2 w-80 bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl shadow-2xl p-2 z-50 space-y-1">
                                <p className="text-[10px] text-slate-400 dark:text-zinc-500 px-2 py-1 uppercase tracking-wider font-semibold">
                                    {isSearching ? "Searching..." : `Projects (${searchResults.length})`}
                                </p>
                                {searchResults.length === 0 && !isSearching ? (
                                    <p className="text-xs text-slate-400 dark:text-zinc-500 px-2 py-3 text-center">No projects found</p>
                                ) : (
                                    searchResults.map((p) => (
                                        <Link
                                            key={p.projectId}
                                            href={`/projects/${p.projectId}`}
                                            onClick={() => setGlobalQuery("")}
                                            className="flex items-center justify-between p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-zinc-800/60 text-xs text-slate-700 dark:text-zinc-200 transition-colors group"
                                        >
                                            <span className="font-medium group-hover:text-slate-900 dark:group-hover:text-white truncate">{p.name}</span>
                                            <ChevronRight className="w-3.5 h-3.5 text-slate-400 dark:text-zinc-500" />
                                        </Link>
                                    ))
                                )}
                            </div>
                        )}
                    </div>

                    {/* Actions: Add User, Theme Toggle & Notifications */}
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => setIsRegisterUserOpen(true)}
                            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 text-xs font-semibold shadow-sm transition-all cursor-pointer"
                        >
                            <UserPlus className="w-3.5 h-3.5" />
                            <span>Add User</span>
                        </button>

                        <button
                            onClick={toggleTheme}
                            title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
                            className="p-2 rounded-xl bg-slate-100 dark:bg-[#181a20] border border-slate-200 dark:border-zinc-800 hover:bg-slate-200 dark:hover:bg-zinc-800 text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200 transition-colors cursor-pointer"
                        >
                            {theme === "dark" ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-700" />}
                        </button>

                        <NotificationBell />
                    </div>
                </header>

                {/* Scrollable Main Area */}
                <main className="flex-1 p-8 overflow-y-auto">{children}</main>
            </div>

            {/* Profile & Change Password Modal */}
            <ProfileModal isOpen={isProfileOpen} onClose={() => setIsProfileOpen(false)} />

            {/* Register User Modal (Admin / Workspace) */}
            <RegisterUserModal isOpen={isRegisterUserOpen} onClose={() => setIsRegisterUserOpen(false)} />
        </div>
    );
}
