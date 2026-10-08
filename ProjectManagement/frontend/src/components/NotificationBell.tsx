"use client";

import { useState, useEffect } from "react";
import { Bell, Check, Clock, CheckCheck } from "lucide-react";
import { NotificationItem } from "@/types";
import { apiFetch } from "@/lib/api";

export default function NotificationBell() {
    const [notifications, setNotifications] = useState<NotificationItem[]>([]);
    const [isOpen, setIsOpen] = useState(false);

    const fetchNotifications = async () => {
        try {
            const res = await apiFetch<NotificationItem[]>("/api/notifications?unreadOnly=false");
            if (res.success && res.data) {
                setNotifications(res.data);
            }
        } catch {
            // Background polling fail-safe
        }
    };

    useEffect(() => {
        fetchNotifications();
        const interval = setInterval(fetchNotifications, 15000); // 15s poll
        return () => clearInterval(interval);
    }, []);

    const markAsRead = async (id: number) => {
        try {
            await apiFetch(`/api/notifications/${id}/read`, { method: "PATCH" });
            setNotifications((prev) =>
                prev.map((n) => (n.id === id ? { ...n, read: true } : n))
            );
        } catch {
            // Ignore error
        }
    };

    const markAllAsRead = async () => {
        try {
            await apiFetch("/api/notifications/read-all", { method: "PATCH" });
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
        } catch {
            // Ignore error
        }
    };

    const unreadCount = notifications.filter((n) => !n.read).length;

    return (
        <div className="relative">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="relative p-2 rounded-xl bg-slate-100 dark:bg-[#181a20] border border-slate-200 dark:border-zinc-800 hover:bg-slate-200 dark:hover:bg-zinc-800 text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:text-zinc-200 transition-colors cursor-pointer"
                aria-label="View notifications"
            >
                <Bell className="w-4 h-4" />
                {unreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-amber-500 text-slate-950 text-[9px] font-bold w-4 h-4 rounded-full flex items-center justify-center animate-pulse">
                        {unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-84 bg-white dark:bg-[#16181d] border border-slate-200 dark:border-zinc-800 rounded-2xl shadow-2xl p-4 z-50 text-slate-900 dark:text-zinc-100">
                    <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-zinc-800">
                        <div className="flex items-center gap-2">
                            <h3 className="font-semibold text-xs text-slate-900 dark:text-zinc-200">Notifications</h3>
                            {unreadCount > 0 && (
                                <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-100 dark:bg-amber-950/50 text-amber-700 dark:text-amber-300 border border-amber-300 dark:border-amber-900/40 font-semibold">
                                    {unreadCount} new
                                </span>
                            )}
                        </div>
                        {unreadCount > 0 && (
                            <button
                                onClick={markAllAsRead}
                                className="text-[11px] text-slate-500 hover:text-slate-900 dark:text-zinc-400 dark:hover:text-zinc-200 flex items-center gap-1 cursor-pointer transition-colors"
                            >
                                <CheckCheck className="w-3.5 h-3.5" />
                                Mark all read
                            </button>
                        )}
                    </div>

                    <div className="max-h-72 overflow-y-auto mt-2 space-y-2 pr-1">
                        {notifications.length === 0 ? (
                            <p className="text-xs text-slate-400 dark:text-zinc-500 text-center py-6">No notifications yet</p>
                        ) : (
                            notifications.map((n) => (
                                <div
                                    key={n.id}
                                    className={`p-3 rounded-xl border text-xs transition-colors ${
                                        n.read
                                            ? "bg-slate-50 dark:bg-[#111317]/50 border-slate-200/60 dark:border-zinc-800/60 text-slate-500 dark:text-zinc-400"
                                            : "bg-amber-50/40 dark:bg-[#1a1d24] border-amber-200/60 dark:border-zinc-700/60 text-slate-800 dark:text-zinc-200"
                                    }`}
                                >
                                    <div className="flex justify-between items-start gap-2">
                                        <p className="font-semibold text-slate-900 dark:text-zinc-100">{n.title}</p>
                                        {!n.read && (
                                            <button
                                                onClick={() => markAsRead(n.id)}
                                                className="text-slate-400 hover:text-slate-700 dark:text-zinc-400 dark:hover:text-zinc-200 shrink-0 cursor-pointer p-0.5"
                                                title="Mark as read"
                                            >
                                                <Check className="w-3.5 h-3.5" />
                                            </button>
                                        )}
                                    </div>
                                    <p className="mt-1 text-slate-600 dark:text-zinc-400 text-[11px] leading-relaxed">{n.message}</p>
                                    <div className="flex items-center gap-1 mt-2 text-[10px] text-slate-400 dark:text-zinc-500">
                                        <Clock className="w-3 h-3" />
                                        <span>{new Date(n.sentAt).toLocaleDateString()}</span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
