package project.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTimeCalculationTest {
    private static TaskManager taskManager;

    @BeforeAll
    static void initializeTaskManager() {
        taskManager = Managers.getDefaultTaskManager();
    }

    @Test
    void taskShouldHaveCorrectTimeAttributes() {
        LocalDateTime expectedStartTime = LocalDateTime.of(2022, 5, 10, 9, 0);
        Duration expectedDuration = Duration.ofHours(2);
        LocalDateTime expectedEndTime = expectedStartTime.plus(expectedDuration);

        Task task = new Task.Builder()
                .setName("Test Task")
                .setDescription("Task for testing time attributes")
                .setStartTime(expectedStartTime)
                .setDuration(expectedDuration)
                .build();

        assertAll(
                () -> assertTrue(task.getStartTime().isPresent()),
                () -> assertEquals(expectedStartTime, task.getStartTime().get()),
                () -> assertTrue(task.getDuration().isPresent()),
                () -> assertEquals(expectedDuration, task.getDuration().get()),
                () -> assertTrue(task.getEndTime().isPresent()),
                () -> assertEquals(expectedEndTime, task.getEndTime().get())
        );
    }

    @Test
    void subtaskShouldHaveCorrectTimeAttributes() {
        LocalDateTime expectedStartTime = LocalDateTime.of(2022, 5, 15, 14, 30);
        Duration expectedDuration = Duration.ofMinutes(90);
        LocalDateTime expectedEndTime = expectedStartTime.plus(expectedDuration);

        Subtask subtask = new Subtask.Builder()
                .setName("Test Subtask")
                .setDescription("Subtask for testing time attributes")
                .setStartTime(expectedStartTime)
                .setDuration(expectedDuration)
                .build();

        assertAll(
                () -> assertTrue(subtask.getStartTime().isPresent()),
                () -> assertEquals(expectedStartTime, subtask.getStartTime().get()),
                () -> assertTrue(subtask.getDuration().isPresent()),
                () -> assertEquals(expectedDuration, subtask.getDuration().get()),
                () -> assertTrue(subtask.getEndTime().isPresent()),
                () -> assertEquals(expectedEndTime, subtask.getEndTime().get())
        );
    }

    @Test
    void shouldCorrectlyCalculateEpicTimeWithSingleSubtask() {
        Epic epic = new Epic.Builder()
                .setName("Simple Epic")
                .setDescription("Epic with single subtask")
                .build();
        Epic savedEpic = taskManager.addEpic(epic);
        LocalDateTime expectedStartTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        Duration expectedDuration = Duration.ofHours(1);
        Subtask subtask = new Subtask.Builder()
                .setName("Single subtask")
                .setDescription("Description")
                .setStartTime(expectedStartTime)
                .setDuration(expectedDuration)
                .build();


        taskManager.addSubtask(subtask, savedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(savedEpic.getId());


        assertTrue(updatedEpic.getStartTime().isPresent());
        assertEquals(expectedStartTime, updatedEpic.getStartTime().get());
        assertTrue(updatedEpic.getDuration().isPresent());
        assertEquals(expectedDuration, updatedEpic.getDuration().get());
    }

    @Test
    void shouldCorrectlyCalculateEpicTimeWithMultipleSubtasks() {
        Epic epic = new Epic.Builder()
                .setName("Epic with multiple subtasks")
                .setDescription("Complex epic")
                .build();
        Epic savedEpic = taskManager.addEpic(epic);
        Subtask firstSubtask = new Subtask.Builder()
                .setName("First subtask")
                .setDescription("First subtask description")
                .setStartTime(LocalDateTime.of(2020, 1, 1, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask secondSubtask = new Subtask.Builder()
                .setName("Second subtask")
                .setDescription("Second subtask description")
                .setStartTime(LocalDateTime.of(2020, 1, 1, 13, 0))
                .setDuration(Duration.ofHours(1))
                .build();

        taskManager.addSubtask(firstSubtask, savedEpic.getId());
        taskManager.addSubtask(secondSubtask, savedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(savedEpic.getId());


        assertAll(
                () -> assertTrue(updatedEpic.getStartTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0), updatedEpic.getStartTime().get()),
                () -> assertTrue(updatedEpic.getEndTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2020, 1, 1, 14, 0), updatedEpic.getEndTime().get()),
                () -> assertTrue(updatedEpic.getDuration().isPresent()),
                () -> assertEquals(Duration.ofHours(3), updatedEpic.getDuration().get())
        );
    }

    @Test
    void epicWithoutSubtasksShouldHaveEmptyTimeAttributes() {
        Epic epic = new Epic.Builder()
                .setName("Epic without subtasks")
                .setDescription("Epic description")
                .build();

        Epic savedEpic = taskManager.addEpic(epic);

        assertAll(
                () -> assertFalse(savedEpic.getStartTime().isPresent()),
                () -> assertFalse(savedEpic.getEndTime().isPresent()),
                () -> assertFalse(savedEpic.getDuration().isPresent())
        );
    }

    @Test
    void epicWithSubtasksWithoutTimeShouldHaveEmptyTimeAttributes() {
        Epic epic = new Epic.Builder()
                .setName("Epic with subtasks without time")
                .setDescription("Epic description")
                .build();
        Epic savedEpic = taskManager.addEpic(epic);

        Subtask subtaskWithoutTime = new Subtask.Builder()
                .setName("Subtask without time")
                .setDescription("No time subtask")
                .build();

        taskManager.addSubtask(subtaskWithoutTime, savedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(savedEpic.getId());

        assertAll(
                () -> assertFalse(updatedEpic.getStartTime().isPresent()),
                () -> assertFalse(updatedEpic.getEndTime().isPresent()),
                () -> assertFalse(updatedEpic.getDuration().isPresent())
        );
    }

    @Test
    void epicTimeAttributesShouldBeEmptyAfterRemovingLastSubtask() {
        Epic epic = new Epic.Builder()
                .setName("Epic with removable subtask")
                .setDescription("Epic description")
                .build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask = new Subtask.Builder()
                .setName("Temporary Subtask")
                .setDescription("Subtask to be removed")
                .setStartTime(LocalDateTime.of(2022, 4, 20, 12, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        Subtask addedSubtask = taskManager.addSubtask(subtask, addedEpic.getId());


        taskManager.deleteSubtask(addedSubtask.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());


        assertAll(
                () -> assertFalse(updatedEpic.getStartTime().isPresent()),
                () -> assertFalse(updatedEpic.getEndTime().isPresent()),
                () -> assertFalse(updatedEpic.getDuration().isPresent())
        );
    }

    @Test
    void epicTimeAttributesShouldBeCorrectAfterRemovingOneOfMultipleSubtasks() {
        Epic epic = new Epic.Builder()
                .setName("Epic with multiple subtasks")
                .setDescription("Epic description")
                .build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask firstSubtask = new Subtask.Builder()
                .setName("First Subtask")
                .setDescription("First subtask")
                .setStartTime(LocalDateTime.of(2022, 4, 20, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask secondSubtask = new Subtask.Builder()
                .setName("Second Subtask")
                .setDescription("Second subtask")
                .setStartTime(LocalDateTime.of(2022, 4, 20, 15, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        Subtask addedFirstSubtask = taskManager.addSubtask(firstSubtask, addedEpic.getId());
        taskManager.addSubtask(secondSubtask, addedEpic.getId());


        taskManager.deleteSubtask(addedFirstSubtask.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());


        assertAll(
                () -> assertTrue(updatedEpic.getStartTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2022, 4, 20, 15, 0), updatedEpic.getStartTime().get()),
                () -> assertTrue(updatedEpic.getEndTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2022, 4, 20, 16, 0), updatedEpic.getEndTime().get()),
                () -> assertTrue(updatedEpic.getDuration().isPresent()),
                () -> assertEquals(Duration.ofHours(1), updatedEpic.getDuration().get())
        );
    }

    @Test
    void epicTimeShouldUpdateWhenSubtaskTimeIsChanged() {
        Epic epic = new Epic.Builder()
                .setName("Epic with modifiable subtask")
                .setDescription("Description")
                .build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask = new Subtask.Builder()
                .setName("Initial subtask")
                .setDescription("Subtask")
                .setStartTime(LocalDateTime.of(2023, 1, 1, 9, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask addedSubtask = taskManager.addSubtask(subtask, addedEpic.getId());
        Subtask updatedSubtask = new Subtask.Builder()
                .fromSubtask(addedSubtask)
                .setStartTime(LocalDateTime.of(2023, 1, 1, 14, 0))
                .setDuration(Duration.ofHours(3))
                .build();

        taskManager.updateSubtask(updatedSubtask);
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertTrue(updatedEpic.getStartTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2023, 1, 1, 14, 0), updatedEpic.getStartTime().get()),
                () -> assertTrue(updatedEpic.getEndTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2023, 1, 1, 17, 0), updatedEpic.getEndTime().get()),
                () -> assertTrue(updatedEpic.getDuration().isPresent()),
                () -> assertEquals(Duration.ofHours(3), updatedEpic.getDuration().get())
        );
    }

    @Test
    void epicTimeShouldRemainCorrectWhenSubtaskWithoutTimeIsAdded() {
        Epic epic = new Epic.Builder()
                .setName("Epic with one timed and one untimed subtask")
                .setDescription("Test")
                .build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask timedSubtask = new Subtask.Builder()
                .setName("Timed Subtask")
                .setDescription("Timed")
                .setStartTime(LocalDateTime.of(2022, 10, 10, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask untimedSubtask = new Subtask.Builder()
                .setName("Untimed Subtask")
                .setDescription("Untimed")
                .build();

        taskManager.addSubtask(timedSubtask, addedEpic.getId());
        taskManager.addSubtask(untimedSubtask, addedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertTrue(updatedEpic.getStartTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2022, 10, 10, 10, 0), updatedEpic.getStartTime().get()),
                () -> assertTrue(updatedEpic.getEndTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2022, 10, 10, 12, 0), updatedEpic.getEndTime().get()),
                () -> assertTrue(updatedEpic.getDuration().isPresent()),
                () -> assertEquals(Duration.ofHours(2), updatedEpic.getDuration().get())
        );
    }

    @Test
    void epicTimeShouldRecalculateAfterAllSubtasksRemovedAndNewOnesAdded() {
        Epic epic = new Epic.Builder()
                .setName("Reused epic")
                .setDescription("Testing re-addition")
                .build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask.Builder()
                .setName("Subtask 1")
                .setDescription("First")
                .setStartTime(LocalDateTime.of(2022, 5, 1, 8, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask subtask2 = new Subtask.Builder()
                .setName("Subtask 2")
                .setDescription("Second")
                .setStartTime(LocalDateTime.of(2022, 5, 1, 11, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        Subtask subtask3 = new Subtask.Builder()
                .setName("Subtask 3")
                .setDescription("Third")
                .setStartTime(LocalDateTime.of(2022, 5, 2, 9, 0))
                .setDuration(Duration.ofHours(4))
                .build();


        Subtask a1 = taskManager.addSubtask(subtask1, addedEpic.getId());
        Subtask a2 = taskManager.addSubtask(subtask2, addedEpic.getId());
        taskManager.deleteSubtask(a1.getId());
        taskManager.deleteSubtask(a2.getId());
        taskManager.addSubtask(subtask3, addedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertTrue(updatedEpic.getStartTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2022, 5, 2, 9, 0), updatedEpic.getStartTime().get()),
                () -> assertTrue(updatedEpic.getEndTime().isPresent()),
                () -> assertEquals(LocalDateTime.of(2022, 5, 2, 13, 0), updatedEpic.getEndTime().get()),
                () -> assertTrue(updatedEpic.getDuration().isPresent()),
                () -> assertEquals(Duration.ofHours(4), updatedEpic.getDuration().get())
        );
    }
}