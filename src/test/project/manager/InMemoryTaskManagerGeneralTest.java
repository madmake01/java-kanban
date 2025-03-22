package test.project.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.manager.TaskManager;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryTaskManagerGeneralTest {

    TaskManager taskManager;

    String nameEpicFirst = "Первый эпик";
    String descriptionFirstEpic = "Я первый эпик";
    Epic firstEpic;

    String nameEpicSecond = "Второй эпик";
    String descriptionSecondEpic = "Я второй эпик";
    Epic secondEpic;

    String nameTaskOne = "Первая задача";
    String descriptionTaskOne = "Описание первой задачи";
    Status statusTaskOne = Status.DONE;
    Task firstTask;

    String nameTaskTwo = "Вторая задача";
    String descriptionTaskTwo = "Описание второй задачи";
    Task secondTask;

    String nameSubtaskOne = "Первая сабтаска первого эпика";
    String descriptionSubtaskOne = "Описание первой сабтаски первого эпика";
    Subtask oneFirstSubtask;

    String nameSubtaskTwo = "Вторая сабтаска первого эпика";
    String descriptionSubtaskTwo = "Описание второй сабтаски первого эпика";
    Subtask twoFirstSubtask;

    String nameSubtaskThree = "Единственная сабтаска второго эпика";
    String descriptionSubtaskThree = "Описание единственной сабтаски второго эпика";
    Subtask oneSecondSubtask;


    @BeforeEach
    void setUp() {
        Managers managers = new Managers();
        taskManager = managers.getDefaultTaskManager();

        firstTask = new Task.Builder()
                .setName(nameTaskOne)
                .setDescription(descriptionTaskOne)
                .setStatus(statusTaskOne)
                .build();
        secondTask = new Task.Builder()
                .setName(nameTaskTwo)
                .setDescription(descriptionTaskTwo)
                .build();

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);

        firstEpic = new Epic.Builder()
                .setName(nameEpicFirst)
                .setDescription(descriptionFirstEpic)
                .build();
        secondEpic = new Epic.Builder()
                .setName(nameEpicSecond)
                .setDescription(descriptionSecondEpic)
                .build();

        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);

        oneFirstSubtask = new Subtask.Builder()
                .setName(nameSubtaskOne)
                .setDescription(descriptionSubtaskOne)
                .setStatus(Status.NEW)
                .build();
        twoFirstSubtask = new Subtask.Builder()
                .setName(nameSubtaskTwo)
                .setDescription(descriptionSubtaskTwo)
                .setStatus(Status.IN_PROGRESS)
                .build();
        oneSecondSubtask = new Subtask.Builder()
                .setName(nameSubtaskThree)
                .setDescription(descriptionSubtaskThree)
                .setStatus(Status.DONE)
                .build();

        taskManager.addSubtask(oneFirstSubtask, 3);
        taskManager.addSubtask(twoFirstSubtask, 3);
        taskManager.addSubtask(oneSecondSubtask, 4);
    }

    @Test
    void sameIdAbstractTasksShouldBeEqual() {
        Epic epic = new Epic.Builder()
                .setId(20)
                .setName(nameEpicFirst)
                .setDescription(nameEpicSecond)
                .build();
        Epic otherEpic = new Epic.Builder()
                .setId(20)
                .setName(nameEpicFirst)
                .setDescription(nameEpicSecond)
                .setStatus(Status.IN_PROGRESS)
                .build();
        assertEquals(epic, otherEpic);

        Task task = new Task.Builder()
                .setId(50)
                .setName("a")
                .setDescription("a")
                .setStatus(Status.IN_PROGRESS)
                .build();
        Task otherTask = new Task.Builder()
                .setId(50)
                .setName("b")
                .setDescription("b")
                .setStatus(Status.DONE)
                .build();
        assertEquals(task, otherTask);

        Subtask subtask = new Subtask.Builder()
                .setId(1)
                .setName("b")
                .setDescription("b")
                .setStatus(Status.DONE)
                .setEpicId(2) //вообще я бы такое тоже проверяла в equals :(
                .build();
        Subtask otherSubtask = new Subtask.Builder()
                .setId(1)
                .setName("dfg")
                .setDescription("dfg")
                .setStatus(Status.NEW)
                .setEpicId(20)
                .build();
        assertEquals(subtask, otherSubtask);
    }

    @Test
    void testHistoryFullCycle() {
        for (int i = 0; i < 10; i++) {
            Task taskToAdd = new Task.Builder()
                    .setName(String.valueOf(i))
                    .setDescription("Desk: " + i)
                    .setStatus(getRandomStatus())
                    .build();
            taskManager.addTask(taskToAdd);
        }

        List<Task> allTasks = taskManager.getTasks();
        for (Task task : allTasks) {
            taskManager.getTaskWithNotification(task.getId());
        }
        AbstractTask ninthHistoryTask = taskManager.getEpicWithNotification(3);
        Subtask lastHistoryTask = taskManager.getSubtaskWithNotification(6);

        List<AbstractTask> history = taskManager.getHistory();
        assertEquals(lastHistoryTask, history.getLast());
        assertEquals(ninthHistoryTask, history.get(history.size() - 2));
        List<Task> tasksRemainInHistory = allTasks.subList(history.size() - 2, allTasks.size());
        assertTrue(history.containsAll(tasksRemainInHistory));

        Subtask subtaskToUpdate = new Subtask.Builder()
                .fromSubtask(lastHistoryTask)
                .setName("new name!!")
                .setDescription("o i i a ")
                .build();
        Subtask updatedSubtask = taskManager.updateSubtask(subtaskToUpdate);

        assertNotEquals(lastHistoryTask.getName(), updatedSubtask.getName(), "Wrong subtask name");
        assertNotEquals(lastHistoryTask.getDescription(), updatedSubtask.getDescription(), "Wrong subtask description");

    }

    Status getRandomStatus() {
        Status[] values = Status.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    @Test
    void addAndCheckEpicStatus() {
        List<Task> tasks = taskManager.getTasks();
        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Epic> epics = taskManager.getEpics();
        assertEquals(2, tasks.size());
        assertEquals(3, subtasks.size());
        assertEquals(2, epics.size());

        assertEquals(Status.IN_PROGRESS, epics.getFirst().getStatus());
        assertEquals(Status.DONE, epics.getLast().getStatus());
    }

    @Test
    void deleteAllSubtasksAndCheckEpicStatusAndSubtasks() {
        taskManager.deleteSubtasks();
        assertEquals(0, taskManager.getSubtasks().size());

        List<Epic> epics = taskManager.getEpics();
        for (Epic epic : epics) {
            assertEquals(Status.NEW, epic.getStatus());
            assertEquals(0, epic.getSubtaskIds().size());
            assertEquals(0, taskManager.getEpicSubtasks(epic.getId()).size());
        }
    }

    @Test
    void testUserCaseSprint6 () {
        extracted();
    }

    private void extracted() {
        List<Epic> epics = taskManager.getEpics();
        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Task> tasks = taskManager.getTasks();

        epics.forEach(epic -> taskManager.getEpicWithNotification(epic.getId()));
        testHistoryTasksAreUnique();
        subtasks.forEach(subtask -> taskManager.getSubtaskWithNotification(subtask.getId()));
        testHistoryTasksAreUnique();
        tasks.forEach(task -> taskManager.getTaskWithNotification(task.getId()));
        testHistoryTasksAreUnique();

        epics.reversed().forEach(epic -> taskManager.getEpicWithNotification(epic.getId()));
        testHistoryTasksAreUnique();
        subtasks.reversed().forEach(epic -> taskManager.getSubtaskWithNotification(epic.getId()));
        testHistoryTasksAreUnique();
        tasks.reversed().forEach(epic -> taskManager.getTaskWithNotification(epic.getId()));
        testHistoryTasksAreUnique();

        List<AbstractTask> history = taskManager.getHistory();
        int expectedSize = epics.size() + subtasks.size() + tasks.size();

        List<AbstractTask> expectedTasks = new ArrayList<>(epics.reversed());
        expectedTasks.addAll(subtasks.reversed());
        expectedTasks.addAll(tasks.reversed());

        assertEquals(expectedTasks, history);
        assertEquals(expectedSize, history.size());

        testDeletingEpicAlsoDeleteSubtasksFromHistory(epics);

        deletedTaskDeletesFromHistory(tasks);

    }

    private void deletedTaskDeletesFromHistory(List<Task> tasks) {
        Task task1 = tasks.get(1);
        taskManager.deleteTask(task1.getId());
        List<AbstractTask> history = taskManager.getHistory();

        assertFalse(history.contains(task1));
    }

    private void testDeletingEpicAlsoDeleteSubtasksFromHistory(List<Epic> epics) {
        Epic epic = epics.getFirst();
        int epicId = epic.getId();
        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);

        taskManager.deleteEpic(epicId);

        List<AbstractTask> history = taskManager.getHistory();

        assertFalse(history.contains(epic));
        for (Subtask epicSubtask : epicSubtasks) {
            assertFalse(history.contains(epicSubtask));
        }
    }

    private void testHistoryTasksAreUnique() {
        List<AbstractTask> history = taskManager.getHistory();
        List<AbstractTask> uniqueTasks = history.stream().distinct().toList();
        assertEquals(uniqueTasks.size(), history.size());
    }
}

