"use client";

import { useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { apiFetch } from "@/lib/api";
import { Task, Project, TaskStatus, ProjectPriority, PagedResponse } from "@/types";
import {
    CheckSquare,
    Plus,
    Search,
    Calendar,
    User,
    AlertCircle,
    X,
    MessageSquare,
    History,
    ArrowRight,
    ArrowLeft,
    Send,
    CheckCircle2,
    LayoutGrid,
    Table as TableIcon
} from "lucide-react";

interface CommentItem {
    commentId: number;
    taskId: number;
    authorUsername: string;
    authorName: string;
    comment: string;
    createdAt: string;
}

interface ActivityItem {
    id: number;
    taskId: number;
    taskTitle: string;
    username: string;
    actionType: string;
    oldValue: string | null;
    newValue: string | null;
    creationTime: string;
}

const COLUMNS: { id: TaskStatus; label: string; dotColor: string }[] = [
    { id: "ASSIGNED", label: "Assigned", dotColor: "bg-slate-400 dark:bg-zinc-400" },
    { id: "IN_PROGRESS", label: "In Progress", dotColor: "bg-amber-500" },
    { id: "COMPLETED", label: "Completed", dotColor: "bg-emerald-500" },
    { id: "OVERDUE", label: "Overdue", dotColor: "bg-rose-500" },
    { id: "BLOCKED", label: "Blocked", dotColor: "bg-violet-500" },
];

export default function TasksPage() {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [projects, setProjects] = useState<Project[]>([]);
    const [allUsers, setAllUsers] = useState<{ userId: number; username: string; name: string; email: string }[]>([]);
    const [loading, setLoading] = useState(true);
    const [viewMode, setViewMode] = useState<"board" | "table">("board");
    const [selectedProject, setSelectedProject] = useState<string>("ALL");
    const [selectedPriority, setSelectedPriority] = useState<string>("ALL");
    const [searchQuery, setSearchQuery] = useState("");
    const [draggedTaskId, setDraggedTaskId] = useState<number | null>(null);
    const [dragOverCol, setDragOverCol] = useState<TaskStatus | null>(null);

    // Selected Task Drawer
    const [selectedTask, setSelectedTask] = useState<Task | null>(null);
    const [drawerTab, setDrawerTab] = useState<"details" | "comments" | "activity">("details");
    const [comments, setComments] = useState<CommentItem[]>([]);
    const [activities, setActivities] = useState<ActivityItem[]>([]);
    const [newComment, setNewComment] = useState("");
    const [submittingComment, setSubmittingComment] = useState(false);
    const [editDueDate, setEditDueDate] = useState("");

    // Create Task Modal
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [createProjectId, setCreateProjectId] = useState<string>("");
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [priority, setPriority] = useState<ProjectPriority>("MEDIUM");
    const [dueDate, setDueDate] = useState("");
    const [effort, setEffort] = useState("0");
    const [assignee, setAssignee] = useState("");
    const [createError, setCreateError] = useState<string | null>(null);

    const loadData = async () => {
        setLoading(true);
        try {
            const [projRes, usersRes] = await Promise.all([
                apiFetch<PagedResponse<Project>>("/api/projects?size=50"),
                apiFetch<{ userId: number; username: string; name: string; email: string }[]>("/api/users"),
            ]);

            if (usersRes.success && usersRes.data) {
                setAllUsers(usersRes.data);
            }

            if (projRes.success && projRes.data) {
                setProjects(projRes.data.content);
                if (projRes.data.content.length > 0 && !createProjectId) {
                    setCreateProjectId(projRes.data.content[0].projectId.toString());
                }

                const taskPromises = projRes.data.content.map(async (p) => {
                    try {
                        const tRes = await apiFetch<PagedResponse<Task>>(`/api/tasks/project/${p.projectId}?size=50`);
                        return tRes.success && tRes.data ? tRes.data.content : [];
                    } catch {
                        return [];
                    }
                });

                const taskResults = await Promise.all(taskPromises);
                setTasks(taskResults.flat());
            }
        } catch (err) {
            console.error("Failed to load task board data", err);
        } finally {
            setLoading(false);
        }
    };

    const handleAssignTask = async (taskId: number, username: string) => {
        if (!username) return;
        try {
            await apiFetch("/api/task-assignments", {
                method: "POST",
                body: JSON.stringify({ taskId, assignedUsername: username }),
            });
            setTasks((prev) =>
                prev.map((t) => (t.taskId === taskId ? { ...t, asigneeUsername: username } : t))
            );
            if (selectedTask?.taskId === taskId) {
                setSelectedTask((prev) => (prev ? { ...prev, asigneeUsername: username } : null));
            }
        } catch (err) {
            console.error("Failed to assign task", err);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const openTaskDrawer = async (task: Task) => {
        setSelectedTask(task);
        setEditDueDate(task.dueDate ? task.dueDate.split("T")[0] : "");
        setDrawerTab("details");

        try {
            const [cRes, aRes] = await Promise.all([
                apiFetch<CommentItem[]>(`/api/tasks/${task.taskId}/comments`),
                apiFetch<ActivityItem[]>(`/api/tasks/${task.taskId}/activities`),
            ]);
            if (cRes.success && cRes.data) setComments(cRes.data);
            if (aRes.success && aRes.data) setActivities(aRes.data);
        } catch (e) {
            console.error(e);
        }
    };

    const handleUpdateStatus = async (taskId: number, newStatus: TaskStatus) => {
        try {
            const res = await apiFetch<Task>(`/api/tasks/${taskId}/status`, {
                method: "PATCH",
                body: JSON.stringify({ status: newStatus }),
            });
            if (res.success && res.data) {
                setTasks((prev) => prev.map((t) => (t.taskId === taskId ? res.data : t)));
                if (selectedTask?.taskId === taskId) setSelectedTask(res.data);
            }
        } catch (err) {
            console.error("Failed to update status", err);
        }
    };

    const handleUpdateDueDate = async () => {
        if (!selectedTask || !editDueDate) return;
        try {
            const res = await apiFetch<Task>(`/api/tasks/${selectedTask.taskId}/due-date`, {
                method: "PATCH",
                body: JSON.stringify({ dueDate: new Date(editDueDate).toISOString() }),
            });
            if (res.success && res.data) {
                setTasks((prev) => prev.map((t) => (t.taskId === selectedTask.taskId ? res.data : t)));
                setSelectedTask(res.data);
                alert("Due date updated successfully!");
            }
        } catch (err) {
            console.error("Failed to update due date", err);
        }
    };

    const handleAddComment = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedTask || !newComment.trim()) return;
        setSubmittingComment(true);

        const mentionMatches = newComment.match(/@(\w+)/g);
        const mentioned = mentionMatches ? mentionMatches.map((m) => m.replace("@", "")) : [];

        try {
            const res = await apiFetch<CommentItem>(`/api/tasks/${selectedTask.taskId}/comments`, {
                method: "POST",
                body: JSON.stringify({
                    comment: newComment,
                    mentionedUsername: mentioned,
                }),
            });

            if (res.success && res.data) {
                setComments((prev) => [...prev, res.data]);
                setNewComment("");
            }
        } catch (err) {
            console.error("Failed to add comment", err);
        } finally {
            setSubmittingComment(false);
        }
    };

    const handleCreateTask = async (e: React.FormEvent) => {
        e.preventDefault();
        setCreateError(null);

        try {
            const payload = {
                projectId: Number(createProjectId),
                title,
                description,
                priority,
                dueDate: new Date(dueDate).toISOString(),
                estimatedEffortHours: Number(effort) || 0,
                assignedUsername: assignee || null,
                status: "ASSIGNED",
            };

            const res = await apiFetch<Task>("/api/tasks", {
                method: "POST",
                body: JSON.stringify(payload),
            });

            if (res.success && res.data) {
                setTasks((prev) => [res.data, ...prev]);
                setIsCreateOpen(false);
                setTitle("");
                setDescription("");
                setDueDate("");
                setEffort("0");
                setAssignee("");
            }
        } catch (err: any) {
            setCreateError(err.message || "Failed to create task");
        }
    };

    const filteredTasks = tasks.filter((t) => {
        const matchesProject = selectedProject === "ALL" || t.projectId.toString() === selectedProject;
        const matchesPriority = selectedPriority === "ALL" || t.priority === selectedPriority;
        const matchesSearch = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
            (t.asigneeUsername && t.asigneeUsername.toLowerCase().includes(searchQuery.toLowerCase()));
        return matchesProject && matchesPriority && matchesSearch;
    });

    const getPriorityBadge = (p: ProjectPriority) => {
        switch (p) {
            case "URGENT": return "text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900/40";
            case "HIGH": return "text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900/40";
            case "MEDIUM": return "text-slate-600 dark:text-zinc-300 bg-slate-100 dark:bg-zinc-800/60 border-slate-200 dark:border-zinc-700/40";
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
                            <CheckSquare className="w-5 h-5 text-slate-600 dark:text-zinc-400" />
                            Kanban Tasks Board
                        </h1>
                        <p className="text-xs text-slate-500 dark:text-zinc-500 mt-1">
                            {tasks.length} total tasks across all active projects
                        </p>
                    </div>

                    <div className="flex items-center gap-2.5">
                        {/* View Mode Switcher */}
                        <div className="flex bg-slate-100 dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl p-0.5">
                            <button
                                onClick={() => setViewMode("board")}
                                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                                    viewMode === "board"
                                        ? "bg-white dark:bg-[#22252d] text-slate-900 dark:text-zinc-100 shadow-sm"
                                        : "text-slate-500 dark:text-zinc-400 hover:text-slate-800 dark:hover:text-zinc-200"
                                }`}
                            >
                                <LayoutGrid className="w-3.5 h-3.5" />
                                Board
                            </button>
                            <button
                                onClick={() => setViewMode("table")}
                                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                                    viewMode === "table"
                                        ? "bg-white dark:bg-[#22252d] text-slate-900 dark:text-zinc-100 shadow-sm"
                                        : "text-slate-500 dark:text-zinc-400 hover:text-slate-800 dark:hover:text-zinc-200"
                                }`}
                            >
                                <TableIcon className="w-3.5 h-3.5" />
                                Table
                            </button>
                        </div>

                        <button
                            onClick={() => setIsCreateOpen(true)}
                            className="inline-flex items-center gap-2 bg-slate-900 dark:bg-zinc-100 hover:bg-slate-800 dark:hover:bg-white text-white dark:text-slate-900 px-4 py-2 rounded-xl text-xs font-semibold transition-all shadow-sm cursor-pointer"
                        >
                            <Plus className="w-4 h-4" />
                            New Task
                        </button>
                    </div>
                </div>

                {/* Filter Toolbar */}
                <div className="flex flex-wrap items-center gap-3">
                    <div className="relative flex-1 min-w-[220px]">
                        <Search className="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 dark:text-zinc-500" />
                        <input
                            type="text"
                            placeholder="Filter by title or assignee..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl pl-9 pr-4 py-2 text-xs text-slate-800 dark:text-zinc-200 placeholder:text-slate-400 dark:placeholder:text-zinc-600 focus:outline-none focus:border-slate-400 dark:focus:border-zinc-500"
                        />
                    </div>

                    <select
                        value={selectedProject}
                        onChange={(e) => setSelectedProject(e.target.value)}
                        className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-zinc-300 focus:outline-none cursor-pointer"
                    >
                        <option value="ALL">All Projects</option>
                        {projects.map((p) => (
                            <option key={p.projectId} value={p.projectId.toString()}>
                                {p.name}
                            </option>
                        ))}
                    </select>

                    <select
                        value={selectedPriority}
                        onChange={(e) => setSelectedPriority(e.target.value)}
                        className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-zinc-300 focus:outline-none cursor-pointer"
                    >
                        <option value="ALL">All Priorities</option>
                        <option value="URGENT">Urgent</option>
                        <option value="HIGH">High</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                    </select>
                </div>

                {viewMode === "board" ? (
                    /* Kanban Columns Grid */
                    <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-3.5 items-start">
                        {COLUMNS.map((col) => {
                            const colTasks = filteredTasks.filter((t) => t.status === col.id);
                            const isOver = dragOverCol === col.id;

                            return (
                                <div
                                    key={col.id}
                                    onDragOver={(e) => {
                                        e.preventDefault();
                                        e.dataTransfer.dropEffect = "move";
                                        if (dragOverCol !== col.id) {
                                            setDragOverCol(col.id);
                                        }
                                    }}
                                    onDragLeave={(e) => {
                                        // Only reset if leaving the column itself
                                        if (!e.currentTarget.contains(e.relatedTarget as Node)) {
                                            if (dragOverCol === col.id) setDragOverCol(null);
                                        }
                                    }}
                                    onDrop={(e) => {
                                        e.preventDefault();
                                        const taskIdStr = e.dataTransfer.getData("text/plain");
                                        if (taskIdStr) {
                                            const tId = parseInt(taskIdStr, 10);
                                            if (!isNaN(tId)) {
                                                handleUpdateStatus(tId, col.id);
                                            }
                                        }
                                        setDraggedTaskId(null);
                                        setDragOverCol(null);
                                    }}
                                    className={`border rounded-2xl p-3.5 space-y-3 flex flex-col min-h-[440px] transition-all ${
                                        isOver
                                            ? "bg-blue-50/60 dark:bg-blue-950/20 border-blue-400 dark:border-blue-500/80 ring-2 ring-blue-400/30"
                                            : "bg-slate-100/70 dark:bg-[#14161b] border-slate-200/80 dark:border-[#22252d]"
                                    }`}
                                >
                                    {/* Column Header */}
                                    <div className="flex items-center justify-between pb-2 border-b border-slate-200 dark:border-[#1f222a]">
                                        <div className="flex items-center gap-2">
                                            <span className={`w-2.5 h-2.5 rounded-full ${col.dotColor}`} />
                                            <span className="text-xs font-semibold text-slate-800 dark:text-zinc-200">{col.label}</span>
                                        </div>
                                        <span className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 bg-white dark:bg-[#0f1115] px-2 py-0.5 rounded-md border border-slate-200 dark:border-[#22252d]">
                                            {colTasks.length}
                                        </span>
                                    </div>

                                    {/* Task Cards */}
                                    <div className="space-y-2.5 flex-1">
                                        {colTasks.map((task) => {
                                            const isThisDragged = draggedTaskId === task.taskId;
                                            return (
                                                <div
                                                    key={task.taskId}
                                                    draggable={true}
                                                    onDragStart={(e) => {
                                                        e.dataTransfer.setData("text/plain", task.taskId.toString());
                                                        e.dataTransfer.effectAllowed = "move";
                                                        setDraggedTaskId(task.taskId);
                                                    }}
                                                    onDragEnd={() => {
                                                        setDraggedTaskId(null);
                                                        setDragOverCol(null);
                                                    }}
                                                    onClick={() => openTaskDrawer(task)}
                                                    className={`group bg-white dark:bg-[#0f1115] hover:bg-slate-50 dark:hover:bg-[#16181e] border border-slate-200 dark:border-[#22252d] hover:border-slate-300 dark:hover:border-zinc-700/60 rounded-xl p-3.5 space-y-2.5 cursor-grab active:cursor-grabbing transition-all shadow-sm ${
                                                        isThisDragged ? "opacity-30 scale-95 ring-2 ring-blue-400" : ""
                                                    }`}
                                                >
                                                    <div className="flex items-start justify-between gap-2">
                                                        <h4 className="text-xs font-semibold text-slate-900 dark:text-zinc-100 group-hover:text-blue-600 dark:group-hover:text-white leading-snug">
                                                            {task.title}
                                                        </h4>
                                                        <span className={`px-1.5 py-0.5 rounded text-[9px] font-bold border shrink-0 ${getPriorityBadge(task.priority)}`}>
                                                            {task.priority}
                                                        </span>
                                                    </div>

                                                    {task.description && (
                                                        <p className="text-[11px] text-slate-500 dark:text-zinc-400 line-clamp-2 leading-relaxed">
                                                            {task.description}
                                                        </p>
                                                    )}

                                                    <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-[#1a1d24] text-[10px] text-slate-500 dark:text-zinc-400">
                                                        <div className="flex items-center gap-1">
                                                            <User className="w-3 h-3 text-slate-400" />
                                                            <span>{task.asigneeUsername ? task.asigneeUsername : "Unassigned"}</span>
                                                        </div>

                                                        <div className="flex items-center gap-1">
                                                            <Calendar className="w-3 h-3 text-slate-400" />
                                                            <span>{new Date(task.dueDate).toLocaleDateString(undefined, { month: "short", day: "numeric" })}</span>
                                                        </div>
                                                    </div>

                                                    {/* Column Transition Selector */}
                                                    <div
                                                        onClick={(e) => e.stopPropagation()}
                                                        className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-[#181a20]"
                                                    >
                                                        <span className="text-[10px] font-semibold text-slate-500 dark:text-zinc-400">Move:</span>
                                                        <select
                                                            value={task.status}
                                                            onChange={(e) => handleUpdateStatus(task.taskId, e.target.value as TaskStatus)}
                                                            className="text-[11px] font-medium bg-slate-100 dark:bg-[#1a1d24] text-slate-800 dark:text-zinc-200 border border-slate-200 dark:border-[#2a2e39] rounded-lg px-2 py-1 focus:outline-none focus:ring-1 focus:ring-blue-500 cursor-pointer"
                                                        >
                                                            <option value="ASSIGNED">Assigned</option>
                                                            <option value="IN_PROGRESS">In Progress</option>
                                                            <option value="COMPLETED">Completed</option>
                                                            <option value="OVERDUE">Overdue</option>
                                                            <option value="BLOCKED">Blocked</option>
                                                        </select>
                                                    </div>
                                                </div>
                                            );
                                        })}

                                        {colTasks.length === 0 && (
                                            <div className={`py-8 text-center border border-dashed rounded-xl text-[11px] transition-colors ${
                                                isOver
                                                    ? "border-blue-400 bg-blue-100/30 dark:bg-blue-900/20 text-blue-600 dark:text-blue-300 font-medium"
                                                    : "border-slate-200 dark:border-[#1f222a] text-slate-400 dark:text-zinc-600"
                                            }`}>
                                                {isOver ? "Drop task here" : "Drag tasks here"}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                ) : (
                    /* Table View */
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-2xl overflow-hidden shadow-sm">
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-xs text-slate-700 dark:text-zinc-300">
                                <thead className="bg-slate-50 dark:bg-[#101216] border-b border-slate-200 dark:border-[#22252d] text-[11px] font-semibold uppercase text-slate-500 dark:text-zinc-400 tracking-wider">
                                    <tr>
                                        <th className="px-4 py-3.5">Task Title</th>
                                        <th className="px-4 py-3.5">Status</th>
                                        <th className="px-4 py-3.5">Priority</th>
                                        <th className="px-4 py-3.5">Assignee</th>
                                        <th className="px-4 py-3.5">Due Date</th>
                                        <th className="px-4 py-3.5">Effort</th>
                                        <th className="px-4 py-3.5 text-right">Action</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100 dark:divide-[#1a1d24]">
                                    {filteredTasks.map((task) => (
                                        <tr
                                            key={task.taskId}
                                            className="hover:bg-slate-50/80 dark:hover:bg-[#181b22] transition-colors"
                                        >
                                            <td className="px-4 py-3.5">
                                                <div className="font-semibold text-slate-900 dark:text-zinc-100">{task.title}</div>
                                                {task.description && (
                                                    <div className="text-[11px] text-slate-500 dark:text-zinc-400 truncate max-w-md mt-0.5">
                                                        {task.description}
                                                    </div>
                                                )}
                                            </td>
                                            <td className="px-4 py-3.5">
                                                <select
                                                    value={task.status}
                                                    onChange={(e) => handleUpdateStatus(task.taskId, e.target.value as TaskStatus)}
                                                    className="text-[11px] font-medium bg-slate-100 dark:bg-[#1a1d24] text-slate-800 dark:text-zinc-200 border border-slate-200 dark:border-[#2a2e39] rounded-lg px-2 py-1 focus:outline-none focus:ring-1 focus:ring-blue-500 cursor-pointer"
                                                >
                                                    <option value="ASSIGNED">Assigned</option>
                                                    <option value="IN_PROGRESS">In Progress</option>
                                                    <option value="COMPLETED">Completed</option>
                                                    <option value="OVERDUE">Overdue</option>
                                                    <option value="BLOCKED">Blocked</option>
                                                </select>
                                            </td>
                                            <td className="px-4 py-3.5">
                                                <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getPriorityBadge(task.priority)}`}>
                                                    {task.priority}
                                                </span>
                                            </td>
                                            <td className="px-4 py-3.5">
                                                <div className="flex items-center gap-1.5 text-slate-800 dark:text-zinc-200 font-medium">
                                                    <User className="w-3.5 h-3.5 text-slate-400" />
                                                    <span>{task.asigneeUsername ? task.asigneeUsername : "Unassigned"}</span>
                                                </div>
                                            </td>
                                            <td className="px-4 py-3.5 whitespace-nowrap">
                                                <div className="flex items-center gap-1.5 text-slate-600 dark:text-zinc-400">
                                                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                                                    <span>{new Date(task.dueDate).toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })}</span>
                                                </div>
                                            </td>
                                            <td className="px-4 py-3.5 text-slate-600 dark:text-zinc-400 font-medium">
                                                {task.estimatedEffortHours ? `${task.estimatedEffortHours} hrs` : "—"}
                                            </td>
                                            <td className="px-4 py-3.5 text-right">
                                                <button
                                                    onClick={() => openTaskDrawer(task)}
                                                    className="px-3 py-1 bg-slate-100 dark:bg-zinc-800 hover:bg-slate-200 dark:hover:bg-zinc-700 text-slate-800 dark:text-zinc-200 rounded-lg text-xs font-semibold transition-colors cursor-pointer"
                                                >
                                                    View Details
                                                </button>
                                            </td>
                                        </tr>
                                    ))}

                                    {filteredTasks.length === 0 && (
                                        <tr>
                                            <td colSpan={7} className="text-center py-8 text-slate-400 dark:text-zinc-600">
                                                No tasks found matching your filter criteria.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>

            {/* Task Detail Drawer */}
            {selectedTask && (
                <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex justify-end">
                    <div className="bg-white dark:bg-[#14161b] border-l border-slate-200 dark:border-[#22252d] w-full max-w-lg h-full overflow-y-auto p-6 space-y-6 flex flex-col justify-between shadow-2xl">
                        <div className="space-y-6">
                            <div className="flex items-start justify-between gap-4">
                                <div>
                                    <span className="text-[10px] font-bold text-slate-400 dark:text-zinc-500 uppercase tracking-wider">
                                        Task #{selectedTask.taskId}
                                    </span>
                                    <h2 className="text-base font-semibold text-slate-900 dark:text-zinc-100 mt-1">
                                        {selectedTask.title}
                                    </h2>
                                </div>
                                <button
                                    onClick={() => setSelectedTask(null)}
                                    className="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-zinc-200 rounded-lg hover:bg-slate-100 dark:hover:bg-zinc-800"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>

                            <div className="grid grid-cols-2 gap-3 p-3.5 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl">
                                <div>
                                    <label className="block text-[10px] font-bold text-slate-500 dark:text-zinc-400 uppercase mb-1">Status</label>
                                    <select
                                        value={selectedTask.status}
                                        onChange={(e) => handleUpdateStatus(selectedTask.taskId, e.target.value as TaskStatus)}
                                        className="w-full bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-lg px-2.5 py-1.5 text-xs text-slate-900 dark:text-zinc-200 focus:outline-none"
                                    >
                                        <option value="ASSIGNED">Assigned</option>
                                        <option value="IN_PROGRESS">In Progress</option>
                                        <option value="COMPLETED">Completed</option>
                                        <option value="OVERDUE">Overdue</option>
                                        <option value="BLOCKED">Blocked</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-[10px] font-bold text-slate-500 dark:text-zinc-400 uppercase mb-1">Due Date</label>
                                    <div className="flex gap-1.5">
                                        <input
                                            type="date"
                                            value={editDueDate}
                                            onChange={(e) => setEditDueDate(e.target.value)}
                                            className="w-full bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-lg px-2 py-1 text-xs text-slate-900 dark:text-zinc-200 focus:outline-none"
                                        />
                                        <button
                                            onClick={handleUpdateDueDate}
                                            className="px-2.5 bg-slate-900 dark:bg-zinc-800 hover:bg-slate-800 dark:hover:bg-zinc-700 text-white rounded-lg text-xs"
                                        >
                                            Save
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <div className="flex border-b border-slate-200 dark:border-[#22252d] text-xs">
                                <button
                                    onClick={() => setDrawerTab("details")}
                                    className={`px-4 py-2 border-b-2 font-semibold transition-colors ${
                                        drawerTab === "details"
                                            ? "border-slate-900 dark:border-zinc-100 text-slate-900 dark:text-zinc-100"
                                            : "border-transparent text-slate-500 dark:text-zinc-400"
                                    }`}
                                >
                                    Overview
                                </button>
                                <button
                                    onClick={() => setDrawerTab("comments")}
                                    className={`px-4 py-2 border-b-2 font-semibold transition-colors flex items-center gap-1.5 ${
                                        drawerTab === "comments"
                                            ? "border-slate-900 dark:border-zinc-100 text-slate-900 dark:text-zinc-100"
                                            : "border-transparent text-slate-500 dark:text-zinc-400"
                                    }`}
                                >
                                    <MessageSquare className="w-3.5 h-3.5" />
                                    Comments ({comments.length})
                                </button>
                                <button
                                    onClick={() => setDrawerTab("activity")}
                                    className={`px-4 py-2 border-b-2 font-semibold transition-colors flex items-center gap-1.5 ${
                                        drawerTab === "activity"
                                            ? "border-slate-900 dark:border-zinc-100 text-slate-900 dark:text-zinc-100"
                                            : "border-transparent text-slate-500 dark:text-zinc-400"
                                    }`}
                                >
                                    <History className="w-3.5 h-3.5" />
                                    Audit Trail ({activities.length})
                                </button>
                            </div>

                            {drawerTab === "details" && (
                                <div className="space-y-4 text-xs text-slate-600 dark:text-zinc-400">
                                    <div>
                                        <h4 className="text-[11px] font-bold text-slate-400 dark:text-zinc-500 uppercase tracking-wider mb-1.5">Description</h4>
                                        <p className="bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl p-3.5 leading-relaxed text-slate-800 dark:text-zinc-200">
                                            {selectedTask.description || "No description provided."}
                                        </p>
                                    </div>

                                    <div className="grid grid-cols-2 gap-3">
                                        <div className="p-3.5 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl space-y-1">
                                            <span className="text-[10px] font-bold text-slate-400 dark:text-zinc-500 uppercase">Assignee</span>
                                            <select
                                                value={selectedTask.asigneeUsername || ""}
                                                onChange={(e) => handleAssignTask(selectedTask.taskId, e.target.value)}
                                                className="w-full bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] rounded-lg px-2.5 py-1 text-xs font-semibold text-slate-900 dark:text-zinc-100 focus:outline-none cursor-pointer"
                                            >
                                                <option value="">Unassigned</option>
                                                {allUsers.map((u) => (
                                                    <option key={u.userId} value={u.username}>
                                                        {u.name || u.username} ({u.username})
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                        <div className="p-3.5 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl space-y-1">
                                            <span className="text-[10px] font-bold text-slate-400 dark:text-zinc-500 uppercase">Estimated Hours</span>
                                            <p className="text-slate-900 dark:text-zinc-100 font-semibold">{selectedTask.estimatedEffortHours} hrs</p>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {drawerTab === "comments" && (
                                <div className="space-y-4">
                                    <div className="space-y-2.5 max-h-80 overflow-y-auto pr-1">
                                        {comments.length === 0 ? (
                                            <p className="text-xs text-slate-400 dark:text-zinc-500 text-center py-6">No comments yet.</p>
                                        ) : (
                                            comments.map((c) => (
                                                <div key={c.commentId} className="p-3 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl space-y-1">
                                                    <div className="flex justify-between text-[11px]">
                                                        <span className="font-semibold text-slate-800 dark:text-zinc-200">@{c.authorUsername}</span>
                                                        <span className="text-slate-400 dark:text-zinc-500">{new Date(c.createdAt).toLocaleDateString()}</span>
                                                    </div>
                                                    <p className="text-xs text-slate-700 dark:text-zinc-300">{c.comment}</p>
                                                </div>
                                            ))
                                        )}
                                    </div>

                                    <form onSubmit={handleAddComment} className="flex gap-2">
                                        <input
                                            type="text"
                                            placeholder="Write comment... (@username to mention)"
                                            value={newComment}
                                            onChange={(e) => setNewComment(e.target.value)}
                                            className="flex-1 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                        />
                                        <button
                                            type="submit"
                                            disabled={submittingComment || !newComment.trim()}
                                            className="px-3.5 bg-slate-900 dark:bg-zinc-100 text-white dark:text-slate-900 rounded-xl text-xs font-semibold disabled:opacity-50"
                                        >
                                            <Send className="w-3.5 h-3.5" />
                                        </button>
                                    </form>
                                </div>
                            )}

                            {drawerTab === "activity" && (
                                <div className="space-y-2 max-h-80 overflow-y-auto pr-1">
                                    {activities.length === 0 ? (
                                        <p className="text-xs text-slate-400 dark:text-zinc-500 text-center py-6">No audit history.</p>
                                    ) : (
                                        activities.map((a) => (
                                            <div key={a.id} className="p-3 bg-slate-50 dark:bg-[#0f1115] border border-slate-200 dark:border-[#22252d] rounded-xl text-xs space-y-1">
                                                <div className="flex justify-between text-[10px] text-slate-400 dark:text-zinc-500">
                                                    <span className="font-semibold text-slate-700 dark:text-zinc-300">@{a.username}</span>
                                                    <span>{new Date(a.creationTime).toLocaleString()}</span>
                                                </div>
                                                <p className="text-slate-800 dark:text-zinc-200">
                                                    Action: <strong className="font-semibold">{a.actionType}</strong>
                                                    {a.oldValue && a.newValue && (
                                                        <span> from <code className="text-rose-500">{a.oldValue}</code> to <code className="text-emerald-500">{a.newValue}</code></span>
                                                    )}
                                                </p>
                                            </div>
                                        ))
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Create Task Modal */}
            {isCreateOpen && (
                <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#14161b] border border-slate-200 dark:border-[#22252d] w-full max-w-lg rounded-2xl p-6 shadow-2xl space-y-4">
                        <div className="flex items-center justify-between">
                            <h2 className="text-sm font-semibold text-slate-900 dark:text-zinc-100">Create New Task</h2>
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

                        <form onSubmit={handleCreateTask} className="space-y-3">
                            <div>
                                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Select Project *</label>
                                <select
                                    value={createProjectId}
                                    onChange={(e) => setCreateProjectId(e.target.value)}
                                    required
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                >
                                    {projects.map((p) => (
                                        <option key={p.projectId} value={p.projectId.toString()}>
                                            {p.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Task Title *</label>
                                <input
                                    type="text"
                                    required
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    placeholder="e.g. Implement Database Indexing"
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 placeholder:text-slate-400 focus:outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Description</label>
                                <textarea
                                    rows={2}
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    placeholder="Task details and deliverables..."
                                    className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 placeholder:text-slate-400 focus:outline-none resize-none"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Priority</label>
                                    <select
                                        value={priority}
                                        onChange={(e) => setPriority(e.target.value as ProjectPriority)}
                                        className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    >
                                        <option value="LOW">Low</option>
                                        <option value="MEDIUM">Medium</option>
                                        <option value="HIGH">High</option>
                                        <option value="URGENT">Urgent</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Due Date *</label>
                                    <input
                                        type="date"
                                        required
                                        value={dueDate}
                                        onChange={(e) => setDueDate(e.target.value)}
                                        className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3.5 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Estimated Hours</label>
                                    <input
                                        type="number"
                                        step="0.5"
                                        min="0"
                                        value={effort}
                                        onChange={(e) => setEffort(e.target.value)}
                                        className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">Assignee</label>
                                    <select
                                        value={assignee}
                                        onChange={(e) => setAssignee(e.target.value)}
                                        className="w-full bg-slate-50 dark:bg-[#0c0e12] border border-slate-200 dark:border-[#22252d] rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-100 focus:outline-none cursor-pointer"
                                    >
                                        <option value="">Unassigned</option>
                                        {allUsers.map((u) => (
                                            <option key={u.userId} value={u.username}>
                                                {u.name || u.username} ({u.username})
                                            </option>
                                        ))}
                                    </select>
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
                                    className="px-4 py-2 bg-slate-900 dark:bg-zinc-100 text-white dark:text-slate-900 rounded-xl text-xs font-semibold shadow-sm"
                                >
                                    Create Task
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </AppShell>
    );
}
