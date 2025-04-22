package project.util;

import org.junit.jupiter.api.function.Executable;
import project.enums.Status;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskUtility {

    public static Task.Builder createTaskBuilder() {
        return new Task.Builder()
                .setName("Task")
                .setDescription("TaskDescription")
                .setStatus(Status.NEW);
    }

    public static Task createTask() {
        return createTaskBuilder().build();
    }

    public static Epic.Builder createEpicBuilder() {
        return new Epic.Builder()
                .setName("Epic")
                .setDescription("EpicDescription")
                .setStatus(Status.NEW);

    }

    public static Epic createEpic() {
        return createEpicBuilder().build();
    }

    public static Subtask.Builder createSubtaskBuilder() {
        return new Subtask.Builder()
                .setName("Subtask")
                .setDescription("Subtask description")
                .setStatus(Status.NEW);
    }

    public static Subtask createSubtask() {
        return createSubtaskBuilder().build();
    }

    public static void assertAbstractTaskEquals(AbstractTask expected, AbstractTask actual) {
        List<Executable> checks = new ArrayList<>();

        checks.add(() -> assertEquals(
                expected.getClass(),
                actual.getClass(),
                "Class should match"));
        checks.add(() -> assertEquals(
                expected.getId(),
                actual.getId(),
                "ID should match"
        ));
        checks.add(() -> assertEquals(
                expected.getName(),
                actual.getName(),
                "Name should match"
        ));
        checks.add(() -> assertEquals(
                expected.getDescription(),
                actual.getDescription(),
                "Description should match"
        ));
        checks.add(() -> assertEquals(
                expected.getStatus(),
                actual.getStatus(),
                "Status should match"
        ));

        checks.add(() -> assertEquals(
                expected.getStartTime(),
                actual.getStartTime(),
                "Start time should match"
        ));
        checks.add(() -> assertEquals(
                expected.getDuration(),
                actual.getDuration(),
                "Duration should match"
        ));

        checks.add(() -> assertEquals(
                expected.getEndTime(),
                actual.getEndTime(),
                "End time should match"
        ));

        if (expected instanceof Subtask expectedSubtask && actual instanceof Subtask actualSubtask) {
            checks.add(() -> assertEquals(
                    expectedSubtask.getEpicId(),
                    actualSubtask.getEpicId(),
                    "Epic ID should match"
            ));
        }

        if (expected instanceof Epic expectedEpic && actual instanceof Epic actualEpic) {
            checks.add(() -> assertEquals(
                    expectedEpic.getSubtaskIds(),
                    actualEpic.getSubtaskIds(),
                    "Subtask IDs should match"
            ));
        }
        assertAll("AbstractTask fields", checks);
    }
}
