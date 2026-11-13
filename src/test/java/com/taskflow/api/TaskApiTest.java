package com.taskflow.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.db.DatabaseManager;
import com.taskflow.db.SqliteTaskRepository;
import com.taskflow.service.TaskService;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskApiTest {

    private Javalin app;
    private HttpClient client;
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    // Start a separate Javalin server before each test.
    @BeforeEach
    void setUp() {

        // Create a separate SQLite database for each test.
        DatabaseManager databaseManager = new DatabaseManager(
                "jdbc:sqlite:" + tempDir.resolve("test-taskflow.db").toAbsolutePath()
        );

        // Create the SQLite repository.
        SqliteTaskRepository repository =
                new SqliteTaskRepository(databaseManager);

        // Create the service using the repository.
        TaskService taskService = new TaskService(repository);

        // Create the Javalin application using TaskApp.
        app = TaskApp.createApp(taskService, tempDir.resolve("backups"));

        // Start the server on a random available port.
        app.start(0);

        int port = app.port();
        baseUrl = "http://localhost:" + port;

        client = HttpClient.newHttpClient();
    }

    // Stop the server after each test.
    @AfterEach
    void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    // Extract the generated task ID from a JSON response.
    private String extractTaskId(String responseBody) throws Exception {
        JsonNode json = objectMapper.readTree(responseBody);
        return json.get("id").asText();
    }

    // Test whether GET / returns the welcome text.
    @Test
    void shouldReturnWelcomeMessage() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertEquals("Willkommen bei TaskFlow!", response.body());
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

        String id = extractTaskId(response.body());

        assertNotNull(id);
        assertDoesNotThrow(() -> UUID.fromString(id));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + id))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(
                getRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Test Task"));
    }

    // Test whether GET /api/tasks supports query filters and rejects invalid filter values.
    @Test
    void shouldFilterTasksAndRejectInvalidFilterParams() throws Exception {

        String json = """
                {
                    "title": "Important Report",
                    "description": "Prepare slides",
                    "priority": "HOCH"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest filterRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks?status=OFFEN&priority=HOCH&keyword=Report&dueFilter=TODAY"))
                .GET()
                .build();

        HttpResponse<String> filterResponse = client.send(
                filterRequest,
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, filterResponse.statusCode());

        HttpRequest badStatusReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks?status=UNGUELTIG"))
                .GET()
                .build();
        assertEquals(400, client.send(badStatusReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest badPriorityReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks?priority=UNGUELTIG"))
                .GET()
                .build();
        assertEquals(400, client.send(badPriorityReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest badDueReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks?dueFilter=UNGUELTIG"))
                .GET()
                .build();
        assertEquals(400, client.send(badDueReq, HttpResponse.BodyHandlers.ofString()).statusCode());
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

        String unknownId = UUID.randomUUID().toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + unknownId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Task not found."));
    }

    // Test whether PUT updates an existing task successfully.
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

        HttpResponse<String> createResponse = client.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, createResponse.statusCode());

        String taskId = extractTaskId(createResponse.body());

        String updateJson = """
                {
                    "title": "Updated Task",
                    "description": "Updated Description",
                    "priority": "HOCH",
                    "status": "IN_BEARBEITUNG"
                }
                """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + taskId))
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

        HttpRequest notFoundPut = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + UUID.randomUUID()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();
        assertEquals(404, client.send(notFoundPut, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    // Test whether DELETE removes a task successfully.
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

        HttpResponse<String> createResponse = client.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, createResponse.statusCode());

        String taskId = extractTaskId(createResponse.body());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                deleteRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(204, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(
                getRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, getResponse.statusCode());
    }

    // Test whether DELETE returns 404 for a non-existing task.
    @Test
    void shouldReturn404WhenDeletingNonExistingTask() throws Exception {

        String unknownId = UUID.randomUUID().toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + unknownId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Task not found."));
    }

    // Test whether GET returns correct task statistics including overdue count.
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
        assertTrue(response.body().contains("\"overdue\":0"));
    }

    // Test whether POST /api/tasks/backup creates a JSON backup file.
    @Test
    void shouldCreateJsonBackupViaApi() throws Exception {

        HttpRequest backupRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/backup"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(
                backupRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("taskflow_backup_"));
    }
}
