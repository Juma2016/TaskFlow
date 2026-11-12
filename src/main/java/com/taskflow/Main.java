
package com.taskflow;

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
            // Returns a simple message to check whether the server is running.
            config.routes.get("/", ctx -> {
                ctx.result("Willkommen bei TaskFlow!");
            });

            // Get all tasks.
            // Returns the current task list as JSON.
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
            // Reads the JSON request, validates it and creates a task.
            config.routes.post("/api/tasks", ctx -> {

                // Convert the incoming JSON into a CreateTaskRequest object.
                CreateTaskRequest request =
                        ctx.bodyAsClass(CreateTaskRequest.class);

                // Validate the task data before creating the task.
                TaskValidator.validate(request);

                // Create the task using the TaskService.
                Task task = taskService.createTask(
                        request.getTitle(),
                        request.getDescription(),
                        request.getPriority(),
                        request.getDueDate()
                );

                // Return HTTP 201 Created with the new task as JSON.
                ctx.status(201);
                ctx.json(task);
            });

        });

        // Start the server on port 7070.
        app.start(7070);
    }
}
