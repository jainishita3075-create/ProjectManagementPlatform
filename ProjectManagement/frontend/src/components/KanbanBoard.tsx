const API = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/\/$/, "");

async function loadTasks(projectId: number, token: string) {
  const response = await fetch(
      `${API}/api/tasks/project/${projectId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
  );

  if (!response.ok) {
    throw new Error("Could not load tasks");
  }

  const result = await response.json();

  // Your backend returns ApiResponse<PagedResponse<TaskResponse>>
  return result.data.content;
}