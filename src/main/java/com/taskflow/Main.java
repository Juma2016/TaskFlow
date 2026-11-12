
package com.taskflow;

import com.taskflow.service.TaskService;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {

        // Create the service that manages tasks.
        TaskService taskService = new TaskService();

        Javalin app = Javalin.create(config -> {

            // Welcome endpoint.
            config.routes.get("/", ctx -> {
                ctx.result("Willkommen bei TaskFlow!");
            });

            // Returns all tasks as JSON.
            config.routes.get("/api/tasks", ctx -> {
                ctx.json(taskService.getAllTasks());
            });

        }).start(7070);
    }
}
