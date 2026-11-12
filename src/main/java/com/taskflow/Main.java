
package com.taskflow;

import com.taskflow.db.DatabaseManager;

import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            config.routes.get("/", ctx -> {
                ctx.result("Willkommen bei TaskFlow!");
            });

            config.routes.get("/api/tasks", ctx -> {
                ctx.result("Hier werden die Aufgaben angezeigt.");
            });

        }).start(7070);

        DatabaseManager dbManager = new DatabaseManager();
        System.out.println("Program started successfully, database is ready!");

    }
}
