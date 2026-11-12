package com.taskflow.validation;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.model.Priority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskValidatorTest {

    // A valid request should pass validation.
    @Test
    void shouldAcceptValidRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Test Task");
        request.setPriority(Priority.HOCH);

        assertDoesNotThrow(() -> TaskValidator.validate(request));
    }

    // A null request should be rejected.
    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TaskValidator.validate(null)
        );
    }

    // A missing title should be rejected.
    @Test
    void shouldRejectNullTitle() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setPriority(Priority.HOCH);

        assertThrows(
                IllegalArgumentException.class,
                () -> TaskValidator.validate(request)
        );
    }

    // An empty or blank title should be rejected.
    @Test
    void shouldRejectBlankTitle() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("   ");
        request.setPriority(Priority.HOCH);

        assertThrows(
                IllegalArgumentException.class,
                () -> TaskValidator.validate(request)
        );
    }

    // A missing priority should be rejected.
    @Test
    void shouldRejectNullPriority() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Test Task");

        assertThrows(
                IllegalArgumentException.class,
                () -> TaskValidator.validate(request)
        );
    }
}