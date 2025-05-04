package project.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import project.enums.Status;
import project.exception.EntityAlreadyExistsException;
import project.exception.NonexistentEntityException;
import project.exception.TaskOverlapException;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskUtility;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TaskManagerTest<T extends TaskManager> {

    public static final int NONEXISTENT_ID = Integer.MIN_VALUE;
    protected T taskManager;


    // ===== Тесты получения списков задач =====
    @Test
    void getTasks_shouldReturnAllAddedTasks() {
        Task task1 = TaskUtility.createTask();
        Task task2 = TaskUtility.createTask();

        List<Task> tasksBefore = taskManager.getTasks();
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        List<Task> tasksAfter = taskManager.getTasks();

        assertAll("verify returned tasks",
                () -> assertNotNull(tasksAfter, "getTasks() should never return null"),
                () -> assertEquals(tasksBefore.size() + 2, tasksAfter.size(), "size should be exactly number of adds")
        );
    }

    @Test
    void getEpics_shouldReturnAllAddedEpics() {
        var epic1 = TaskUtility.createEpicBuilder().setName("Epic 1").build();
        var epic2 = TaskUtility.createEpicBuilder().setName("Epic 2").build();

        List<Epic> epicsBefore = taskManager.getEpics();
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        List<Epic> epicsAfter = taskManager.getEpics();

        assertAll("verify returned epics",
                () -> assertNotNull(epicsAfter, "getEpics() should never return null"),
                () -> assertEquals(epicsBefore.size() + 2, epicsAfter.size(), "size should increase by 2")
        );
    }

    @Test
    void getSubtasks_shouldReturnAllAddedSubtasks() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var subtask1 = TaskUtility.createSubtask();
        var subtask2 = TaskUtility.createSubtask();

        taskManager.addSubtask(subtask1, epicId);
        taskManager.addSubtask(subtask2, epicId);
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertTrue(subtasks.size() >= 2, "Should contain at least two added subtasks");
    }

    // ===== Тесты добавления сущностей с дублирующимися ID =====
    @Test
    void addTask_duplicateId_throwsException() {
        Task original = taskManager.addTask(TaskUtility.createTask());
        Task duplicate = new Task.Builder()
                .fromTask(original)
                .setName("Another Name")
                .setDescription("Same ID")
                .build();

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> taskManager.addTask(duplicate),
                "Adding a task with an existing ID must throw EntityAlreadyExistsException"
        );
    }

    @Test
    void addEpic_duplicateId_throwsException() {
        var original = taskManager.addEpic(TaskUtility.createEpic());
        var duplicate = TaskUtility.createEpicBuilder()
                .fromEpic(original)
                .setName("Other Name")
                .build();

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> taskManager.addEpic(duplicate),
                "Adding an epic with an existing ID must throw EntityAlreadyExistsException"
        );
    }

    @Test
    void addSubtask_withDuplicateId_shouldThrowException() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();
        var original = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);
        var duplicate = TaskUtility.createSubtaskBuilder()
                .fromSubtask(original)
                .setName("Duplicate Subtask")
                .build();

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> taskManager.addSubtask(duplicate, epicId),
                "Adding a subtask with duplicate ID must throw EntityAlreadyExistsException"
        );
    }

    // ===== Тесты удаления всех сущностей =====
    @Test
    void deleteTasks_allTasksRemoved() {
        int initialCount = 10;
        for (int i = 0; i < initialCount; i++) {
            taskManager.addTask(TaskUtility.createTask());
        }
        List<Task> beforeDelete = taskManager.getTasks();

        taskManager.deleteTasks();

        List<Task> afterDelete = taskManager.getTasks();
        assertAll("Verify tasks were cleared",
                () -> assertEquals(initialCount, beforeDelete.size(),
                        "Snapshot before delete should remain unchanged"),
                () -> assertTrue(afterDelete.isEmpty(),
                        "After deleteTasks(), no tasks should remain")
        );
    }

    @Test
    void deleteEpics_allEpicsRemoved() {
        int initialCount = 5;
        for (int i = 0; i < initialCount; i++) {
            taskManager.addEpic(TaskUtility.createEpic());
        }

        List<Epic> beforeDelete = taskManager.getEpics();
        taskManager.deleteEpics();
        List<Epic> afterDelete = taskManager.getEpics();

        assertAll("Verify epics were cleared",
                () -> assertEquals(initialCount, beforeDelete.size(), "Before delete size mismatch"),
                () -> assertTrue(afterDelete.isEmpty(), "After deleteEpics() list should be empty")
        );
    }

    @Test
    void deleteSubtasks_shouldRemoveAllSubtasks() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        for (int i = 0; i < 5; i++) {
            taskManager.addSubtask(TaskUtility.createSubtask(), epicId);
        }

        List<Subtask> beforeDelete = taskManager.getSubtasks();
        taskManager.deleteSubtasks();
        List<Subtask> afterDelete = taskManager.getSubtasks();

        assertAll("Subtasks should be cleared",
                () -> assertEquals(5, beforeDelete.size(), "Should initially have 5 subtasks"),
                () -> assertTrue(afterDelete.isEmpty(), "After deletion, subtasks list should be empty")
        );
    }

    // ===== Тесты получения сущности по ID =====
    @Test
    void getTaskByReturnedId_returnsSameTask() {
        Task task = TaskUtility.createTask();
        Task addedTask = taskManager.addTask(task);
        int taskId = addedTask.getId();

        Task receivedTask = taskManager.getTaskWithNotification(taskId);

        TaskUtility.assertAbstractTaskEquals(addedTask, receivedTask);
    }

    @Test
    void getEpicByReturnedId_returnsSameEpic() {
        var epic = TaskUtility.createEpic();
        var added = taskManager.addEpic(epic);
        int id = added.getId();

        var received = taskManager.getEpicWithNotification(id);

        TaskUtility.assertAbstractTaskEquals(added, received);
    }

    @Test
    void getSubtaskByReturnedId_shouldReturnSameSubtask() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var subtask = TaskUtility.createSubtask();
        var added = taskManager.addSubtask(subtask, epicId);
        int id = added.getId();

        var received = taskManager.getSubtaskWithNotification(id);
        TaskUtility.assertAbstractTaskEquals(added, received);
    }

    // ===== Тесты удаления сущности по ID =====
    @Test
    void deleteTask_shouldRemoveTaskFromManager() {
        Task task = TaskUtility.createTask();
        Task addedTask = taskManager.addTask(task);

        List<Task> beforeDelete = taskManager.getTasks();
        Task deleteTask = taskManager.deleteTask(addedTask.getId());
        List<Task> afterDelete = taskManager.getTasks();

        assertFalse(afterDelete.contains(deleteTask), "Delete task should not have been deleted");
        assertEquals(beforeDelete.size() - 1, afterDelete.size(), "Deleting task by id should delete only one task");
    }

    @Test
    void deleteEpic_shouldRemoveEpicFromManager() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());

        List<Epic> beforeDelete = taskManager.getEpics();
        var deleted = taskManager.deleteEpic(epic.getId());
        List<Epic> afterDelete = taskManager.getEpics();

        assertFalse(afterDelete.contains(deleted), "Deleted epic must not be present");
        assertEquals(beforeDelete.size() - 1, afterDelete.size(), "One epic should be removed");
    }

    @Test
    void deleteSubtask_shouldRemoveItFromManager() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var subtask = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);

        List<Subtask> beforeDelete = taskManager.getSubtasks();
        var deleted = taskManager.deleteSubtask(subtask.getId());
        List<Subtask> afterDelete = taskManager.getSubtasks();

        assertFalse(afterDelete.contains(deleted), "Deleted subtask should no longer be in the list");
        assertEquals(beforeDelete.size() - 1, afterDelete.size(), "Exactly one subtask should be removed");
    }

    // ===== Тесты связи Epic и Subtask =====
    @Test
    void addSubtasks_shouldAppearInEpicSubtaskList() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var subtask1 = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);
        var subtask2 = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);

        assertAll("Epic should contain added subtasks",
                () -> assertEquals(2, epicSubtasks.size(), "Epic must have 2 subtasks"),
                () -> assertTrue(epicSubtasks.contains(subtask1), "First subtask must be in list"),
                () -> assertTrue(epicSubtasks.contains(subtask2), "Second subtask must be in list")
        );
    }

    @Test
    void addSubtask_withPredefinedId_shouldThrowException() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var existingSubtask = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);

        var subtaskWithDuplicateId = new Subtask.Builder()
                .fromSubtask(existingSubtask)
                .setId(existingSubtask.getId()) // вручную установленный ID
                .build();

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> taskManager.addSubtask(subtaskWithDuplicateId, epicId),
                "Adding subtask with duplicate ID must throw EntityAlreadyExistsException"
        );
    }

    @Test
    void deleteSubtask_shouldRemoveItFromEpicSubtaskList() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var subtask1 = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);
        var subtask2 = taskManager.addSubtask(TaskUtility.createSubtask(), epicId);

        taskManager.deleteSubtask(subtask1.getId());
        List<Subtask> remainingSubtasks = taskManager.getEpicSubtasks(epicId);

        assertAll("Only second subtask must remain",
                () -> assertEquals(1, remainingSubtasks.size(), "Only one subtask should remain"),
                () -> assertFalse(remainingSubtasks.contains(subtask1), "Deleted subtask must not be present"),
                () -> assertTrue(remainingSubtasks.contains(subtask2), "Remaining subtask must be present")
        );
    }

    @Test
    void deleteAllSubtasks_shouldClearEpicSubtaskList() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        taskManager.addSubtask(TaskUtility.createSubtask(), epicId);
        taskManager.addSubtask(TaskUtility.createSubtask(), epicId);

        taskManager.deleteSubtasks();
        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);

        assertTrue(epicSubtasks.isEmpty(), "All subtasks must be removed from epic");
    }

    @Test
    void getEpicSubtaskList_whenNoSubtasks_shouldReturnEmptyList() {
        var epic = TaskUtility.createEpicBuilder()
                .setName("Epic without subtasks")
                .setDescription("No subtasks yet")
                .build();

        var savedEpic = taskManager.addEpic(epic);
        List<Subtask> subtasks = taskManager.getEpicSubtasks(savedEpic.getId());

        assertNotNull(subtasks, "Returned list of subtasks must not be null");
        assertTrue(subtasks.isEmpty(), "Epic without subtasks must return empty list");
    }

    // ===== Тесты обновления сущностей =====
    @Test
    void updateTaskShouldReturnNewVersionWithUpdatedFields() {
        Task original = new Task.Builder()
                .setName("Original task")
                .setDescription("Original desc")
                .setStatus(Status.NEW)
                .setStartTime(LocalDateTime.now())
                .setDuration(Duration.ofHours(1))
                .build();
        Task added = taskManager.addTask(original);
        int taskId = added.getId();
        Task updated = new Task.Builder()
                .setId(taskId)
                .setName("Updated task")
                .setDescription("Updated desc")
                .setStatus(Status.IN_PROGRESS)
                .setStartTime(LocalDateTime.now().minusHours(100))
                .setDuration(Duration.ofHours(5))
                .build();

        Task result = taskManager.updateTask(updated);
        Task sameTask = taskManager.getTaskWithNotification(taskId);

        assertAll("Updated task fields should match",
                () -> TaskUtility.assertAbstractTaskEquals(sameTask, result),
                () -> assertEquals(taskId, result.getId(), "ID must stay the same"),
                () -> assertEquals("Updated task", result.getName(), "Name should be updated"),
                () -> assertEquals("Updated desc", result.getDescription(), "Description should be updated"),
                () -> assertEquals(Status.IN_PROGRESS, result.getStatus(), "Status should be updated"),
                () -> assertEquals(updated.getStartTime(), result.getStartTime(), "Start time should be updated"),
                () -> assertEquals(updated.getDuration(), result.getDuration(), "Duration should be updated"),
                () -> assertEquals(updated.getEndTime(), result.getEndTime(), "End time should be updated")
        );
    }

    @Test
    void updateEpicShouldReturnNewVersionWithUpdatedFields() {
        var original = TaskUtility.createEpicBuilder()
                .setName("Original Epic")
                .setDescription("Original Description")
                .setStatus(Status.NEW)
                .build();
        var added = taskManager.addEpic(original);
        int id = added.getId();

        var updated = TaskUtility.createEpicBuilder()
                .setId(id)
                .setName("Updated Epic")
                .setDescription("Updated Description")
                .build();

        var result = taskManager.updateEpic(updated);
        var fromManager = taskManager.getEpicWithNotification(id);

        assertAll("Updated epic fields should match",
                () -> TaskUtility.assertAbstractTaskEquals(result, fromManager),
                () -> assertEquals("Updated Epic", result.getName()),
                () -> assertEquals("Updated Description", result.getDescription())
        );
    }

    @Test
    void updateSubtask_shouldReturnUpdatedVersion() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();

        var original = TaskUtility.createSubtaskBuilder()
                .setName("Initial")
                .setStatus(Status.NEW)
                .build();
        var added = taskManager.addSubtask(original, epicId);
        int id = added.getId();

        var updated = TaskUtility.createSubtaskBuilder()
                .setId(id)
                .setEpicId(epicId)
                .setName("Updated")
                .setDescription("Updated description")
                .setStatus(Status.IN_PROGRESS)
                .build();

        var result = taskManager.updateSubtask(updated);
        var fromManager = taskManager.getSubtaskWithNotification(id);

        assertAll("Updated subtask fields should match",
                () -> TaskUtility.assertAbstractTaskEquals(result, fromManager),
                () -> assertEquals("Updated", result.getName()),
                () -> assertEquals("Updated description", result.getDescription()),
                () -> assertEquals(Status.IN_PROGRESS, result.getStatus())
        );
    }

    // ===== Тесты некорректных операций при добавлении =====
    @Test
    void addEpic_withInvalidStatus_shouldThrowException() {
        var invalidEpic = TaskUtility.createEpicBuilder()
                .setStatus(Status.DONE)
                .build();

        assertThrows(
                InvalidParameterException.class,
                () -> taskManager.addEpic(invalidEpic),
                "Epic with manually set status must be rejected"
        );
    }

    @Test
    void addSubtask_toNonexistentEpic_shouldThrowException() {
        Subtask subtask = TaskUtility.createSubtask();
        assertThrows(
                NonexistentEntityException.class,
                () -> taskManager.addSubtask(subtask, NONEXISTENT_ID),
                "Subtask cannot be added to non-existent epic"
        );
    }

    @Test
    void addEpic_withNotEmptySubtasks_shouldThrowException() {
        var invalidEpic = TaskUtility.createEpicBuilder()
                .setSubtaskIds(List.of(1))
                .build();

        assertThrows(
                InvalidParameterException.class,
                () -> taskManager.addEpic(invalidEpic),
                "Epic with manually set subtasks must be rejected"
        );
    }

    @Test
    void updateSubtaskWithDifferentEpicId_shouldThrowException() {
        Epic epic1 = taskManager.addEpic(TaskUtility.createEpic());
        Epic epic2 = taskManager.addEpic(TaskUtility.createEpic());
        Subtask original = taskManager.addSubtask(TaskUtility.createSubtask(), epic1.getId());

        Subtask updated = TaskUtility.createSubtaskBuilder()
                .fromSubtask(original)
                .setEpicId(epic2.getId())
                .build();

        assertThrows(IllegalArgumentException.class, () -> taskManager.updateSubtask(updated));
    }

    // ===== Параметризованные тесты некорректных операций =====
    @ParameterizedTest(name = "Invalid task operation should throw: {index}")
    @MethodSource("invalidOperations")
    void taskOperationsWithInvalidId_shouldThrow(Executable operation) {
        assertThrows(NonexistentEntityException.class, operation);
    }

    Stream<Executable> invalidOperations() {
        return Stream.of(
                () -> taskManager.getTaskWithNotification(NONEXISTENT_ID),
                () -> taskManager.getEpicWithNotification(NONEXISTENT_ID),
                () -> taskManager.getSubtaskWithNotification(NONEXISTENT_ID),

                () -> {
                    var task = TaskUtility.createTaskBuilder().setId(NONEXISTENT_ID).build();
                    taskManager.updateTask(task);
                },
                () -> {
                    var epic = TaskUtility.createEpicBuilder().setId(NONEXISTENT_ID).build();
                    taskManager.updateEpic(epic);
                },
                () -> {
                    var subtask = TaskUtility.createSubtaskBuilder().setId(NONEXISTENT_ID).setEpicId(1).build();
                    taskManager.updateSubtask(subtask);
                },

                () -> taskManager.deleteTask(NONEXISTENT_ID),
                () -> taskManager.deleteEpic(NONEXISTENT_ID),
                () -> taskManager.deleteSubtask(NONEXISTENT_ID)
        );
    }

    // ===== Тесты корректности статуса Epic =====

    @ParameterizedTest(name = "Subtask statuses: {0} => Expected Epic status: {1}")
    @MethodSource("epicStatusScenarios")
    void epicStatusShouldMatchExpected(List<Status> subtaskStatuses, Status expectedEpicStatus) {
        Epic epic = taskManager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();
        for (Status status : subtaskStatuses) {
            Subtask subtask = TaskUtility.createSubtaskBuilder()
                    .setStatus(status)
                    .build();
            taskManager.addSubtask(subtask, epicId);
        }

        Epic updatedEpic = taskManager.getEpicWithNotification(epicId);

        assertEquals(expectedEpicStatus, updatedEpic.getStatus());
    }

    private static Stream<Arguments> epicStatusScenarios() {
        return Stream.of(
                of(
                        List.of(Status.NEW, Status.NEW, Status.NEW, Status.NEW), Status.NEW
                ),
                of(
                        List.of(Status.DONE, Status.DONE, Status.DONE, Status.DONE), Status.DONE
                ),
                of(
                        List.of(Status.NEW, Status.DONE, Status.DONE, Status.DONE), Status.IN_PROGRESS
                ),
                of(
                        List.of(Status.IN_PROGRESS), Status.IN_PROGRESS
                ),
                of(
                        List.of(Status.NEW, Status.IN_PROGRESS, Status.DONE), Status.IN_PROGRESS
                )
        );
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
