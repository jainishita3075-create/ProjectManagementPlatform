"use client";

import { useEffect, useState, use } from "react";
import AppShell from "@/components/AppShell";
import { apiFetch } from "@/lib/api";
import { Project, Task, PagedResponse, ProjectPriority, TaskStatus, ProjectStatus } from "@/types";
import {
    ChevronLeft,
    Calendar,
    Users,
    CheckSquare,
    Plus,
    Trash2,
    UserPlus,
    AlertCircle,
    X,
    Shield,
    CheckCircle2,
    Edit3,
    UserCheck,
    Archive
} from "lucide-react";
import Link from "next/link";

interface ProjectMember {
    membershipId: number;
    projectId: number;
    projectName: string;
    userId: number;
    username: string;
    name: string;
    email: string;
    memberRole: "OWNER" | "EDITOR" | "VIEWER";
    joinedAt: string;
}

export default function ProjectDetailsPage({ params }: { params: Promise<{ id: string }> }) {
    const resolvedParams = use(params);
    const projectId = Number(resolvedParams.id);

    const [project, setProject] = useState<Project | null>(null);
    const [tasks, setTasks] = useState<Task[]>([]);
    const [members, setMembers] = useState<ProjectMember[]>([]);
    const [allUsers, setAllUsers] = useState<{ userId: number; username: string; name: string; email: string }[]>([]);
    const [activeTab, setActiveTab] = useState<"tasks" | "members">("tasks");
    const [loading, setLoading] = useState(true);

    // Edit Project Modal
    const [isEditProjectOpen, setIsEditProjectOpen] = useState(false);
    const [editName, setEditName] = useState("");
    const [editDescription, setEditDescription] = useState("");
    const [editStatus, setEditStatus] = useState<ProjectStatus>("ACTIVE");
    const [editPriority, setEditPriority] = useState<ProjectPriority>("MEDIUM");
    const [editDueDate, setEditDueDate] = useState("");
    const [editProjectError, setEditProjectError] = useState<string | null>(null);

    // Add Member Modal
    const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
    const [memberUsername, setMemberUsername] = useState("");
    const [memberRole, setMemberRole] = useState<"EDITOR" | "VIEWER">("EDITOR");
    const [memberError, setMemberError] = useState<string | null>(null);

    // Create Task Modal
    const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false);
    const [taskTitle, setTaskTitle] = useState("");
    const [taskDescription, setTaskDescription] = useState("");
    const [taskPriority, setTaskPriority] = useState<ProjectPriority>("MEDIUM");
    const [taskDueDate, setTaskDueDate] = useState("");
    const [taskEffort, setTaskEffort] = useState("0");
    const [assignedUsername, setAssignedUsername] = useState("");
    const [taskError, setTaskError] = useState<string | null>(null);

    const loadProjectData = async () => {
        setLoading(true);
        try {
            const [projRes, tasksRes, membersRes, usersRes] = await Promise.all([
                apiFetch<Project>(`/api/projects/${projectId}`),
                apiFetch<PagedResponse<Task>>(`/api/tasks/project/${projectId}?size=50`),
                apiFetch<ProjectMember[]>(`/api/projects/${projectId}/members`),
                apiFetch<{ userId: number; username: string; name: string; email: string }[]>("/api/users"),
            ]);

            if (projRes.success && projRes.data) {
                setProject(projRes.data);
                setEditName(projRes.data.name);
                setEditDescription(projRes.data.description || "");
                setEditStatus(projRes.data.status);
                setEditPriority(projRes.data.priority);
                setEditDueDate(projRes.data.dueDate ? projRes.data.dueDate.split("T")[0] : "");
            }
            if (tasksRes.success && tasksRes.data) setTasks(tasksRes.data.content);
            if (membersRes.success && membersRes.data) setMembers(membersRes.data);
            if (usersRes.success && usersRes.data) {
                setAllUsers(usersRes.data);
                if (usersRes.data.length > 0 && !memberUsername) {
                    setMemberUsername(usersRes.data[0].username);
                }
            }
        } catch (err) {
            console.error("Failed to load project details", err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (projectId) loadProjectData();
    }, [projectId]);

    const handleUpdateProject = async (e: React.FormEvent) => {
        e.preventDefault();
        setEditProjectError(null);
        try {
            const payload = {
                name: editName,
                description: editDescription,
                status: editStatus,
                priority: editPriority,
                dueDate: editDueDate ? new Date(editDueDate).toISOString() : null,
            };

            const res = await apiFetch<Project>(`/api/projects/${projectId}`, {
                method: "PUT",
                body: JSON.stringify(payload),
            });

            if (res.success && res.data) {
                setProject(res.data);
                setIsEditProjectOpen(false);
                loadProjectData();
            }
        } catch (err: any) {
            setEditProjectError(err.message || "Failed to update project");
        }
    };

    const handleArchiveProject = async () => {
        if (!confirm("Are you sure you want to archive this project?")) return;
        try {
            await apiFetch(`/api/projects/${projectId}/archive`, { method: "PATCH" });
            loadProjectData();
        } catch (err) {
            console.error("Failed to archive project", err);
        }
    };

    const handleAssignTask = async (taskId: number, username: string) => {
        if (!username) return;
        try {
            await apiFetch("/api/task-assignments", {
                method: "POST",
                body: JSON.stringify({ taskId, assignedUsername: username }),
            });
            loadProjectData();
        } catch (err) {
            console.error("Failed to assign task", err);
        }
    };

    const handleStatusChange = async (taskId: number, status: TaskStatus) => {
        try {
            await apiFetch(`/api/tasks/${taskId}/status`, {
                method: "PATCH",
                body: JSON.stringify({ status }),
            });
            loadProjectData();
        } catch (err) {
            console.error("Failed to update status", err);
        }
    };

    const handleDeleteTask = async (taskId: number) => {
        if (!confirm("Are you sure you want to delete this task?")) return;
        try {
            await apiFetch(`/api/tasks/${taskId}`, { method: "DELETE" });
            loadProjectData();
        } catch (err) {
            console.error("Failed to delete task", err);
        }
    };

    const handleAddMember = async (e: React.FormEvent) => {
        e.preventDefault();
        setMemberError(null);
        try {
            const res = await apiFetch<ProjectMember>(`/api/projects/${projectId}/members`, {
                method: "POST",
                body: JSON.stringify({ username: memberUsername, memberRole }),
            });
            if (res.success) {
                setIsAddMemberOpen(false);
                setMemberUsername("");
                loadProjectData();
            }
        } catch (err: any) {
            setMemberError(err.message || "Failed to add member");
        }
    };

    const handleRemoveMember = async (userId: number) => {
        if (!confirm("Are you sure you want to remove this member?")) return;
        try {
            await apiFetch(`/api/projects/${projectId}/members/${userId}`, { method: "DELETE" });
            loadProjectData();
        } catch (err) {
            console.error("Failed to remove member", err);
        }
    };

    const handleUpdateMemberRole = async (userId: number, role: "OWNER" | "EDITOR" | "VIEWER") => {
        try {
            await apiFetch(`/api/projects/${projectId}/members/${userId}`, {
                method: "PATCH",
                body: JSON.stringify({ memberRole: role }),
            });
            loadProjectData();
        } catch (err) {
            console.error("Failed to update role", err);
        }
    };

    const handleCreateTask = async (e: React.FormEvent) => {
        e.preventDefault();
        setTaskError(null);
        try {
            const payload = {
                projectId,
                title: taskTitle,
                description: taskDescription,
                priority: taskPriority,
                dueDate: new Date(taskDueDate).toISOString(),
                estimatedEffortHours: Number(taskEffort) || 0,
                assignedUsername: assignedUsername || null,
                status: "ASSIGNED",
            };

            const res = await apiFetch<Task>("/api/tasks", {
                method: "POST",
                body: JSON.stringify(payload),
            });

            if (res.success) {
                setIsCreateTaskOpen(false);
                setTaskTitle("");
                setTaskDescription("");
                setTaskDueDate("");
                setTaskEffort("0");
                setAssignedUsername("");
                loadProjectData();
            }
        } catch (err: any) {
            setTaskError(err.message || "Failed to create task");
        }
    };

    if (loading && !project) {
        return (
            <AppShell>
                <div className="flex items-center justify-center h-64 text-slate-500 dark:text-zinc-500 text-xs">
                    Loading project details...
                </div>
            </AppShell>
        );
    }

    if (!project) {
        return (
            <AppShell>
                <div className="text-center py-16 space-y-4">
                    <p className="text-sm text-slate-500 dark:text-zinc-400">Project not found or you don't have access.</p>
                    <Link
                        href="/projects"
                        className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 dark:bg-zinc-100 text-white dark:text-slate-900 text-xs font-semibold"
                    >
                        <ChevronLeft className="w-4 h-4" />
                        Back to Projects
                    </Link>
                </div>
            </AppShell>
        );
    }

    return (
        <AppShell>
            <div className="space-y-6 max-w-7xl mx-auto">
                {/* Back Button */}
                <div>
                    <Link
                        href="/projects"
                        className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-100 transition-colors"
                    >
                        <ChevronLeft className="w-4 h-4" />
                        Back to Projects
                    </Link>
                </div>

                {/* Project Header Card */}
                <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-6 space-y-6 shadow-sm">
                    <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
                        <div className="space-y-2">
                            <div className="flex flex-wrap items-center gap-2.5">
                                <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-zinc-100">{project.name}</h1>
                                <span className="px-2.5 py-0.5 rounded-md text-xs font-bold bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800/40">
                                    {project.status}
                                </span>
                                <span className="px-2.5 py-0.5 rounded-md text-xs font-bold bg-slate-100 dark:bg-zinc-800/60 text-slate-700 dark:text-zinc-300 border border-slate-200 dark:border-zinc-700/40">
                                    {project.priority}
                                </span>
                            </div>
                            <p className="text-xs text-slate-600 dark:text-zinc-400 max-w-2xl leading-relaxed">
                                {project.description || "No description provided."}
                            </p>
                        </div>

                        {/* Top Actions & Progress KPI */}
                        <div className="flex flex-col sm:flex-row lg:flex-col items-start lg:items-end gap-3.5">
                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => setIsEditProjectOpen(true)}
                                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-slate-200 dark:border-[#22252d] hover:bg-slate-50 dark:hover:bg-[#1a1d24] text-slate-700 dark:text-zinc-200 text-xs font-semibold transition-colors cursor-pointer shadow-sm"
                                >
                                    <Edit3 className="w-3.5 h-3.5 text-blue-500" />
                                    Edit Project
                                </button>
                                <button
                                    onClick={handleArchiveProject}
                                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-slate-200 dark:border-[#22252d] hover:bg-rose-50 dark:hover:bg-rose-950/20 text-slate-600 hover:text-rose-600 dark:text-zinc-400 dark:hover:text-rose-400 text-xs font-semibold transition-colors cursor-pointer shadow-sm"
                                >
                                    <Archive className="w-3.5 h-3.5" />
                                    Archive
                                </button>
                            </div>

                            {/* Progress KPI Box */}
                            <div className="w-full sm:w-64 p-3.5 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl space-y-2">
                                <div className="flex justify-between text-xs text-slate-600 dark:text-zinc-400 font-semibold">
                                    <span>Overall Progress</span>
                                    <span className="font-bold text-slate-900 dark:text-zinc-100">{project.progressPercentage}%</span>
                                </div>
                                <div className="w-full h-2 bg-slate-200 dark:bg-zinc-800 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-emerald-500 rounded-full transition-all duration-300"
                                        style={{ width: `${project.progressPercentage}%` }}
                                    />
                                </div>
                                <div className="flex justify-between text-[11px] text-slate-500 dark:text-zinc-500 font-medium pt-0.5">
                                    <span>{project.completedTasks}/{project.totalTasks} tasks completed</span>
                                    <span>Due {new Date(project.dueDate).toLocaleDateString()}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Meta Bar */}
                    <div className="flex flex-wrap items-center gap-6 pt-4 border-t border-slate-100 dark:border-[#1d2027] text-xs text-slate-600 dark:text-zinc-400 font-medium">
                        <div className="flex items-center gap-2">
                            <span className="text-slate-400 dark:text-zinc-500">Owner:</span>
                            <span className="font-semibold text-slate-900 dark:text-zinc-200">{project.ownerUsername}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <Calendar className="w-3.5 h-3.5 text-slate-400 dark:text-zinc-500" />
                            <span>Created: {new Date(project.createdAt).toLocaleDateString()}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <Users className="w-3.5 h-3.5 text-slate-400 dark:text-zinc-500" />
                            <span>{members.length} team members</span>
                        </div>
                    </div>
                </div>

                {/* Tabs Bar */}
                <div className="flex items-center justify-between border-b border-slate-200 dark:border-[#22252d] pb-2">
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => setActiveTab("tasks")}
                            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors cursor-pointer ${
                                activeTab === "tasks"
                                    ? "bg-slate-900 dark:bg-zinc-100 text-white dark:text-slate-900 shadow-sm"
                                    : "text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200"
                            }`}
                        >
                            <CheckSquare className="w-4 h-4" />
                            Tasks ({tasks.length})
                        </button>
                        <button
                            onClick={() => setActiveTab("members")}
                            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors cursor-pointer ${
                                activeTab === "members"
                                    ? "bg-slate-900 dark:bg-zinc-100 text-white dark:text-slate-900 shadow-sm"
                                    : "text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200"
                            }`}
                        >
                            <Users className="w-4 h-4" />
                            Team Members ({members.length})
                        </button>
                    </div>

                    {activeTab === "tasks" ? (
                        <button
                            onClick={() => setIsCreateTaskOpen(true)}
                            className="inline-flex items-center gap-1.5 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all shadow-sm cursor-pointer"
                        >
                            <Plus className="w-3.5 h-3.5" />
                            Add Task
                        </button>
                    ) : (
                        <button
                            onClick={() => setIsAddMemberOpen(true)}
                            className="inline-flex items-center gap-1.5 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all shadow-sm cursor-pointer"
                        >
                            <UserPlus className="w-3.5 h-3.5" />
                            Add Member
                        </button>
                    )}
                </div>

                {/* Tab 1: Tasks Content */}
                {activeTab === "tasks" && (
                    <div className="space-y-3">
                        {tasks.length === 0 ? (
                            <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl p-12 text-center shadow-sm">
                                <CheckSquare className="w-8 h-8 text-slate-400 dark:text-zinc-600 mx-auto mb-2.5" />
                                <p className="text-xs font-semibold text-slate-800 dark:text-zinc-300">No tasks in this project yet</p>
                                <p className="text-[11px] text-slate-500 dark:text-zinc-500 mt-1">Create your first task to track progress</p>
                            </div>
                        ) : (
                            tasks.map((task) => (
                                <div
                                    key={task.taskId}
                                    className="bg-white dark:bg-[#14161b] hover:bg-slate-50 dark:hover:bg-[#181a20] border border-slate-200 dark:border-[#22252d] rounded-xl p-4 transition-colors flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-sm"
                                >
                                    <div className="space-y-1.5 flex-1">
                                        <div className="flex items-center gap-2">
                                            <h4 className="text-xs font-semibold text-slate-900 dark:text-zinc-100">{task.title}</h4>
                                            <span className="text-[10px] px-2 py-0.5 rounded font-bold bg-slate-100 dark:bg-zinc-800/60 text-slate-600 dark:text-zinc-400 border border-slate-200 dark:border-zinc-700/40">
                                                {task.priority}
                                            </span>
                                        </div>
                                        {task.description && (
                                            <p className="text-[11px] text-slate-500 dark:text-zinc-400 line-clamp-1">{task.description}</p>
                                        )}
                                        <div className="flex flex-wrap items-center gap-4 text-[11px] text-slate-500 dark:text-zinc-400 pt-1">
                                            {/* Assignee Selector Dropdown */}
                                            <div className="flex items-center gap-1.5">
                                                <span className="font-semibold text-slate-600 dark:text-zinc-400">Assignee:</span>
                                                <select
                                                    value={task.asigneeUsername || ""}
                                                    onChange={(e) => handleAssignTask(task.taskId, e.target.value)}
                                                    className="bg-slate-100 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-lg px-2 py-0.5 text-xs text-slate-800 dark:text-zinc-200 focus:outline-none cursor-pointer"
                                                >
                                                    <option value="" disabled>Select Assignee</option>
                                                    {(allUsers.length > 0 ? allUsers : members).map((u) => (
                                                        <option key={u.userId} value={u.username}>
                                                            {u.name || u.username} ({u.username})
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>

                                            <span>Due: <strong className="text-slate-700 dark:text-zinc-300 font-semibold">{new Date(task.dueDate).toLocaleDateString()}</strong></span>
                                            {task.estimatedEffortHours > 0 && (
                                                <span>Est: <strong className="text-slate-700 dark:text-zinc-300 font-semibold">{task.estimatedEffortHours}h</strong></span>
                                            )}
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-2.5 shrink-0">
                                        <select
                                            value={task.status}
                                            onChange={(e) => handleStatusChange(task.taskId, e.target.value as TaskStatus)}
                                            className="bg-slate-100 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-lg px-2.5 py-1 text-[11px] font-semibold text-slate-800 dark:text-zinc-200 focus:outline-none cursor-pointer"
                                        >
                                            <option value="ASSIGNED">Assigned</option>
                                            <option value="IN_PROGRESS">In Progress</option>
                                            <option value="COMPLETED">Completed</option>
                                            <option value="OVERDUE">Overdue</option>
                                            <option value="BLOCKED">Blocked</option>
                                        </select>

                                        <button
                                            onClick={() => handleDeleteTask(task.taskId)}
                                            className="p-1.5 text-slate-400 hover:text-rose-600 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/20 rounded-lg transition-colors cursor-pointer"
                                            title="Delete task"
                                        >
                                            <Trash2 className="w-3.5 h-3.5" />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}

                {/* Tab 2: Members Content */}
                {activeTab === "members" && (
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl divide-y divide-slate-100 dark:divide-[#1d2027] overflow-hidden shadow-sm">
                        {members.map((member) => (
                            <div key={member.membershipId} className="p-4 flex items-center justify-between gap-4">
                                <div className="flex items-center gap-3">
                                    <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-zinc-800 border border-slate-200 dark:border-zinc-700/60 flex items-center justify-center text-xs font-bold text-slate-700 dark:text-zinc-300">
                                        {(member.name || member.username).charAt(0).toUpperCase()}
                                    </div>
                                    <div>
                                        <p className="text-xs font-semibold text-slate-900 dark:text-zinc-200">{member.name || member.username}</p>
                                        <p className="text-[11px] text-slate-500 dark:text-zinc-500">{member.email || member.username}</p>
                                    </div>
                                </div>

                                <div className="flex items-center gap-3">
                                    {member.memberRole === "OWNER" ? (
                                        <span className="flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[11px] font-bold bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-900/40">
                                            <Shield className="w-3 h-3" />
                                            Owner
                                        </span>
                                    ) : (
                                        <select
                                            value={member.memberRole}
                                            onChange={(e) => handleUpdateMemberRole(member.userId, e.target.value as "OWNER" | "EDITOR" | "VIEWER")}
                                            className="bg-slate-100 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-lg px-2.5 py-1 text-[11px] font-semibold text-slate-800 dark:text-zinc-300 focus:outline-none cursor-pointer"
                                        >
                                            <option value="EDITOR">Editor</option>
                                            <option value="VIEWER">Viewer</option>
                                        </select>
                                    )}

                                    {member.memberRole !== "OWNER" && (
                                        <button
                                            onClick={() => handleRemoveMember(member.userId)}
                                            className="p-1.5 text-slate-400 hover:text-rose-600 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/20 rounded-lg transition-colors cursor-pointer"
                                            title="Remove member"
                                        >
                                            <Trash2 className="w-3.5 h-3.5" />
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Edit Project Modal */}
            {isEditProjectOpen && (
                <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] w-full max-w-lg rounded-2xl p-6 shadow-2xl space-y-4">
                        <div className="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-[#1f222a]">
                            <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">Edit Project Settings</h2>
                            <button onClick={() => setIsEditProjectOpen(false)} className="text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200">
                                <X className="w-4 h-4" />
                            </button>
                        </div>

                        {editProjectError && (
                            <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                                <AlertCircle className="w-4 h-4 shrink-0" />
                                <span>{editProjectError}</span>
                            </div>
                        )}

                        <form onSubmit={handleUpdateProject} className="space-y-3.5">
                            <div>
                                <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Project Name *</label>
                                <input
                                    type="text"
                                    required
                                    value={editName}
                                    onChange={(e) => setEditName(e.target.value)}
                                    className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Description</label>
                                <textarea
                                    rows={3}
                                    value={editDescription}
                                    onChange={(e) => setEditDescription(e.target.value)}
                                    className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                />
                            </div>

                            <div className="grid grid-cols-3 gap-3">
                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Status</label>
                                    <select
                                        value={editStatus}
                                        onChange={(e) => setEditStatus(e.target.value as ProjectStatus)}
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    >
                                        <option value="ACTIVE">ACTIVE</option>
                                        <option value="COMPLETED">COMPLETED</option>
                                        <option value="ARCHIVED">ARCHIVED</option>
                                        <option value="BLOCKED">BLOCKED</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Priority</label>
                                    <select
                                        value={editPriority}
                                        onChange={(e) => setEditPriority(e.target.value as ProjectPriority)}
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    >
                                        <option value="LOW">LOW</option>
                                        <option value="MEDIUM">MEDIUM</option>
                                        <option value="HIGH">HIGH</option>
                                        <option value="URGENT">URGENT</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Due Date</label>
                                    <input
                                        type="date"
                                        value={editDueDate}
                                        onChange={(e) => setEditDueDate(e.target.value)}
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="flex justify-end gap-2 pt-3">
                                <button
                                    type="button"
                                    onClick={() => setIsEditProjectOpen(false)}
                                    className="px-4 py-2 border border-slate-200 dark:border-[#22252d] rounded-xl text-xs font-semibold text-slate-600 dark:text-zinc-400 hover:bg-slate-50 dark:hover:bg-zinc-800"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-semibold shadow-sm"
                                >
                                    Save Changes
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Create Task Modal */}
            {isCreateTaskOpen && (
                <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] w-full max-w-lg rounded-2xl p-6 shadow-2xl space-y-4">
                        <div className="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-[#1f222a]">
                            <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">Add Task to {project.name}</h2>
                            <button onClick={() => setIsCreateTaskOpen(false)} className="text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200">
                                <X className="w-4 h-4" />
                            </button>
                        </div>

                        {taskError && (
                            <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                                <AlertCircle className="w-4 h-4 shrink-0" />
                                <span>{taskError}</span>
                            </div>
                        )}

                        <form onSubmit={handleCreateTask} className="space-y-3">
                            <div>
                                <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Task Title *</label>
                                <input
                                    type="text"
                                    required
                                    value={taskTitle}
                                    onChange={(e) => setTaskTitle(e.target.value)}
                                    placeholder="e.g. Implement OAuth Flow"
                                    className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Description</label>
                                <textarea
                                    rows={2}
                                    value={taskDescription}
                                    onChange={(e) => setTaskDescription(e.target.value)}
                                    placeholder="Task specifications..."
                                    className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Priority</label>
                                    <select
                                        value={taskPriority}
                                        onChange={(e) => setTaskPriority(e.target.value as ProjectPriority)}
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    >
                                        <option value="LOW">LOW</option>
                                        <option value="MEDIUM">MEDIUM</option>
                                        <option value="HIGH">HIGH</option>
                                        <option value="URGENT">URGENT</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Due Date *</label>
                                    <input
                                        type="date"
                                        required
                                        value={taskDueDate}
                                        onChange={(e) => setTaskDueDate(e.target.value)}
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Estimated Effort (Hours)</label>
                                    <input
                                        type="number"
                                        value={taskEffort}
                                        onChange={(e) => setTaskEffort(e.target.value)}
                                        min="0"
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Assignee</label>
                                    <select
                                        value={assignedUsername}
                                        onChange={(e) => setAssignedUsername(e.target.value)}
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    >
                                        <option value="">Unassigned</option>
                                        {(allUsers.length > 0 ? allUsers : members).map((u) => (
                                            <option key={u.userId} value={u.username}>
                                                {u.name || u.username} ({u.username})
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            <div className="flex justify-end gap-2 pt-3">
                                <button
                                    type="button"
                                    onClick={() => setIsCreateTaskOpen(false)}
                                    className="px-4 py-2 border border-slate-200 dark:border-[#22252d] rounded-xl text-xs font-semibold text-slate-600 dark:text-zinc-400 hover:bg-slate-50 dark:hover:bg-zinc-800"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-semibold shadow-sm"
                                >
                                    Create Task
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Add Member Modal */}
            {isAddMemberOpen && (
                <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] w-full max-w-md rounded-2xl p-6 shadow-2xl space-y-4">
                        <div className="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-[#1f222a]">
                            <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">Add Team Member to {project.name}</h2>
                            <button onClick={() => setIsAddMemberOpen(false)} className="text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200">
                                <X className="w-4 h-4" />
                            </button>
                        </div>

                        {memberError && (
                            <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                                <AlertCircle className="w-4 h-4 shrink-0" />
                                <span>{memberError}</span>
                            </div>
                        )}

                        <form onSubmit={handleAddMember} className="space-y-3.5">
                            <div>
                                <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Select User *</label>
                                {allUsers.length > 0 ? (
                                    <select
                                        value={memberUsername}
                                        onChange={(e) => setMemberUsername(e.target.value)}
                                        required
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none cursor-pointer"
                                    >
                                        {allUsers.map((u) => (
                                            <option key={u.userId} value={u.username}>
                                                {u.name || u.username} ({u.username}) — {u.email}
                                            </option>
                                        ))}
                                    </select>
                                ) : (
                                    <input
                                        type="text"
                                        required
                                        value={memberUsername}
                                        onChange={(e) => setMemberUsername(e.target.value)}
                                        placeholder="Enter username (e.g. sarah_connor)"
                                        className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                )}
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-700 dark:text-zinc-300 mb-1">Project Role *</label>
                                <select
                                    value={memberRole}
                                    onChange={(e) => setMemberRole(e.target.value as "EDITOR" | "VIEWER")}
                                    className="w-full bg-white dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                >
                                    <option value="EDITOR">Editor (Can create & edit tasks)</option>
                                    <option value="VIEWER">Viewer (Read-only)</option>
                                </select>
                            </div>

                            <div className="flex justify-end gap-2 pt-3">
                                <button
                                    type="button"
                                    onClick={() => setIsAddMemberOpen(false)}
                                    className="px-4 py-2 border border-slate-200 dark:border-[#22252d] rounded-xl text-xs font-semibold text-slate-600 dark:text-zinc-400 hover:bg-slate-50 dark:hover:bg-zinc-800"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-semibold shadow-sm"
                                >
                                    Add Member
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </AppShell>
    );
}
