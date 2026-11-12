
package com.taskflow.api;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.enums.TaskStatus;
import com.taskflow.service.TaskService;
import com.taskflow.validation.TaskValidator;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class TaskApiTest {

    private Javalin app;
    private HttpClient client;
    private String baseUrl;

    // Start a separate Javalin server before each test.
    @BeforeEach
    void setUp() {

        TaskService taskService = new TaskService();

        app = Javalin.create(config -> {

            config.routes.exception(IllegalArgumentException.class,
                    (e, ctx) -> {
                        ctx.status(400);
                        ctx.json(java.util.Map.of(
                                "error", e.getMessage()
                        ));
                    });

            config.routes.get("/api/tasks", ctx -> {
                ctx.json(taskService.getAllTasks());
            });

            // Get task statistics.
            config.routes.get("/api/tasks/stats", ctx -> {
                ctx.json(taskService.getTaskStats());
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

            config.routes.post("/api/tasks", ctx -> {

                CreateTaskRequest request =
                        ctx.bodyAsClass(CreateTaskRequest.class);

                TaskValidator.validate(request);

                var task = taskService.createTask(
                        request.getTitle(),
                        request.getDescription(),
                        request.getPriority(),
                        request.getDueDate()
                );

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

            // Delete a task by its ID.
            config.routes.delete("/api/tasks/{id}", ctx -> {

                long id = Long.parseLong(ctx.pathParam("id"));

                boolean deleted = taskService.deleteTask(id);

                if (deleted) {
                    ctx.status(204);
                } else {
                    ctx.status(404);
                    ctx.json(java.util.Map.of(
                            "error", "Task not found."
                    ));
                }
            });
        });

        app.start(0);

        int port = app.port();
        baseUrl = "http://localhost:" + port;

        client = HttpClient.newHttpClient();
    }

    // Stop the server after each test.
    @AfterEach
    void tearDown() {
        app.stop();
    }

    // Test whether GET returns an empty task list initially.
    @Test
    void shouldReturnEmptyTaskList() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    // Test whether POST creates a task successfully.
    @Test
    void shouldCreateTaskSuccessfully() throws Exception {

        String json = """
                {
                    "title": "Test Task",
                    "description": "API testing",
                    "priority": "HOCH"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
        assertTrue(response.body().contains("HOCH"));
    }

    // Test whether invalid task data returns HTTP 400.
    @Test
    void shouldRejectTaskWithEmptyTitle() throws Exception {

        String json = """
                {
                    "title": "",
                    "description": "Invalid task",
                    "priority": "HOCH"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Task title is required."));
    }

    // Test whether a non-existing task returns HTTP 404.
    @Test
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Task not found."));
    }

    @Test
    void shouldUpdateTaskSuccessfully() throws Exception {

        String createJson = """
            {
                "title": "Old Task",
                "description": "Old Description",
                "priority": "NIEDRIG"
            }
            """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                .build();

        client.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        String updateJson = """
            {
                "title": "Updated Task",
                "description": "Updated Description",
                "priority": "HOCH",
                "status": "IN_BEARBEITUNG"
            }
            """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/1"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();

        HttpResponse<String> response = client.send(
                updateRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Updated Task"));
        assertTrue(response.body().contains("IN_BEARBEITUNG"));
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception {

        String json = """
            {
                "title": "Task to Delete",
                "description": "Testing DELETE endpoint",
                "priority": "HOCH"
            }
            """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                deleteRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(204, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(
                getRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTask() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Task not found."));
    }

    @Test
    void shouldReturnTaskStatistics() throws Exception {

        String json = """
            {
                "title": "Stats Test Task",
                "description": "Testing statistics",
                "priority": "HOCH"
            }
            """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        HttpRequest statsRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/stats"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                statsRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"total\":1"));
        assertTrue(response.body().contains("\"OFFEN\":1"));
    }
}
