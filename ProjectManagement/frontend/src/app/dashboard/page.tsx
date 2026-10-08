"use client";

import { useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { apiFetch } from "@/lib/api";
import { DashboardSummary } from "@/types";
import {
    FolderKanban,
    CheckCircle2,
    Clock,
    AlertTriangle,
    Users,
} from "lucide-react";

export default function DashboardPage() {
    const [data, setData] = useState<DashboardSummary | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboard = async () => {
            try {
                const res = await apiFetch<DashboardSummary>("/api/dashboard");
                setData(res.data);
            } catch (err) {
                console.error("Failed to load dashboard data", err);
            } finally {
                setLoading(false);
            }
        };
        fetchDashboard();
    }, []);

    if (loading) {
        return (
            <AppShell>
                <div className="h-full flex items-center justify-center text-slate-500 dark:text-zinc-500 text-xs">
                    Loading metrics...
                </div>
            </AppShell>
        );
    }

    const cards = [
        {
            title: "Total Projects",
            value: data?.totalProjects || 0,
            sub: `${data?.activeProjects || 0} active`,
            icon: FolderKanban,
            badge: "bg-sky-50 dark:bg-sky-950/40 text-sky-700 dark:text-sky-300 border-sky-200 dark:border-sky-800/40",
        },
        {
            title: "Completed Tasks",
            value: data?.completedTasks || 0,
            sub: `out of ${data?.totalTasks || 0} total`,
            icon: CheckCircle2,
            badge: "bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800/40",
        },
        {
            title: "In Progress",
            value: data?.inProgressTasks || 0,
            sub: "Active execution",
            icon: Clock,
            badge: "bg-amber-50 dark:bg-amber-950/40 text-amber-700 dark:text-amber-300 border-amber-200 dark:border-amber-800/40",
        },
        {
            title: "Overdue Tasks",
            value: data?.overdueTasks || 0,
            sub: "Requires attention",
            icon: AlertTriangle,
            badge: "bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 border-rose-200 dark:border-rose-800/40",
        },
    ];

    return (
        <AppShell>
            <div className="space-y-6 max-w-7xl mx-auto">
                <div>
                    <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-zinc-100">Overview</h2>
                    <p className="text-xs text-slate-500 dark:text-zinc-400 mt-0.5">
                        Key workspace metrics and team workload distribution.
                    </p>
                </div>

                {/* Metric Cards */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                    {cards.map((c) => {
                        const Icon = c.icon;
                        return (
                            <div
                                key={c.title}
                                className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-zinc-800/80 rounded-2xl p-5 shadow-sm"
                            >
                                <div className="flex items-center justify-between">
                                    <span className="text-xs font-semibold text-slate-600 dark:text-zinc-400">
                                        {c.title}
                                    </span>
                                    <div className={`p-1.5 rounded-lg border ${c.badge}`}>
                                        <Icon className="w-3.5 h-3.5" />
                                    </div>
                                </div>
                                <div className="mt-3">
                                    <span className="text-2xl font-bold text-slate-900 dark:text-zinc-100">{c.value}</span>
                                    <p className="text-xs text-slate-500 dark:text-zinc-400 mt-0.5">{c.sub}</p>
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Team Workload Section */}
                <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-zinc-800/80 rounded-2xl p-6 shadow-sm">
                    <div className="flex items-center gap-2 mb-4">
                        <Users className="w-4 h-4 text-slate-600 dark:text-zinc-400" />
                        <h3 className="font-semibold text-sm text-slate-900 dark:text-zinc-100">Team Workload Distribution</h3>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                            <thead className="text-[11px] font-semibold text-slate-500 dark:text-zinc-400 border-b border-slate-200 dark:border-zinc-800 uppercase tracking-wider">
                            <tr>
                                <th className="pb-3">Team Member</th>
                                <th className="pb-3 text-center">Assigned</th>
                                <th className="pb-3 text-center">Completed</th>
                                <th className="pb-3 text-center">Pending</th>
                                <th className="pb-3 text-center">Overdue</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-zinc-800/50">
                            {data?.workloadByUser && data.workloadByUser.length > 0 ? (
                                data.workloadByUser.map((u) => (
                                    <tr key={u.userId} className="hover:bg-slate-50 dark:hover:bg-zinc-800/30 transition-colors">
                                        <td className="py-3.5">
                                            <div className="font-semibold text-slate-900 dark:text-zinc-200">{u.name || u.username}</div>
                                            <div className="text-[10px] text-slate-500 dark:text-zinc-400">@{u.username}</div>
                                        </td>
                                        <td className="py-3.5 text-center font-semibold text-slate-700 dark:text-zinc-300">
                                            {u.totalAssignedTasks}
                                        </td>
                                        <td className="py-3.5 text-center font-semibold text-emerald-600 dark:text-emerald-400">
                                            {u.completedTasks}
                                        </td>
                                        <td className="py-3.5 text-center font-semibold text-amber-600 dark:text-amber-300">
                                            {u.pendingTasks}
                                        </td>
                                        <td className="py-3.5 text-center font-semibold text-rose-600 dark:text-rose-400">
                                            {u.overdueTasks}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={5} className="py-8 text-center text-xs text-slate-400 dark:text-zinc-500">
                                        No team task data available
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </AppShell>
    );
}
