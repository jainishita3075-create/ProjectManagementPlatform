import TaskCard from "./TaskCard";

interface Task {
    taskId: number;
    title: string;
    description: string;
    priority: string;
    assignee: string;
    dueDate: string | null;
}

interface KanbanColumnProps {
    title: string;
    tasks: Task[];
}

export default function KanbanColumn({
                                         title,
                                         tasks,
                                     }: KanbanColumnProps) {
    return (
        <div className="flex min-h-[500px] w-full flex-col rounded-xl bg-gray-100 p-4">

            <div className="mb-4 flex items-center justify-between">
                <h2 className="font-semibold text-gray-800">
                    {title}
                </h2>

                <span className="rounded-full bg-gray-200 px-2 py-1 text-xs">
          {tasks.length}
        </span>
            </div>

            <div className="flex flex-col gap-3">
                {tasks.map((task) => (
                    <TaskCard
                        key={task.taskId}
                        task={task}
                    />
                ))}
            </div>
        </div>
    );
}