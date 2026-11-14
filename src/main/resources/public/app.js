import { api } from "./api.js";

const statusLabels = {
  OFFEN: "Offen",
  IN_BEARBEITUNG: "In Bearbeitung",
  ABGESCHLOSSEN: "Abgeschlossen"
};
const priorityLabels = { NIEDRIG: "Niedrig", MITTEL: "Mittel", HOCH: "Hoch" };

const byId = (id) => document.getElementById(id);
const filters = byId("filters");
const list = byId("task-list");
const editor = byId("editor");
const form = byId("task-form");
const titleInput = form.elements.namedItem("title");
const details = byId("details");
const deleteDialog = byId("delete-dialog");
const deleteForm = byId("delete-form");

let listRequest;
let statsRequest;
let detailRequest;
let searchTimer;
let editingId;
let deletingId;

function element(tag, className, text) {
  const item = document.createElement(tag);
  if (className) item.className = className;
  if (text !== undefined) item.textContent = text;
  return item;
}

function showMessage(target, message = "") {
  target.textContent = message;
  target.hidden = !message;
}

function notify(message = "", error = "") {
  showMessage(byId("message"), message);
  showMessage(byId("action-error"), error);
}

// Jackson may serialize LocalDateTime as an ISO string or an array.
function dateParts(value) {
  if (Array.isArray(value)) {
    return [
      value[0], value[1], value[2], value[3] ?? 0, value[4] ?? 0
    ].map((part, index) => String(part).padStart(index === 0 ? 4 : 2, "0"));
  }
  if (typeof value !== "string") return [];
  const match = /^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}))?/.exec(value);
  return match ? match.slice(1) : [];
}

function dateInputValue(value) {
  return dateParts(value).slice(0, 3).join("-");
}

function formatDate(value, includeTime = false) {
  const [year, month, day, hours, minutes] = dateParts(value);
  if (!year) return "–";
  const date = [day, month, year].join(".");
  return includeTime && hours ? date + ", " + hours + ":" + minutes + " Uhr" : date;
}

function localDateValue(date) {
  const pad = (part) => String(part).padStart(2, "0");
  return [date.getFullYear(), pad(date.getMonth() + 1), pad(date.getDate())].join("-");
}

function dueBadge(task) {
  if (!task.dueDate) return null;
  const text = formatDate(task.dueDate);
  const due = dateInputValue(task.dueDate);
  const today = localDateValue(new Date());
  if (task.status !== "ABGESCHLOSSEN") {
    if (due && due < today) return element("span", "badge past-due", "Überfällig · " + text);
    if (due === today) return element("span", "badge due-soon", "Heute fällig");
  }
  return element("span", "badge", "Fällig am " + text);
}

function taskButton(label, task, handler, extraClass = "") {
  const button = element("button", "button secondary " + extraClass, label);
  button.type = "button";
  button.setAttribute("aria-label", label + " – " + task.title);
  button.addEventListener("click", () => handler(task));
  return button;
}

function taskRow(task) {
  const row = element("li", "task-row");
  const summary = element("div", "task-summary");
  summary.append(element("h3", "", task.title));
  if (task.description) summary.append(element("p", "task-description", task.description));

  const facts = element("div", "task-facts");
  const statusStyle = {
    OFFEN: "offen",
    IN_BEARBEITUNG: "in-bearbeitung",
    ABGESCHLOSSEN: "abgeschlossen"
  };
  const priorityStyle = { NIEDRIG: "niedrig", MITTEL: "mittel", HOCH: "hoch" };
  facts.append(
    element("span", "badge " + (statusStyle[task.status] ?? ""), statusLabels[task.status] ?? "–"),
    element("span", "badge " + (priorityStyle[task.priority] ?? ""), "Priorität: " + (priorityLabels[task.priority] ?? "–"))
  );
  const due = dueBadge(task);
  if (due) facts.append(due);

  const actions = element("div", "task-actions");
  actions.append(
    taskButton("Details", task, openDetails),
    taskButton("Bearbeiten", task, openEditor),
    taskButton("Löschen", task, confirmDelete, "remove")
  );
  row.append(summary, facts, actions);
  return row;
}

