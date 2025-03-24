package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.Managers;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerEpicTest {
    TaskManager taskManager;

    String name = "Первый эпик";
    String description = "Мое id будет 3";
    Epic firstEpic = new Epic.Builder()
            .setName(name)
            .setDescription(description)
            .build();

    @BeforeEach
    void setUp() {
        Managers managers = new Managers();
        taskManager = managers.getDefaultTaskManager();

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
    void getAllEpicsShouldBeEmpty() {
        List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics);
        assertEquals(0, epics.size());
    }

    @Test
    void addCorrectEpic() {
        Epic addedEpic = taskManager.addEpic(firstEpic);
        assertNotNull(addedEpic);
        assertEquals(firstEpic.getName(), addedEpic.getName());
        assertEquals(firstEpic.getDescription(), addedEpic.getDescription());
        assertEquals(firstEpic.getStatus(), addedEpic.getStatus());
        assertEquals(3, addedEpic.getId());
        assertEquals(0, addedEpic.getSubtaskIds().size());
    }

    @Test
    void addIncorrectEpicWrongStatus() {
        Epic wrongEpic = new Epic.Builder()
                .fromEpic(firstEpic)
                .setStatus(Status.DONE)
                .build();
        assertThrows(InvalidParameterException.class, () -> taskManager.addEpic(wrongEpic));
    }

    @Test
    void addIncorrectEpicNotEmptySubtaskList() {
        Epic wrongEpic = new Epic.Builder()
                .fromEpicWithNewSubtasks(firstEpic, new ArrayList<>(List.of(1, 3, 5)))
                .build();
        assertThrows(InvalidParameterException.class, () -> taskManager.addEpic(wrongEpic));
    }


    @Test
    void getEpicWithCorrectId() {
        Epic addedEpic = taskManager.addEpic(firstEpic);
        Epic gettedEpic = taskManager.getEpicWithNotification(3);
        assertEquals(addedEpic, gettedEpic);
        assertSame(addedEpic, gettedEpic);
        assertEquals(name, gettedEpic.getName());
        assertEquals(description, gettedEpic.getDescription());
        assertEquals(Status.NEW, gettedEpic.getStatus());
        assertEquals(3, gettedEpic.getId());

        List<AbstractTask> history = taskManager.getHistory();
        assertEquals(1, history.size());
        Epic historyEpic = (Epic) history.getFirst();
        assertSame(addedEpic, historyEpic);
    }

    @Test
    void getEpicWithIncorrectId() {
        assertThrows(NonexistentEntityException.class, () -> taskManager.getEpicWithNotification(-1));
    }

    @Test
    void updateEpicShouldReturnNewEpic() {
        Epic savedEpic = taskManager.addEpic(firstEpic);
        String newName = "Новое название";
        String newDesc = "Новое описание";

        Epic updatedEpic = new Epic.Builder()
                .fromEpic(savedEpic)
                .setName(newName)
                .setDescription(newDesc)
                .build();
        Epic updatedSavedEpic = taskManager.updateEpic(updatedEpic);

        assertEquals(savedEpic.getName(), name);
        assertEquals(savedEpic.getDescription(), description);

        assertEquals(newName, updatedSavedEpic.getName());
        assertEquals(newDesc, updatedSavedEpic.getDescription());
        assertEquals(Status.NEW, updatedSavedEpic.getStatus());
        assertEquals(3, updatedSavedEpic.getId());
    }

    @Test
    void updateEpicWithIncorrectId() {
        assertThrows(NonexistentEntityException.class, () -> taskManager.updateEpic(firstEpic));
    }

    @Test
    void deleteEpicDoesntAffectTasks() {
        List<Task> tasks = taskManager.getTasks();
        Epic savedEpic = taskManager.addEpic(firstEpic);
        Epic deletedEpic = taskManager.deleteEpic(3);
        assertSame(savedEpic, deletedEpic);

        assertThrows(NonexistentEntityException.class, () -> taskManager.deleteEpic(3));
        List<Epic> epics = taskManager.getEpics();
        assertEquals(0, epics.size());

        assertEquals(0, taskManager.getHistory().size());

        List<Task> tasksAfter = taskManager.getTasks();
        assertEquals(tasks, tasksAfter);
    }

    @Test
    void getEmptyEpicSubtaskList() {
        Epic savedEpic = taskManager.addEpic(firstEpic);
        List<Subtask> subtasks = taskManager.getEpicSubtasks(savedEpic.getId());
        assertNotNull(subtasks);
        assertEquals(0, subtasks.size());
    }

    @Test
    void getNonexistentEpicTaskList() {
        taskManager.addEpic(firstEpic);
        taskManager.deleteEpic(3);
        assertThrows(NonexistentEntityException.class, () -> taskManager.getEpicWithNotification(3));
    }
}
