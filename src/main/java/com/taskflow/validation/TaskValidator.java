
package com.taskflow.validation;

import com.taskflow.dto.CreateTaskRequest;

/**
 * Validates incoming task data before creating a task.
 */
public class TaskValidator {

    /**
     * Checks the required fields of a new task.
     *
     * @throws IllegalArgumentException if the request is invalid
     */
    public static void validate(CreateTaskRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Task data is required."
            );
        }

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Task title is required."
            );
        }

        if (request.getPriority() == null) {
            throw new IllegalArgumentException(
                    "Task priority is required."
            );
        }
    }
}
