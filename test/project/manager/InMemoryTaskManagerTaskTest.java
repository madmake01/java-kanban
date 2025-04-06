package project.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.exception.EntityAlreadyExistsException;
import project.exception.NonexistentEntityException;
import project.model.AbstractTask;
import project.model.Task;
import project.util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerTaskTest {


    TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefaultTaskManager();

        Task firstTask = new Task.Builder()
                .setName("Первая задача")
                .setDescription("Описание первой задачи")
                .build();

        Task secondTask = new Task.Builder()
                .setName("Вторая задача с недефолтным статусом")
                .setDescription("Описание второй задачи")
                .setStatus(Status.DONE)
                .build();

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
    }

    @Test
    void getAllTasks() {
        List<Task> allTasks = taskManager.getTasks();
        assertNotNull(allTasks);
        assertEquals(2, allTasks.size());
        Task firstTask = allTasks.getFirst();
        assertEquals("Первая задача", firstTask.getName(), "Wrong task name");
        assertEquals(Status.NEW, firstTask.getStatus(), "Wrong task status");
        assertEquals("Описание первой задачи", firstTask.getDescription(), "Wrong task description");
        assertEquals(1, firstTask.getId(), "Wrong task ID");
    }

    @Test
    void addTask3() {
        String name = "Новая задача";
        String description = "Описание новой задачи";
        Status status = Status.DONE;

        Task newTask = new Task.Builder()
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .build();
        Task addedTask = taskManager.addTask(newTask);
        assertNotNull(addedTask);
        assertEquals(name, addedTask.getName(), "Name of task should be the same");
        assertEquals(description, addedTask.getDescription(), "Description of task should be the same");
        assertEquals(status, addedTask.getStatus(), "Status of task should be the same");
        assertEquals(3, addedTask.getId(), "Id of task should be the 3");
    }

    @Test
    void addTaskWithId1ThatAlreadyExists() {
        String name = "Новая задача";
        String description = "Описание новой задачи";
        Status status = Status.DONE;

        Task newTask = new Task.Builder()
                .setId(1)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .build();
        assertThrows(EntityAlreadyExistsException.class, () -> taskManager.addTask(newTask));
    }

    @Test
    void deleteTasks() {
        taskManager.deleteTasks();
        List<Task> allTasks = taskManager.getTasks();
        assertNotNull(allTasks, "Tasks should not be null");
        assertEquals(0, allTasks.size(), "Tasks should be empty");
    }

    @Test
    void getTaskById1IsPresent() {
        Task task = taskManager.getTaskWithNotification(1);
        Task expected =
                new Task.Builder()
                        .setId(1)
                        .setName("Первая задача")
                        .setDescription("Описание первой задачи")
                        .build();
        assertEquals(expected.getId(), task.getId(), "Id of task should be the 1");
        assertEquals(expected.getName(), task.getName(), "Wrong task name");
        assertEquals(expected.getStatus(), task.getStatus(), "Wrong task status");
        assertEquals(expected.getDescription(), task.getDescription(), "Wrong task description");

        List<AbstractTask> historyTasks = taskManager.getHistory();
        assertEquals(1, historyTasks.size(), "History tasks should have 1 task");
        AbstractTask historyTask = historyTasks.getFirst();
        assertEquals(expected.getId(), historyTask.getId(), "Id of history task should be the 1");
        assertEquals(expected.getName(), historyTask.getName(), "Wrong task name");
        assertEquals(expected.getStatus(), historyTask.getStatus(), "Wrong task status");
        assertEquals(expected.getDescription(), historyTask.getDescription(), "Wrong task description");
    }

    @Test
    void getTaskByWrongIdShouldThrowException() {
        assertThrows(NonexistentEntityException.class, () -> taskManager.getTaskWithNotification(100));
    }

    @Test
    void deleteTaskById() {
        Task task = taskManager.deleteTask(1);
        Task expected =
                new Task.Builder()
                        .setId(1)
                        .setName("Первая задача")
                        .setDescription("Описание первой задачи")
                        .build();
        assertNotNull(task, "Task should not be null");
        assertEquals(expected.getId(), task.getId(), "Id of task should be the 1");
        assertEquals(expected.getName(), task.getName(), "Wrong task name");
        assertEquals(expected.getStatus(), task.getStatus(), "Wrong task status");
        assertEquals(expected.getDescription(), task.getDescription(), "Wrong task description");
        assertThrows(NonexistentEntityException.class, () -> taskManager.getTaskWithNotification(1));
        List<Task> allTasks = taskManager.getTasks();
        assertEquals(1, allTasks.size(), "Tasks should contain only id = 2 task");
        Task remainingTask = allTasks.getFirst();

        Task expectedTask = new Task.Builder()
                .setId(2)
                .setName("Вторая задача с недефолтным статусом")
                .setDescription("Описание второй задачи")
                .setStatus(Status.DONE)
                .build();

        assertEquals(expectedTask.getId(), remainingTask.getId(), "Id of task should be the 2");
        assertEquals(expectedTask.getName(), remainingTask.getName(), "Wrong task name");
        assertEquals(expectedTask.getStatus(), remainingTask.getStatus(), "Wrong task status");
        assertEquals(expectedTask.getDescription(), remainingTask.getDescription(), "Wrong task description");
        assertEquals(remainingTask, taskManager.getTaskWithNotification(2));
    }

    @Test
    void updateTaskShouldReturnNewVersionWithUpdatedFields() {

        Task task = taskManager.getTaskWithNotification(1);


        String newName = "Updated Task Name";
        String newDescription = "Updated Task Description";
        Status newStatus = Status.IN_PROGRESS;


        Task updatedTaskInput = new Task.Builder()
                .fromTask(task)
                .setName(newName)
                .setDescription(newDescription)
                .setStatus(newStatus)
                .build();


        Task updatedTask = taskManager.updateTask(updatedTaskInput);


        assertNotSame(task, updatedTask, "Task reference should be different after update");

        assertEquals(1, updatedTask.getId(), "Id of updated task should be the 1");
        assertEquals(newName, updatedTask.getName(), "Task name should be updated");
        assertEquals(newDescription, updatedTask.getDescription(), "Task description should be updated");
        assertEquals(newStatus, updatedTask.getStatus(), "Task status should be updated");
    }

    @Test
    void updateTaskShouldThrowExceptionForNonExistentTaskId() {

        Task newTask = new Task.Builder()
                .setId(20)
                .setName("New Task")
                .setDescription("New Task Description")
                .setStatus(Status.NEW)
                .build();

        assertThrows(NonexistentEntityException.class, () -> taskManager.updateTask(newTask));
    }


}