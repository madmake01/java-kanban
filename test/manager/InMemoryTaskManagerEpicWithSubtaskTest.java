package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.exception.EntityAlreadyExistsException;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;
import project.model.Epic;
import project.model.Subtask;
import project.util.Managers;

import java.security.InvalidParameterException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerEpicWithSubtaskTest {
    TaskManager taskManager;

    String nameEpicFirst = "Первый эпик";
    String descriptionFirstEpic = "Мое id будет 1";
    Epic firstEpic;
    String nameSubtaskOne = "Первая сабтаска первого эпика";
    String descriptionSubtaskOne = "Описание первой сабтаски первого эпика";
    Subtask oneFirstSubtask = new Subtask.Builder()
            .setName(nameSubtaskOne)
            .setDescription(descriptionSubtaskOne)
            .setStatus(Status.DONE)
            .build();

    @BeforeEach
    void setUp() {
        Managers managers = new Managers();
        taskManager = managers.getDefaultTaskManager();
        Epic epicToAdd = new Epic.Builder()
                .setName(nameEpicFirst)
                .setDescription(descriptionFirstEpic)
                .build();
        taskManager.addEpic(epicToAdd);
        firstEpic = taskManager.getEpicWithNotification(1);
    }


    @Test
    void addSubtaskWithCorrectIdAndEpicId() {

        Subtask addedSubtask = taskManager.addSubtask(oneFirstSubtask, firstEpic.getId());
        assertNotNull(addedSubtask);

        assertEquals(2, addedSubtask.getId(), "Id of subtask should be the 2");
        assertEquals(nameSubtaskOne, addedSubtask.getName(), "Wrong subtask name");
        assertEquals(descriptionSubtaskOne, addedSubtask.getDescription(), "Wrong subtask description");
        assertEquals(Status.DONE, addedSubtask.getStatus(), "Wrong subtask status");

        assertEquals(-1, oneFirstSubtask.getId(), "Id of original subtask should be default -1 ");

        Epic updatedEpic = taskManager.getEpicWithNotification(1);
        assertEquals(Status.DONE, updatedEpic.getStatus(), "Wrong subtask status");
        assertEquals(1, updatedEpic.getSubtaskIds().size(), "Wrong subtask id list");

    }

    @Test
    void addSubtaskWithNotDefaultIdAndAssignedEpicId() {
        Subtask subtaskWithWrongId = new Subtask.Builder()
                .fromSubtask(oneFirstSubtask)
                .setId(2)
                .build();
        int epicId = firstEpic.getId();
        assertThrows(EntityAlreadyExistsException.class, () -> taskManager.addSubtask(subtaskWithWrongId,
                epicId));

        Subtask subtaskWithNonDefaultEpicId = new Subtask.Builder()
                .fromSubtask(oneFirstSubtask)
                .setEpicId(1)
                .build();
        assertThrows(EntityAlreadyExistsException.class, () -> taskManager.addSubtask(subtaskWithNonDefaultEpicId,
                epicId));
    }

    @Test
    void addSubtaskCorrectButEpicIdIsIncorrect() {
        assertThrows(NonexistentEntityException.class, () -> taskManager.addSubtask(oneFirstSubtask, 3));
    }

    @Test
    void updateSubtaskCorrect() {
        Subtask addedSubtask = taskManager.addSubtask(oneFirstSubtask, firstEpic.getId());

        String updatedName = "new name";
        String updatedDescription = "new description";
        Status updatedStatus = Status.IN_PROGRESS;

        Subtask updatedSubtask = new Subtask.Builder()
                .fromSubtask(addedSubtask)
                .setName(updatedName)
                .setDescription(updatedDescription)
                .setStatus(updatedStatus)
                .build();
        Subtask savedUpdatedSubtask = taskManager.updateSubtask(updatedSubtask);

        assertEquals(updatedName, savedUpdatedSubtask.getName(), "Wrong subtask name");
        assertEquals(updatedDescription, savedUpdatedSubtask.getDescription(), "Wrong subtask description");
        assertEquals(updatedStatus, savedUpdatedSubtask.getStatus(), "Wrong subtask status");
        assertEquals(addedSubtask.getId(), savedUpdatedSubtask.getId(), "Id of subtask doesnt match");
        assertEquals(addedSubtask.getEpicId(), savedUpdatedSubtask.getEpicId(), "Wrong subtask name");

        assertEquals(nameSubtaskOne, addedSubtask.getName(), "Wrong subtask name");
        assertEquals(descriptionSubtaskOne, addedSubtask.getDescription(), "Wrong subtask description");
    }

    @Test
    void updateSubtaskIncorrect() {
        Subtask addedSubtask = taskManager.addSubtask(oneFirstSubtask, firstEpic.getId());
        Subtask subtaskWrongId = new Subtask.Builder()
                .fromSubtask(addedSubtask)
                .setId(3)
                .build();
        assertThrows(NonexistentEntityException.class, () -> taskManager.updateSubtask(subtaskWrongId));

        Subtask subtaskWrongEpicId = new Subtask.Builder()
                .fromSubtask(addedSubtask)
                .setEpicId(2)
                .build();
        assertThrows(InvalidParameterException.class, () -> taskManager.updateSubtask(subtaskWrongEpicId));
    }

    @Test
    void deleteSubtaskCorrect() {
        Subtask addedSubtask = taskManager.addSubtask(oneFirstSubtask, firstEpic.getId());
        List<Subtask> allSubtasks = taskManager.getSubtasks();
        Subtask getSubtask = taskManager.getSubtaskWithNotification(2);
        Subtask deletedSubtask = taskManager.deleteSubtask(2);
        assertEquals(addedSubtask.getId(), getSubtask.getId(), "Wrong subtask id");
        assertEquals(addedSubtask.getName(), getSubtask.getName(), "Wrong subtask name");
        assertEquals(addedSubtask.getDescription(), getSubtask.getDescription(), "Wrong subtask description");
        assertEquals(addedSubtask.getStatus(), getSubtask.getStatus(), "Wrong subtask status");
        assertSame(getSubtask, deletedSubtask, "Wrong subtask");

        List<Subtask> emptySubtasks = taskManager.getSubtasks();
        assertEquals(1, allSubtasks.size(), "Wrong subtasks size");
        assertEquals(0, emptySubtasks.size(), "Wrong subtasks size");

        Epic afterDeletedSubtask = taskManager.getEpicWithNotification(1);

        assertEquals(Status.NEW, afterDeletedSubtask.getStatus(), "Wrong subtask status");
        assertEquals(0, afterDeletedSubtask.getSubtaskIds().size(), "Wrong subtask id list");
    }
}
