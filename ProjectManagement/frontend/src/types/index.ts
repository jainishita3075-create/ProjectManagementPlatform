export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
}

export interface PagedResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export type ProjectStatus = "ACTIVE" | "PENDING" | "COMPLETED" | "ARCHIVED" | "BLOCKED";
export type ProjectPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";
export type TaskStatus = "ASSIGNED" | "IN_PROGRESS" | "COMPLETED" | "OVERDUE" | "CANCELLED" | "ARCHIVED" | "BLOCKED";
export type ProjectMemberRole = "LEAD" | "DEVELOPER" | "TESTER" | "VIEWER";

export interface User {
    userId: number;
    userName: string;
    name: string;
    email: string;
    roles: string[];
}

export interface Project {
    projectId: number;
    name: string;
    description: string;
    ownerUsername: string;
    startDate: string | null;
    endDate: string | null;
    dueDate: string;
    status: ProjectStatus;
    priority: ProjectPriority;
    totalTasks: number;
    completedTasks: number;
    progressPercentage: number;
    createdAt: string;
}

export interface Task {
    taskId: number;
    projectId: number;
    projectName: string;
    title: string;
    description: string;
    status: TaskStatus;
    priority: ProjectPriority;
    createdByUsername: string;
    startDate: string | null;
    dueDate: string;
    completionDate: string | null;
    estimatedEffortHours: number;
    assigneeUserId: number | null;
    asigneeUsername: string | null;
    asigneeName: string | null;
    createdAt: string;
}

export interface DashboardSummary {
    totalProjects: number;
    activeProjects: number;
    completedProjects: number;
    totalTasks: number;
    completedTasks: number;
    inProgressTasks: number;
    overdueTasks: number;
    workloadByUser: {
        userId: number;
        username: string;
        name: string;
        totalAssignedTasks: number;
        completedTasks: number;
        pendingTasks: number;
        overdueTasks: number;
    }[];
}

export interface ProjectProgress {
    projectId: number;
    projectName: string;
    status: ProjectStatus;
    priority: ProjectPriority;
    totalTasks: number;
    completedTasks: number;
    inProgressTasks: number;
    pendingTasks: number;
    overdueTasks: number;
    progressPercentage: number;
    totalEstimatedHours: number;
}

export interface TaskComment {
    commentId: number;
    taskId: number;
    authorUsername: string;
    authorName: string;
    comment: string;
    parentCommentId: number | null;
    createdAt: string;
}

export interface TaskActivity {
    taId: number;
    taskId: number;
    taskTitle: string;
    username: string;
    userFullName: string;
    projectId: number;
    projectName: string;
    actionType: string;
    oldValue: string | null;
    newValue: string | null;
    creationTime: string;
}

export interface NotificationItem {
    id: number;
    title: string;
    message: string;
    notificationType: string;
    sentByUsername: string;
    taskId: number | null;
    projectId: number | null;
    read: boolean;
    sentAt: string;
}
