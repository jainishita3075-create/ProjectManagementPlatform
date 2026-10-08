interface Task {
    taskId: number;
    title: string;
    description: string;
    priority: string;
    assignee: string;
    dueDate: string | null;
}

interface TaskCardProps {
    task: Task;
}

export default function TaskCard({ task }: TaskCardProps) {
    return (
        <div className="rounded-lg border bg-white p-4 shadow-sm">
            <h3 className="font-semibold text-gray-900">
                {task.title}
            </h3>

            <p className="mt-2 text-sm text-gray-600">
                {task.description}
            </p>

            <div className="mt-4 flex items-center justify-between">
        <span className="rounded-full bg-red-100 px-2 py-1 text-xs font-medium text-red-700">
          {task.priority}
        </span>

                <span className="text-sm text-gray-500">
          {task.assignee}
        </span>
            </div>

            {task.dueDate && (
                <p className="mt-3 text-xs text-gray-500">
                    Due: {task.dueDate}
                </p>
            )}
        </div>
    );
}