function queryFilters() {
  const params = new URLSearchParams();
  for (const [name, value] of new FormData(filters)) {
    if (value.trim()) params.set(name, value.trim());
  }
  return params;
}

async function loadTasks() {
  clearTimeout(searchTimer);
  listRequest?.abort();
  const request = new AbortController();
  listRequest = request;
  const query = queryFilters();
  byId("list-error").hidden = true;
  byId("list-loading").hidden = false;
  byId("empty").hidden = true;
  byId("result-count").textContent = "";
  list.hidden = true;

  try {
    const tasks = await api.list(query, request.signal);
    if (request.signal.aborted) return;
    const rows = document.createDocumentFragment();
    tasks.forEach((task) => rows.append(taskRow(task)));
    list.replaceChildren(rows);
    byId("result-count").textContent = tasks.length + (tasks.length === 1 ? " Aufgabe" : " Aufgaben");
    if (tasks.length) {
      list.hidden = false;
    } else {
      byId("empty-title").textContent = query.size ? "Keine passenden Aufgaben" : "Noch keine Aufgaben";
      byId("empty-text").textContent = query.size
        ? "Ändere die Suche oder setze die Filter zurück."
        : "Lege über „Neue Aufgabe“ deine erste Aufgabe an.";
      byId("empty").hidden = false;
    }
  } catch {
    if (!request.signal.aborted) {
      list.replaceChildren();
      byId("list-error").hidden = false;
    }
  } finally {
    if (!request.signal.aborted) byId("list-loading").hidden = true;
  }
}

async function loadStats() {
  statsRequest?.abort();
  const request = new AbortController();
  statsRequest = request;
  const values = document.querySelectorAll("[data-stat]");
  showMessage(byId("stats-error"));
  try {
    const stats = await api.stats(request.signal);
    if (request.signal.aborted) return;
    values.forEach((node) => { node.textContent = stats[node.dataset.stat] ?? "–"; });
  } catch {
    if (request.signal.aborted) return;
    values.forEach((node) => { node.textContent = "–"; });
    showMessage(byId("stats-error"), "Die Statistik konnte nicht geladen werden.");
  }
}

function refresh() {
  return Promise.all([loadTasks(), loadStats()]);
}

function openEditor(task = null) {
  editingId = task?.id;
  form.reset();
  titleInput.setCustomValidity("");
  showMessage(byId("editor-error"));
  byId("editor-heading").textContent = task ? "Aufgabe bearbeiten" : "Neue Aufgabe";
  byId("status-field").hidden = !task;
  if (task) {
    for (const field of ["title", "description", "priority", "status"]) {
      form.elements.namedItem(field).value = task[field] ?? "";
    }
    form.elements.namedItem("dueDate").value = dateInputValue(task.dueDate);
  }
  editor.showModal();
}

function setBusy(dialog, busy) {
  dialog.setAttribute("aria-busy", String(busy));
  dialog.querySelectorAll("button, input, select, textarea").forEach((control) => {
    control.disabled = busy;
  });
}

function mutationError(error, fallback) {
  if (error.status === 404) return "Diese Aufgabe existiert nicht mehr. Lade die Liste neu.";
  if (error.status === 400) return "Die Eingabe wurde abgelehnt. Prüfe bitte die Angaben.";
  return fallback;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (editor.getAttribute("aria-busy") === "true") return;
  const values = new FormData(form);
  const title = values.get("title").trim();
  titleInput.setCustomValidity(title ? "" : "Bitte gib einen Titel ein.");
  if (!form.reportValidity()) return;

  const data = {
    title,
    description: values.get("description").trim() || null,
    priority: values.get("priority"),
    dueDate: values.get("dueDate") || null
  };
  const id = editingId;
  if (id) data.status = values.get("status");

  notify();
  showMessage(byId("editor-error"));
  setBusy(editor, true);
  try {
    if (id) await api.update(id, data);
    else await api.create(data);
  } catch (error) {
    showMessage(byId("editor-error"), mutationError(error, "Die Aufgabe konnte nicht gespeichert werden."));
    return;
  } finally {
    setBusy(editor, false);
  }

  editor.close();
  notify(id ? "Aufgabe gespeichert." : "Aufgabe erstellt.");
  if (id) byId("list-heading").focus();
  await refresh();
});

