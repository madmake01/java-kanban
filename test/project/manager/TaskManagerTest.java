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
    void addSubtask_withMismatchedEpicId_shouldThrowException() {
        var epic = taskManager.addEpic(TaskUtility.createEpic());
        int actualEpicId = epic.getId();

        var subtask = new Subtask.Builder()
                .fromSubtask(TaskUtility.createSubtask())
                .setEpicId(NONEXISTENT_ID)
                .build();

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> taskManager.addSubtask(subtask, actualEpicId),
                "Subtask epicId must match the provided one; otherwise, exception expected"
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
}
