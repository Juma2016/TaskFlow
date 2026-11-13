package com.taskflow;

import com.taskflow.api.TaskApp;
import com.taskflow.db.DatabaseManager;
import com.taskflow.db.SqliteTaskRepository;
import com.taskflow.db.TaskRepository;
import com.taskflow.service.TaskService;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {

        // Initialize the SQLite database.
        DatabaseManager databaseManager = new DatabaseManager();

        // Create the SQLite repository.
        TaskRepository taskRepository =
                new SqliteTaskRepository(databaseManager);

        // Create the service responsible for managing tasks.
        TaskService taskService = new TaskService(taskRepository);

        // Create and start the Javalin application on port 7070.
        Javalin app = TaskApp.createApp(taskService);
        app.start(7070);
    }
}
