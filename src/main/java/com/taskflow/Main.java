
package com.taskflow;

import com.taskflow.enums.TaskStatus;
import com.taskflow.model.Task;
import com.taskflow.service.TaskService;
import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.validation.TaskValidator;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {

        // Create the service responsible for managing tasks.
        TaskService taskService = new TaskService();

        // Create and configure the Javalin application.
        Javalin app = Javalin.create(config -> {

            // Handle invalid input and return HTTP 400.
            config.routes.exception(IllegalArgumentException.class,
                    (e, ctx) -> {
                        ctx.status(400);
                        ctx.json(java.util.Map.of(
                                "error", e.getMessage()
                        ));
                    });

            // Welcome endpoint.
            config.routes.get("/", ctx -> {
                ctx.result("Willkommen bei TaskFlow!");
            });

            // Get all tasks.
            config.routes.get("/api/tasks", ctx -> {
                ctx.json(taskService.getAllTasks());
            });

            // Get a task by its ID.
            config.routes.get("/api/tasks/{id}", ctx -> {

                long id = Long.parseLong(ctx.pathParam("id"));

                var task = taskService.getTaskById(id);

                if (task.isPresent()) {
                    ctx.json(task.get());
                } else {
                    ctx.status(404);
                    ctx.json(java.util.Map.of(
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

                long id = Long.parseLong(ctx.pathParam("id"));

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
                    ctx.json(java.util.Map.of(
                            "error", "Task not found."
                    ));
                }
            });

        });

        // Start the server on port 7070.
        app.start(7070);
    }
}
