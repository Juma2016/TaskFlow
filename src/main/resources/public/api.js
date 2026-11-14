const taskPath = "/api/tasks";

async function request(path, { method = "GET", data, signal } = {}) {
  const response = await fetch(path, {
    method,
    signal,
    ...(data === undefined ? {} : {
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    })
  });

  if (!response.ok) {
    const error = new Error("HTTP " + response.status);
    error.status = response.status;
    throw error;
  }

  return response.status === 204 ? null : response.json();
}

const taskUrl = (id) => taskPath + "/" + encodeURIComponent(id);

export const api = {
  list: (filters, signal) => request(taskPath + (filters.size ? "?" + filters : ""), { signal }),
  stats: (signal) => request(taskPath + "/stats", { signal }),
  details: (id, signal) => request(taskUrl(id), { signal }),
  create: (data) => request(taskPath, { method: "POST", data }),
  update: (id, data) => request(taskUrl(id), { method: "PUT", data }),
  remove: (id) => request(taskUrl(id), { method: "DELETE" }),
  backup: () => request(taskPath + "/backup", { method: "POST" })
};
