package project.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.exception.TaskOverlapException;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.Managers;
import project.util.TaskUtility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void setup() {
        taskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();
    }

    // ===== Тесты подсчета полей, связанных со временем =====
    @Test
    void taskShouldHaveCorrectTimeAttributes() {
        LocalDateTime expectedStartTime = LocalDateTime.of(2022, 5, 10, 9, 0);
        Duration expectedDuration = Duration.ofHours(2);
        LocalDateTime expectedEndTime = expectedStartTime.plus(expectedDuration);

        Task task = TaskUtility.createTaskBuilder()
                .setStartTime(expectedStartTime)
                .setDuration(expectedDuration)
                .build();

        assertAll(
                () -> assertEquals(expectedStartTime, task.getStartTime().orElseThrow()),
                () -> assertEquals(expectedDuration, task.getDuration().orElseThrow()),
                () -> assertEquals(expectedEndTime, task.getEndTime().orElseThrow())
        );
    }

    @Test
    void subtaskShouldHaveCorrectTimeAttributes() {
        LocalDateTime expectedStartTime = LocalDateTime.of(2022, 5, 15, 14, 30);
        Duration expectedDuration = Duration.ofMinutes(90);
        LocalDateTime expectedEndTime = expectedStartTime.plus(expectedDuration);

        Subtask subtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(expectedStartTime)
                .setDuration(expectedDuration)
                .build();

        assertAll(
                () -> assertEquals(expectedStartTime, subtask.getStartTime().orElseThrow()),
                () -> assertEquals(expectedDuration, subtask.getDuration().orElseThrow()),
                () -> assertEquals(expectedEndTime, subtask.getEndTime().orElseThrow())
        );
    }

    @Test
    void shouldCorrectlyCalculateEpicTimeWithSingleSubtask() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic savedEpic = taskManager.addEpic(epic);
        LocalDateTime expectedStartTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        Duration expectedDuration = Duration.ofHours(1);
        Subtask subtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(expectedStartTime)
                .setDuration(expectedDuration)
                .build();

        taskManager.addSubtask(subtask, savedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(savedEpic.getId());

        assertEquals(expectedStartTime, updatedEpic.getStartTime().orElseThrow());
        assertEquals(expectedDuration, updatedEpic.getDuration().orElseThrow());
    }

    @Test
    void shouldCorrectlyCalculateEpicTimeWithMultipleSubtasks() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic savedEpic = taskManager.addEpic(epic);
        Subtask firstSubtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2020, 1, 1, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask secondSubtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2020, 1, 1, 13, 0))
                .setDuration(Duration.ofHours(1))
                .build();

        taskManager.addSubtask(firstSubtask, savedEpic.getId());
        taskManager.addSubtask(secondSubtask, savedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(savedEpic.getId());

        assertAll(
                () -> assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0), updatedEpic.getStartTime().orElseThrow()),
                () -> assertEquals(LocalDateTime.of(2020, 1, 1, 14, 0), updatedEpic.getEndTime().orElseThrow()),
                () -> assertEquals(Duration.ofHours(3), updatedEpic.getDuration().orElseThrow())
        );
    }

    @Test
    void epicWithoutSubtasksShouldHaveEmptyTimeAttributes() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic savedEpic = taskManager.addEpic(epic);

        assertAll(
                () -> assertFalse(savedEpic.getStartTime().isPresent()),
                () -> assertFalse(savedEpic.getEndTime().isPresent()),
                () -> assertFalse(savedEpic.getDuration().isPresent())
        );
    }

    @Test
    void epicWithSubtasksWithoutTimeShouldHaveEmptyTimeAttributes() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic savedEpic = taskManager.addEpic(epic);

        Subtask subtaskWithoutTime = TaskUtility.createSubtaskBuilder().build();

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
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask = TaskUtility.createSubtaskBuilder()
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
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask firstSubtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2022, 4, 20, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask secondSubtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2022, 4, 20, 15, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        Subtask addedFirstSubtask = taskManager.addSubtask(firstSubtask, addedEpic.getId());
        taskManager.addSubtask(secondSubtask, addedEpic.getId());

        taskManager.deleteSubtask(addedFirstSubtask.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertEquals(LocalDateTime.of(2022, 4, 20, 15, 0), updatedEpic.getStartTime().orElseThrow()),
                () -> assertEquals(LocalDateTime.of(2022, 4, 20, 16, 0), updatedEpic.getEndTime().orElseThrow()),
                () -> assertEquals(Duration.ofHours(1), updatedEpic.getDuration().orElseThrow())
        );
    }

    @Test
    void epicTimeShouldUpdateWhenSubtaskTimeIsChanged() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2023, 1, 1, 9, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask addedSubtask = taskManager.addSubtask(subtask, addedEpic.getId());
        Subtask updatedSubtask = TaskUtility.createSubtaskBuilder()
                .fromSubtask(addedSubtask)
                .setStartTime(LocalDateTime.of(2023, 1, 1, 14, 0))
                .setDuration(Duration.ofHours(3))
                .build();

        taskManager.updateSubtask(updatedSubtask);
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertEquals(LocalDateTime.of(2023, 1, 1, 14, 0), updatedEpic.getStartTime().orElseThrow()),
                () -> assertEquals(LocalDateTime.of(2023, 1, 1, 17, 0), updatedEpic.getEndTime().orElseThrow()),
                () -> assertEquals(Duration.ofHours(3), updatedEpic.getDuration().orElseThrow())
        );
    }

    @Test
    void epicTimeShouldRemainCorrectWhenSubtaskWithoutTimeIsAdded() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask timedSubtask = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2022, 10, 10, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask untimedSubtask = TaskUtility.createSubtaskBuilder().build();

        taskManager.addSubtask(timedSubtask, addedEpic.getId());
        taskManager.addSubtask(untimedSubtask, addedEpic.getId());
        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertEquals(LocalDateTime.of(2022, 10, 10, 10, 0), updatedEpic.getStartTime().orElseThrow()),
                () -> assertEquals(LocalDateTime.of(2022, 10, 10, 12, 0), updatedEpic.getEndTime().orElseThrow()),
                () -> assertEquals(Duration.ofHours(2), updatedEpic.getDuration().orElseThrow())
        );
    }

    @Test
    void epicTimeShouldRecalculateAfterAllSubtasksRemovedAndNewOnesAddedWithoutIntersections() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic addedEpic = taskManager.addEpic(epic);

        Subtask subtask1 = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2025, 5, 1, 8, 0))
                .setDuration(Duration.ofHours(2))
                .build();

        Subtask subtask2 = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2025, 5, 1, 10, 0))
                .setDuration(Duration.ofHours(1))
                .build();

        Subtask subtask3 = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2025, 5, 2, 9, 0))
                .setDuration(Duration.ofHours(4))
                .build();

        Subtask a1 = taskManager.addSubtask(subtask1, addedEpic.getId());
        Subtask a2 = taskManager.addSubtask(subtask2, addedEpic.getId());
        taskManager.deleteSubtask(a1.getId());
        taskManager.deleteSubtask(a2.getId());
        taskManager.addSubtask(subtask3, addedEpic.getId());

        Epic updatedEpic = taskManager.getEpicWithNotification(addedEpic.getId());

        assertAll(
                () -> assertEquals(LocalDateTime.of(2025, 5, 2, 9, 0), updatedEpic.getStartTime().orElseThrow()),
                () -> assertEquals(LocalDateTime.of(2025, 5, 2, 13, 0), updatedEpic.getEndTime().orElseThrow()),
                () -> assertEquals(Duration.ofHours(4), updatedEpic.getDuration().orElseThrow())
        );
    }

    @Test
    void shouldThrowExceptionWhenSubtasksIntersect() {
        Epic epic = TaskUtility.createEpicBuilder().build();
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask1 = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2022, 5, 1, 8, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        Subtask subtask2 = TaskUtility.createSubtaskBuilder()
                .setStartTime(LocalDateTime.of(2022, 5, 1, 9, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        int epicId = addedEpic.getId();

        taskManager.addSubtask(subtask1, epicId);
        assertThrows(
                TaskOverlapException.class,
                () -> taskManager.addSubtask(subtask2, epicId)
        );
    }

    @Test
    void updateTask_withOverlappingTime_shouldThrowException() {
        Task task1 = TaskUtility.createTaskBuilder()
                .setStartTime(LocalDateTime.of(2024, 6, 1, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();
        taskManager.addTask(task1);
        Task task2 = TaskUtility.createTaskBuilder()
                .setStartTime(LocalDateTime.of(2024, 6, 1, 13, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        Task added2 = taskManager.addTask(task2);


        Task updated2 = new Task.Builder()
                .fromTask(added2)
                .setStartTime(LocalDateTime.of(2024, 6, 1, 11, 0))
                .build();

        assertThrows(
                TaskOverlapException.class,
                () -> taskManager.updateTask(updated2),
                "Task update must throw TaskOverlapException"
        );
    }

    @Test
    void deletedTask_shouldFreeTimeSlotForNewTask() {
        Task original = TaskUtility.createTaskBuilder()
                .setStartTime(LocalDateTime.of(2024, 7, 1, 9, 0))
                .setDuration(Duration.ofHours(1))
                .build();
        Task added = taskManager.addTask(original);


        taskManager.deleteTask(added.getId());
        Task newTask = TaskUtility.createTaskBuilder()
                .setStartTime(original.getStartTime().orElseThrow())
                .setDuration(original.getDuration().orElseThrow())
                .build();

        assertDoesNotThrow(
                () -> taskManager.addTask(newTask),
                "After deleting a task, its time slot should be available for a new task."
        );
    }

    @Test
    void getPrioritizedTasks_shouldReturnTasksSortedByStartTime() {
        Task task1 = TaskUtility.createTaskBuilder()
                .setName("Task A")
                .setStartTime(LocalDateTime.of(2024, 6, 1, 12, 0))
                .setDuration(Duration.ofHours(1))
                .build();

        Task task2 = TaskUtility.createTaskBuilder()
                .setName("Task B")
                .setStartTime(LocalDateTime.of(2024, 6, 1, 9, 0))
                .setDuration(Duration.ofHours(1))
                .build();

        Task task3 = TaskUtility.createTaskBuilder()
                .setName("Task C")
                .setStartTime(LocalDateTime.of(2024, 6, 1, 15, 0))
                .setDuration(Duration.ofHours(1))
                .build();

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        List<AbstractTask> prioritized = taskManager.getPrioritizedTasks();

        assertAll(
                () -> assertEquals("Task B", prioritized.getFirst().getName()),
                () -> assertEquals("Task A", prioritized.get(1).getName()),
                () -> assertEquals("Task C", prioritized.get(2).getName())
        );
    }

    @Test
    void taskWithoutTime_shouldNotAppearInPrioritizedList() {
        Task timedTask = TaskUtility.createTaskBuilder()
                .setName("Timed Task")
                .setStartTime(LocalDateTime.of(2024, 7, 1, 10, 0))
                .setDuration(Duration.ofHours(2))
                .build();

        Task untimedTask = TaskUtility.createTaskBuilder()
                .setName("Untimed Task")
                .build();

        taskManager.addTask(timedTask);
        taskManager.addTask(untimedTask);

        List<AbstractTask> prioritized = taskManager.getPrioritizedTasks();

        assertAll(
                () -> assertEquals(1, prioritized.size(), "Only timed tasks should be in the prioritized list"),
                () -> assertEquals("Timed Task", prioritized.getFirst().getName())
        );
    }

    // ===== Тест истории доступа =====
    @Test
    void accessedTask_shouldBePresentInHistoryAsLast() {
        Task added = taskManager.addTask(TaskUtility.createTask());

        taskManager.getTaskWithNotification(added.getId());
        List<AbstractTask> history = taskManager.getHistory();

        assertFalse(history.isEmpty(), "History must not be empty after access");
        TaskUtility.assertAbstractTaskEquals(added, history.getLast());
    }

    @Test
    void deleteTask_shouldRemoveItFromHistory() {
        Task task = taskManager.addTask(TaskUtility.createTask());
        taskManager.getTaskWithNotification(task.getId());
        taskManager.deleteTask(task.getId());

        assertFalse(taskManager.getHistory().contains(task), "Deleted task should be removed from history");
    }

    @Test
    void deleteEpic_shouldRemoveItFromHistory() {
        Epic epic = taskManager.addEpic(TaskUtility.createEpic());
        taskManager.getEpicWithNotification(epic.getId());
        taskManager.deleteEpic(epic.getId());

        assertFalse(taskManager.getHistory().contains(epic), "Deleted epic should be removed from history");
    }

    @Test
    void deleteSubtask_shouldRemoveItFromHistory() {
        Epic epic = taskManager.addEpic(TaskUtility.createEpic());
        Subtask subtask = taskManager.addSubtask(TaskUtility.createSubtask(), epic.getId());
        taskManager.getSubtaskWithNotification(subtask.getId());
        taskManager.deleteSubtask(subtask.getId());

        assertFalse(taskManager.getHistory().contains(subtask), "Deleted subtask should be removed from history");
    }
}