function fillDetails(task) {
  byId("detail-title").textContent = task.title;
  byId("detail-description").textContent = task.description || "Keine Beschreibung.";
  const fields = [
    ["Status", statusLabels[task.status]],
    ["Priorität", priorityLabels[task.priority]],
    ["Fälligkeitsdatum", formatDate(task.dueDate)],
    ["Erstellt am", formatDate(task.createdAt, true)],
    ["Aufgaben-ID", task.id]
  ];
  const metadata = fields.map(([label, value]) => {
    const group = element("div");
    group.append(element("dt", "", label), element("dd", "", value ?? "–"));
    return group;
  });
  byId("detail-meta").replaceChildren(...metadata);

  const entries = (task.history ?? []).map((change) => {
    const previous = statusLabels[change.oldStatus];
    const next = statusLabels[change.newStatus] ?? "–";
    const transition = previous ? previous + " → " + next : next;
    return element("li", "", formatDate(change.changedAt, true) + " · " + transition);
  });
  byId("history").replaceChildren(...entries);
  byId("history-empty").hidden = entries.length > 0;
}

async function openDetails(task) {
  detailRequest?.abort();
  const request = new AbortController();
  detailRequest = request;
  byId("detail-body").hidden = true;
  showMessage(byId("detail-feedback"), "Details werden geladen …");
  details.showModal();

  try {
    const result = await api.details(task.id, request.signal);
    if (request.signal.aborted) return;
    fillDetails(result);
    showMessage(byId("detail-feedback"));
    byId("detail-body").hidden = false;
  } catch (error) {
    if (!request.signal.aborted) {
      showMessage(byId("detail-feedback"), mutationError(error, "Die Details konnten nicht geladen werden."));
    }
  }
}

function confirmDelete(task) {
  deletingId = task.id;
  byId("delete-title").textContent = task.title;
  showMessage(byId("delete-error"));
  deleteDialog.showModal();
}

deleteForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (deleteDialog.getAttribute("aria-busy") === "true") return;
  notify();
  showMessage(byId("delete-error"));
  setBusy(deleteDialog, true);
  try {
    await api.remove(deletingId);
  } catch (error) {
    showMessage(byId("delete-error"), mutationError(error, "Die Aufgabe konnte nicht gelöscht werden."));
    return;
  } finally {
    setBusy(deleteDialog, false);
  }

  deleteDialog.close();
  notify("Aufgabe gelöscht.");
  byId("list-heading").focus();
  await refresh();
});

byId("backup").addEventListener("click", async (event) => {
  const button = event.currentTarget;
  button.disabled = true;
  notify();
  try {
    const backup = await api.backup();
    notify("Backup auf dem Server gespeichert: " + backup.file);
  } catch {
    notify("", "Das Backup konnte nicht erstellt werden.");
  } finally {
    button.disabled = false;
  }
});

filters.addEventListener("submit", (event) => {
  event.preventDefault();
  loadTasks();
});
filters.elements.namedItem("keyword").addEventListener("input", () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(loadTasks, 250);
});
filters.addEventListener("change", (event) => {
  if (event.target instanceof HTMLSelectElement) loadTasks();
});
filters.addEventListener("reset", () => setTimeout(loadTasks, 0));
byId("new-task").addEventListener("click", () => openEditor());
byId("retry").addEventListener("click", refresh);
titleInput.addEventListener("input", () => titleInput.setCustomValidity(""));
details.addEventListener("close", () => detailRequest?.abort());

for (const dialog of document.querySelectorAll("dialog")) {
  dialog.querySelector("[data-close]").addEventListener("click", () => dialog.close());
  dialog.addEventListener("cancel", (event) => {
    if (dialog.getAttribute("aria-busy") === "true") event.preventDefault();
  });
}

refresh();
