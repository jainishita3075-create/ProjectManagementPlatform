"use client";

import { useState, useEffect } from "react";
import AppShell from "@/components/AppShell";
import { apiFetch } from "@/lib/api";
import {
    BarChart3,
    Calendar,
    CheckCircle2,
    Clock,
    AlertCircle,
    FolderKanban,
    TrendingUp,
    RefreshCw
} from "lucide-react";

interface ProjectReportItem {
    projectId: number;
    projectName: string;
    status: string;
    totalTasks: number;
    completedTasks: number;
    progressPercentage: number;
}

interface ProjectProgressReport {
    fromDate: string;
    toDate: string;
    totalProjectsInScope: number;
    tasksCreatedInPeriod: number;
    tasksCompletedInPeriod: number;
    overdueTasksInPeriod: number;
    overallCompletionRate: number;
    projectBreakdown: ProjectReportItem[];
}

interface UserPerformanceItem {
    userId: number;
    username: string;
    name: string;
    tasksAssigned: number;
    tasksCompleted: number;
    tasksOverdue: number;
    completionRate: number;
}

interface UserPerformanceReport {
    fromDate: string;
    toDate: string;
    userPerformanceList: UserPerformanceItem[];
}

export default function ReportsPage() {
    const getDefaultDates = () => {
        const to = new Date();
        const from = new Date();
        from.setDate(to.getDate() - 30);
        return {
            from: from.toISOString().split("T")[0],
            to: to.toISOString().split("T")[0],
        };
    };

    const [dates, setDates] = useState(getDefaultDates());
    const [loading, setLoading] = useState(false);
    const [projectReport, setProjectReport] = useState<ProjectProgressReport | null>(null);
    const [userReport, setUserReport] = useState<UserPerformanceReport | null>(null);
    const [error, setError] = useState<string | null>(null);

    const generateReports = async (fromDate: string, toDate: string) => {
        setLoading(true);
        setError(null);
        try {
            const fromIso = new Date(`${fromDate}T00:00:00Z`).toISOString();
            const toIso = new Date(`${toDate}T23:59:59Z`).toISOString();

            const [pRes, uRes] = await Promise.all([
                apiFetch<ProjectProgressReport>(`/api/reports/project-progress?from=${fromIso}&to=${toIso}`),
                apiFetch<UserPerformanceReport>(`/api/reports/user-performance?from=${fromIso}&to=${toIso}`),
            ]);

            if (pRes.success && pRes.data) setProjectReport(pRes.data);
            if (uRes.success && uRes.data) setUserReport(uRes.data);
        } catch (err: any) {
            console.error("Failed to generate reports", err);
            setError(err.message || "Failed to generate reports.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        generateReports(dates.from, dates.to);
    }, []);

    const handlePreset = (days: number) => {
        const to = new Date();
        const from = new Date();
        from.setDate(to.getDate() - days);
        const newDates = {
            from: from.toISOString().split("T")[0],
            to: to.toISOString().split("T")[0],
        };
        setDates(newDates);
        generateReports(newDates.from, newDates.to);
    };

    return (
        <AppShell>
            <div className="space-y-6 max-w-7xl mx-auto">
                {/* Header Row */}
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-zinc-800/80">
                    <div>
                        <h1 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-zinc-100 flex items-center gap-2.5">
                            <BarChart3 className="w-5 h-5 text-slate-600 dark:text-zinc-400" />
                            Analytics & Performance Reports
                        </h1>
                        <p className="text-xs text-slate-500 dark:text-zinc-500 mt-1">
                            Team delivery velocity, completed milestones, and individual workload metrics
                        </p>
                    </div>

                    {/* Date Control Toolbar */}
                    <div className="flex flex-wrap items-center gap-2 bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] p-1.5 rounded-xl shadow-sm">
                        <div className="flex items-center gap-1 border-r border-slate-200 dark:border-[#22252d] pr-2">
                            <button
                                onClick={() => handlePreset(7)}
                                className="px-2.5 py-1 text-xs rounded-lg text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-100 hover:bg-slate-100 dark:hover:bg-zinc-800 transition-colors cursor-pointer"
                            >
                                7D
                            </button>
                            <button
                                onClick={() => handlePreset(30)}
                                className="px-2.5 py-1 text-xs rounded-lg text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-100 hover:bg-slate-100 dark:hover:bg-zinc-800 transition-colors cursor-pointer"
                            >
                                30D
                            </button>
                            <button
                                onClick={() => handlePreset(90)}
                                className="px-2.5 py-1 text-xs rounded-lg text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-100 hover:bg-slate-100 dark:hover:bg-zinc-800 transition-colors cursor-pointer"
                            >
                                90D
                            </button>
                        </div>

                        <div className="flex items-center gap-1.5 px-1 text-xs">
                            <input
                                type="date"
                                value={dates.from}
                                onChange={(e) => setDates((prev) => ({ ...prev, from: e.target.value }))}
                                className="bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-lg px-2 py-1 text-xs text-slate-800 dark:text-zinc-200 focus:outline-none"
                            />
                            <span className="text-slate-400 text-xs">to</span>
                            <input
                                type="date"
                                value={dates.to}
                                onChange={(e) => setDates((prev) => ({ ...prev, to: e.target.value }))}
                                className="bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-lg px-2 py-1 text-xs text-slate-800 dark:text-zinc-200 focus:outline-none"
                            />
                        </div>

                        <button
                            onClick={() => generateReports(dates.from, dates.to)}
                            disabled={loading}
                            className="inline-flex items-center gap-1.5 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 px-3 py-1.5 rounded-lg text-xs font-medium transition-all disabled:opacity-50 cursor-pointer shadow-sm"
                        >
                            <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} />
                            Run
                        </button>
                    </div>
                </div>

                {error && (
                    <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                        <AlertCircle className="w-4 h-4 shrink-0" />
                        <span>{error}</span>
                    </div>
                )}

                {/* 5 KPI Metric Cards */}
                {projectReport && (
                    <div className="grid grid-cols-2 lg:grid-cols-5 gap-3.5">
                        <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-4 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 dark:text-zinc-400">
                                <span className="text-xs font-medium">Projects in Scope</span>
                                <FolderKanban className="w-4 h-4 text-slate-400 dark:text-zinc-500" />
                            </div>
                            <div className="mt-3">
                                <p className="text-2xl font-bold tracking-tight text-slate-900 dark:text-zinc-100">
                                    {projectReport.totalProjectsInScope}
                                </p>
                                <span className="text-[11px] text-slate-400 dark:text-zinc-500">Active projects</span>
                            </div>
                        </div>

                        <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-4 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 dark:text-zinc-400">
                                <span className="text-xs font-medium">Tasks Created</span>
                                <TrendingUp className="w-4 h-4 text-sky-500" />
                            </div>
                            <div className="mt-3">
                                <p className="text-2xl font-bold tracking-tight text-slate-900 dark:text-zinc-100">
                                    {projectReport.tasksCreatedInPeriod}
                                </p>
                                <span className="text-[11px] text-slate-400 dark:text-zinc-500">Added to backlog</span>
                            </div>
                        </div>

                        <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-4 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 dark:text-zinc-400">
                                <span className="text-xs font-medium">Completed</span>
                                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                            </div>
                            <div className="mt-3">
                                <p className="text-2xl font-bold tracking-tight text-emerald-600 dark:text-emerald-400">
                                    {projectReport.tasksCompletedInPeriod}
                                </p>
                                <span className="text-[11px] text-slate-400 dark:text-zinc-500">Shipped items</span>
                            </div>
                        </div>

                        <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-4 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 dark:text-zinc-400">
                                <span className="text-xs font-medium">Overdue</span>
                                <AlertCircle className="w-4 h-4 text-rose-500" />
                            </div>
                            <div className="mt-3">
                                <p className="text-2xl font-bold tracking-tight text-rose-600 dark:text-rose-400">
                                    {projectReport.overdueTasksInPeriod}
                                </p>
                                <span className="text-[11px] text-slate-400 dark:text-zinc-500">Delayed delivery</span>
                            </div>
                        </div>

                        <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-4 shadow-sm col-span-2 lg:col-span-1 flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 dark:text-zinc-400">
                                <span className="text-xs font-medium">Completion Rate</span>
                                <Clock className="w-4 h-4 text-slate-400 dark:text-zinc-500" />
                            </div>
                            <div className="mt-3">
                                <p className="text-2xl font-bold tracking-tight text-slate-900 dark:text-zinc-100">
                                    {projectReport.overallCompletionRate}%
                                </p>
                                <div className="w-full h-1.5 bg-slate-100 dark:bg-zinc-800 rounded-full overflow-hidden mt-1.5">
                                    <div
                                        className="h-full bg-emerald-500 rounded-full transition-all"
                                        style={{ width: `${projectReport.overallCompletionRate}%` }}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Section 1: Project Breakdown Table */}
                <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-6 shadow-sm space-y-4">
                    <div>
                        <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">Project Progress Breakdown</h2>
                        <p className="text-xs text-slate-500 dark:text-zinc-500 mt-0.5">Task completion ratio and status across all active initiatives</p>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                            <thead>
                            <tr className="border-b border-slate-100 dark:border-[#1f222a] text-slate-500 dark:text-zinc-500 text-[11px] font-semibold uppercase tracking-wider">
                                <th className="pb-3">Project Name</th>
                                <th className="pb-3">Status</th>
                                <th className="pb-3">Total Tasks</th>
                                <th className="pb-3">Completed</th>
                                <th className="pb-3 w-48">Progress</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-[#181a20]">
                            {projectReport?.projectBreakdown.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="py-8 text-center text-slate-400 dark:text-zinc-500">
                                        No project data recorded during this date period.
                                    </td>
                                </tr>
                            ) : (
                                projectReport?.projectBreakdown.map((item) => (
                                    <tr key={item.projectId} className="hover:bg-slate-50 dark:hover:bg-[#181b22] transition-colors">
                                        <td className="py-3.5 font-medium text-slate-900 dark:text-zinc-200">{item.projectName}</td>
                                        <td className="py-3.5">
                                                <span className="px-2 py-0.5 rounded-md text-[10px] font-medium bg-slate-100 dark:bg-zinc-800 text-slate-700 dark:text-zinc-300 border border-slate-200 dark:border-zinc-700">
                                                    {item.status}
                                                </span>
                                        </td>
                                        <td className="py-3.5 text-slate-600 dark:text-zinc-400">{item.totalTasks}</td>
                                        <td className="py-3.5 text-emerald-600 dark:text-emerald-400 font-semibold">{item.completedTasks}</td>
                                        <td className="py-3.5">
                                            <div className="flex items-center gap-3">
                                                <div className="flex-1 h-1.5 bg-slate-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                                                    <div
                                                        className="h-full bg-slate-800 dark:bg-zinc-200 rounded-full"
                                                        style={{ width: `${item.progressPercentage}%` }}
                                                    />
                                                </div>
                                                <span className="text-[11px] font-medium text-slate-700 dark:text-zinc-300 w-10 text-right">
                                                        {item.progressPercentage}%
                                                    </span>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Section 2: Team Performance Matrix */}
                <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-6 shadow-sm space-y-4">
                    <div>
                        <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">Team Performance & Output</h2>
                        <p className="text-xs text-slate-500 dark:text-zinc-500 mt-0.5">Individual task assignments, output, and overdue ratios</p>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                            <thead>
                            <tr className="border-b border-slate-100 dark:border-[#1f222a] text-slate-500 dark:text-zinc-500 text-[11px] font-semibold uppercase tracking-wider">
                                <th className="pb-3">Team Member</th>
                                <th className="pb-3">Assigned</th>
                                <th className="pb-3">Completed</th>
                                <th className="pb-3">Overdue</th>
                                <th className="pb-3 w-48">Completion Rate</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-[#181a20]">
                            {userReport?.userPerformanceList.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="py-8 text-center text-slate-400 dark:text-zinc-500">
                                        No user performance metrics available for this period.
                                    </td>
                                </tr>
                            ) : (
                                userReport?.userPerformanceList.map((user) => (
                                    <tr key={user.userId} className="hover:bg-slate-50 dark:hover:bg-[#181b22] transition-colors">
                                        <td className="py-3.5">
                                            <div className="flex items-center gap-2.5">
                                                <div className="w-7 h-7 rounded-full bg-slate-200 dark:bg-zinc-800 border border-slate-300 dark:border-zinc-700 flex items-center justify-center text-[11px] font-semibold text-slate-700 dark:text-zinc-300">
                                                    {(user.name || user.username).charAt(0).toUpperCase()}
                                                </div>
                                                <div>
                                                    <p className="font-medium text-slate-900 dark:text-zinc-200">{user.name || user.username}</p>
                                                    <p className="text-[10px] text-slate-400 dark:text-zinc-500">{user.username}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="py-3.5 text-slate-700 dark:text-zinc-300 font-medium">{user.tasksAssigned}</td>
                                        <td className="py-3.5 text-emerald-600 dark:text-emerald-400 font-semibold">{user.tasksCompleted}</td>
                                        <td className="py-3.5">
                                                <span className={`px-2 py-0.5 rounded-md text-[11px] font-medium ${
                                                    user.tasksOverdue > 0
                                                        ? "bg-rose-100 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-900/40"
                                                        : "text-slate-400 dark:text-zinc-500"
                                                }`}>
                                                    {user.tasksOverdue}
                                                </span>
                                        </td>
                                        <td className="py-3.5">
                                            <div className="flex items-center gap-3">
                                                <div className="flex-1 h-1.5 bg-slate-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                                                    <div
                                                        className="h-full bg-slate-800 dark:bg-zinc-200 rounded-full"
                                                        style={{ width: `${user.completionRate}%` }}
                                                    />
                                                </div>
                                                <span className="text-[11px] font-medium text-slate-700 dark:text-zinc-300 w-10 text-right">
                                                        {user.completionRate}%
                                                    </span>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </AppShell>
    );
}
