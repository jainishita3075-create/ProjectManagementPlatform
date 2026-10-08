"use client";

import { useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { apiFetch } from "@/lib/api";
import { PagedResponse, Project, ProjectPriority, ProjectStatus } from "@/types";
import {
    FolderKanban,
    Plus,
    Search,
    Calendar,
    Archive,
    X,
    ChevronLeft,
    ChevronRight,
    AlertCircle
} from "lucide-react";
import Link from "next/link";

export default function ProjectsPage() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalElements, setTotalElements] = useState(0);
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState<string>("ALL");

    // Create Modal State
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState<string | null>(null);
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [priority, setPriority] = useState<ProjectPriority>("MEDIUM");
    const [dueDate, setDueDate] = useState("");
    const [startDate, setStartDate] = useState("");

    const fetchProjects = async () => {
        setLoading(true);
        try {
            const res = await apiFetch<PagedResponse<Project>>(`/api/projects?page=${page}&size=9&sortBy=createdAt&sortDir=desc`);
            if (res.success && res.data) {
                setProjects(res.data.content);
                setTotalPages(res.data.totalPages);
                setTotalElements(res.data.totalElements);
            }
        } catch (err) {
            console.error("Failed to load projects", err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProjects();
    }, [page]);

    const handleCreateProject = async (e: React.FormEvent) => {
        e.preventDefault();
        setCreating(true);
        setCreateError(null);

        try {
            const payload = {
                name,
                description,
                priority,
                startDate: startDate ? new Date(startDate).toISOString() : null,
                dueDate: new Date(dueDate).toISOString(),
                status: "ACTIVE"
            };

            const res = await apiFetch<Project>("/api/projects", {
                method: "POST",
                body: JSON.stringify(payload),
            });

            if (res.success) {
                setIsCreateOpen(false);
                setName("");
                setDescription("");
                setDueDate("");
                setStartDate("");
                fetchProjects();
            }
        } catch (err: any) {
            setCreateError(err.message || "Failed to create project");
        } finally {
            setCreating(false);
        }
    };

    const handleArchive = async (projectId: number, e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (!confirm("Are you sure you want to archive this project?")) return;

        try {
            await apiFetch(`/api/projects/${projectId}/archive`, { method: "PATCH" });
            fetchProjects();
        } catch (err) {
            console.error("Failed to archive project", err);
        }
    };

    const filteredProjects = projects.filter((p) => {
        const matchesSearch = p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            (p.description && p.description.toLowerCase().includes(searchQuery.toLowerCase()));
        const matchesStatus = statusFilter === "ALL" || p.status === statusFilter;
        return matchesSearch && matchesStatus;
    });

    const getStatusBadge = (status: ProjectStatus) => {
        switch (status) {
            case "ACTIVE": return "bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800/40";
            case "COMPLETED": return "bg-sky-50 dark:bg-sky-950/40 text-sky-700 dark:text-sky-300 border-sky-200 dark:border-sky-800/40";
            case "ARCHIVED": return "bg-slate-100 dark:bg-zinc-800 text-slate-600 dark:text-zinc-400 border-slate-200 dark:border-zinc-700";
            case "BLOCKED": return "bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 border-rose-200 dark:border-rose-800/40";
            default: return "bg-amber-50 dark:bg-amber-950/40 text-amber-700 dark:text-amber-300 border-amber-200 dark:border-amber-800/40";
        }
    };

    const getPriorityBadge = (p: ProjectPriority) => {
        switch (p) {
            case "URGENT": return "text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/30 border-rose-200 dark:border-rose-900/40";
            case "HIGH": return "text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/30 border-amber-200 dark:border-amber-900/40";
            case "MEDIUM": return "text-slate-700 dark:text-zinc-300 bg-slate-100 dark:bg-zinc-800/50 border-slate-200 dark:border-zinc-700/40";
            default: return "text-slate-500 dark:text-zinc-400 bg-slate-100 dark:bg-zinc-800/30 border-slate-200 dark:border-zinc-800";
        }
    };

    return (
        <AppShell>
            <div className="space-y-6 max-w-7xl mx-auto">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-zinc-800/80">
                    <div>
                        <h1 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-zinc-100 flex items-center gap-2.5">
                            <FolderKanban className="w-5 h-5 text-slate-600 dark:text-zinc-400" />
                            Projects
                        </h1>
                        <p className="text-xs text-slate-500 dark:text-zinc-400 mt-1">
                            {totalElements} total project{totalElements !== 1 ? "s" : ""} across your team
                        </p>
                    </div>

                    <button
                        onClick={() => setIsCreateOpen(true)}
                        className="inline-flex items-center gap-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 px-4 py-2 rounded-xl text-xs font-semibold transition-all shadow-sm cursor-pointer"
                    >
                        <Plus className="w-4 h-4" />
                        New Project
                    </button>
                </div>

                {/* Filters */}
                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 dark:text-zinc-500" />
                        <input
                            type="text"
                            placeholder="Search projects by name..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl pl-10 pr-4 py-2 text-xs text-slate-900 dark:text-zinc-100 placeholder:text-slate-400 dark:placeholder:text-zinc-500 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500 transition-colors"
                        />
                    </div>

                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-zinc-300 focus:outline-none cursor-pointer"
                    >
                        <option value="ALL">All Statuses</option>
                        <option value="ACTIVE">Active</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="BLOCKED">Blocked</option>
                        <option value="ARCHIVED">Archived</option>
                    </select>
                </div>

                {/* Projects Grid */}
                {loading ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {[1, 2, 3].map((n) => (
                            <div key={n} className="h-44 bg-slate-100 dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl animate-pulse" />
                        ))}
                    </div>
                ) : filteredProjects.length === 0 ? (
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-12 text-center shadow-sm">
                        <FolderKanban className="w-10 h-10 text-slate-400 dark:text-zinc-600 mx-auto mb-3" />
                        <p className="text-sm font-semibold text-slate-800 dark:text-zinc-300">No projects found</p>
                        <p className="text-xs text-slate-500 dark:text-zinc-400 mt-1">Get started by creating a new project</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {filteredProjects.map((project) => (
                            <Link
                                key={project.projectId}
                                href={`/projects/${project.projectId}`}
                                className="group bg-white dark:bg-[#14161b] hover:bg-slate-50 dark:hover:bg-[#181a20] border border-slate-200 dark:border-[#22252d] hover:border-slate-300 dark:hover:border-zinc-700 rounded-2xl p-5 transition-all flex flex-col justify-between block shadow-sm"
                            >
                                <div>
                                    <div className="flex items-start justify-between gap-2 mb-2">
                                        <h3 className="text-sm font-semibold text-slate-900 dark:text-zinc-100 group-hover:text-blue-600 dark:group-hover:text-white transition-colors truncate">
                                            {project.name}
                                        </h3>
                                        <div className="flex items-center gap-1.5 shrink-0">
                                            <span className={`px-2 py-0.5 rounded-md text-[10px] font-bold border ${getStatusBadge(project.status)}`}>
                                                {project.status}
                                            </span>
                                            <span className={`px-2 py-0.5 rounded-md text-[10px] font-bold border ${getPriorityBadge(project.priority)}`}>
                                                {project.priority}
                                            </span>
                                        </div>
                                    </div>

                                    <p className="text-xs text-slate-500 dark:text-zinc-400 line-clamp-2 mb-4 leading-relaxed">
                                        {project.description || "No description provided."}
                                    </p>
                                </div>

                                <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-[#1d2027]">
                                    {/* High-Contrast Progress Bar */}
                                    <div>
                                        <div className="flex justify-between text-[11px] text-slate-600 dark:text-zinc-400 mb-1.5 font-medium">
                                            <span>Progress</span>
                                            <span className="font-bold text-slate-900 dark:text-zinc-200">{project.progressPercentage}%</span>
                                        </div>
                                        <div className="w-full h-2 bg-slate-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                                            <div
                                                className="h-full bg-emerald-500 rounded-full transition-all duration-300"
                                                style={{ width: `${Math.max(project.progressPercentage, 0)}%` }}
                                            />
                                        </div>
                                    </div>

                                    {/* Footer Info */}
                                    <div className="flex items-center justify-between text-[11px] text-slate-500 dark:text-zinc-400">
                                        <div className="flex items-center gap-1.5">
                                            <Calendar className="w-3.5 h-3.5 text-slate-400 dark:text-zinc-500" />
                                            <span>Due {new Date(project.dueDate).toLocaleDateString()}</span>
                                        </div>

                                        <div className="flex items-center gap-2">
                                            <span className="font-medium text-slate-700 dark:text-zinc-300">{project.completedTasks}/{project.totalTasks} tasks</span>
                                            {project.status !== "ARCHIVED" && (
                                                <button
                                                    onClick={(e) => handleArchive(project.projectId, e)}
                                                    title="Archive project"
                                                    className="p-1 hover:text-slate-800 dark:hover:text-zinc-200 transition-colors"
                                                >
                                                    <Archive className="w-3.5 h-3.5" />
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </Link>
                        ))}
                    </div>
                )}

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-[#22252d]">
                        <p className="text-xs text-slate-500 dark:text-zinc-400">
                            Page {page + 1} of {totalPages}
                        </p>
                        <div className="flex items-center gap-2">
                            <button
                                disabled={page === 0}
                                onClick={() => setPage(page - 1)}
                                className="p-2 bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-lg text-slate-600 dark:text-zinc-400 hover:text-slate-900 disabled:opacity-40"
                            >
                                <ChevronLeft className="w-4 h-4" />
                            </button>
                            <button
                                disabled={page >= totalPages - 1}
                                onClick={() => setPage(page + 1)}
                                className="p-2 bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-lg text-slate-600 dark:text-zinc-400 hover:text-slate-900 disabled:opacity-40"
                            >
                                <ChevronRight className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Create Project Modal */}
            {isCreateOpen && (
                <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] w-full max-w-lg rounded-2xl p-6 shadow-2xl space-y-5">
                        <div className="flex items-center justify-between">
                            <h2 className="text-base font-semibold text-slate-900 dark:text-zinc-100">Create New Project</h2>
                            <button onClick={() => setIsCreateOpen(false)} className="text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200">
                                <X className="w-4 h-4" />
                            </button>
                        </div>

                        {createError && (
                            <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                                <AlertCircle className="w-4 h-4 shrink-0" />
                                <span>{createError}</span>
                            </div>
                        )}

                        <form onSubmit={handleCreateProject} className="space-y-4">
                            <div>
                                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1.5">Project Name *</label>
                                <input
                                    type="text"
                                    required
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="e.g. Website Redesign"
                                    className="w-full bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 placeholder:text-slate-400 focus:outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1.5">Description</label>
                                <textarea
                                    rows={3}
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    placeholder="Brief overview of project goals..."
                                    className="w-full bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 placeholder:text-slate-400 focus:outline-none resize-none"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1.5">Priority</label>
                                    <select
                                        value={priority}
                                        onChange={(e) => setPriority(e.target.value as ProjectPriority)}
                                        className="w-full bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    >
                                        <option value="LOW">Low</option>
                                        <option value="MEDIUM">Medium</option>
                                        <option value="HIGH">High</option>
                                        <option value="URGENT">Urgent</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1.5">Due Date *</label>
                                    <input
                                        type="date"
                                        required
                                        value={dueDate}
                                        onChange={(e) => setDueDate(e.target.value)}
                                        className="w-full bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="flex justify-end gap-2.5 pt-2">
                                <button
                                    type="button"
                                    onClick={() => setIsCreateOpen(false)}
                                    className="px-4 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-zinc-400 hover:bg-slate-100 dark:hover:bg-zinc-800"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={creating}
                                    className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 text-white dark:text-slate-900 rounded-xl text-xs font-semibold shadow-sm"
                                >
                                    {creating ? "Creating..." : "Create Project"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </AppShell>
    );
}
