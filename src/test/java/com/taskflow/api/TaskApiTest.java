
package com.taskflow.api;

import com.taskflow.dto.CreateTaskRequest;
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
}
