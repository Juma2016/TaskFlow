package com.taskflow.api;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskFilterCriteria;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.TaskService;
import com.taskflow.validation.TaskValidator;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TaskApp {

    public static Javalin createApp(TaskService taskService) {
        return createApp(taskService, Paths.get("backups"));
    }

    public static Javalin createApp(TaskService taskService, Path backupDir) {

        return Javalin.create(config -> {

            // Serve frontend files from src/main/resources/public if present.
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/ui";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            // Handle invalid input and return HTTP 400.
            config.routes.exception(IllegalArgumentException.class,
                    (e, ctx) -> {
                        ctx.status(400);
                        ctx.json(Map.of(
                                "error", e.getMessage()
                        ));
                    });

            // Welcome endpoint.
            config.routes.get("/", ctx -> {
                ctx.result("Willkommen bei TaskFlow!");
            });

            // Get all tasks with optional filters.
            config.routes.get("/api/tasks", ctx -> {

                TaskFilterCriteria criteria = new TaskFilterCriteria();

                // Filter by status.
                String status = ctx.queryParam("status");
                if (status != null && !status.isBlank()) {
                    try {
                        criteria.setStatus(TaskStatus.valueOf(status.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid task status: " + status);
                    }
                }

                // Filter by priority.
                String priority = ctx.queryParam("priority");
                if (priority != null && !priority.isBlank()) {
                    try {
                        criteria.setPriority(Priority.valueOf(priority.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid task priority: " + priority);
                    }
                }

                // Search by keyword.
                String keyword = ctx.queryParam("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    criteria.setKeyword(keyword);
                }

                // Filter by due date.
                String dueFilter = ctx.queryParam("dueFilter");
                if (dueFilter != null && !dueFilter.isBlank()) {
                    try {
                        criteria.setDueFilter(
                                TaskFilterCriteria.DueFilter.valueOf(
                                        dueFilter.toUpperCase()
                                )
                        );
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid due filter: " + dueFilter);
                    }
                }

                ctx.json(taskService.getAllTasks(criteria));
            });

            // Get task statistics.
            config.routes.get("/api/tasks/stats", ctx -> {
                ctx.json(taskService.getTaskStats());
            });

            // Export JSON backup of all tasks.
            config.routes.post("/api/tasks/backup", ctx -> {
                Path file = taskService.exportBackup(backupDir);
                ctx.status(201);
                ctx.json(Map.of(
                        "message", "Backup created successfully.",
                        "file", file.getFileName().toString()
                ));
            });

            // Get a task by its ID.
            config.routes.get("/api/tasks/{id}", ctx -> {

                String id = ctx.pathParam("id");

                var task = taskService.getTaskById(id);

                if (task.isPresent()) {
                    ctx.json(task.get());
                } else {
                    ctx.status(404);
                    ctx.json(Map.of(
                            "error", "Task not found."
                    ));
                }
            });

            // Create a new task.
            config.routes.post("/api/tasks", ctx -> {

                CreateTaskRequest request =
                        ctx.bodyAsClass(CreateTaskRequest.class);

                TaskValidator.validate(request);

                Task task = taskService.createTask(
                        request.getTitle(),
                        request.getDescription(),
                        request.getPriority(),
                        request.getDueDate()
                );

                // Return HTTP 201 Created.
                ctx.status(201);
                ctx.json(task);
            });

            // Update an existing task.
            config.routes.put("/api/tasks/{id}", ctx -> {

                String id = ctx.pathParam("id");

                CreateTaskRequest request =
                        ctx.bodyAsClass(CreateTaskRequest.class);

                TaskValidator.validate(request);

                TaskStatus status = request.getStatus() != null
                        ? request.getStatus()
                        : TaskStatus.OFFEN;

                var updatedTask = taskService.updateTask(
                        id,
                        request.getTitle(),
                        request.getDescription(),
                        request.getPriority(),
                        request.getDueDate(),
                        status
                );

                if (updatedTask.isPresent()) {
                    ctx.json(updatedTask.get());
                } else {
                    ctx.status(404);
                    ctx.json(Map.of(
                            "error", "Task not found."
                    ));
                }
            });

            // Delete a task by its ID.
            config.routes.delete("/api/tasks/{id}", ctx -> {

                String id = ctx.pathParam("id");

                boolean deleted = taskService.deleteTask(id);

                if (deleted) {
                    ctx.status(204);
                } else {
                    ctx.status(404);
                    ctx.json(Map.of(
                            "error", "Task not found."
                    ));
                }
            });

        });
    }
}
