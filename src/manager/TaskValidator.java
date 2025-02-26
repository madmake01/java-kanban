package manager;

import exception.EntityAlreadyExistsException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.HashSet;
import java.util.List;

import static exception.TaskExceptionMessage.EPICS_SUBTASKS_SHOULD_BE_EQUAL;
import static exception.TaskExceptionMessage.ID_SHOULD_BE_DEFAULT;
import static exception.TaskExceptionMessage.NEW_EPIC_SHOULD_BE_EMPTY;
import static exception.TaskExceptionMessage.SUBTASK_SHOULD_HAVE_EQUAL_EPICS;
import static model.AbstractTask.DEFAULT_ID;

public class TaskValidator {

    public void validateNewTask(Task task) {
        validateId(task.getId());
    }

    public void validateNewSubTask(Subtask subtask) {
        validateId(subtask.getId());
        validateId(subtask.getEpicId());
    }

    public void validateNewEpic(Epic epic) {
        validateId(epic.getId());

        if (!epic.getSubTaskIds().isEmpty()) {
            throw new EntityAlreadyExistsException(NEW_EPIC_SHOULD_BE_EMPTY);
        }
    }

    public void ensureEpicSubtasksAreEqual(Epic oldEpic, Epic newEpic) {
        List<Integer> oldSubtaskList = oldEpic.getSubTaskIds();
        List<Integer> newSubtaskList = newEpic.getSubTaskIds();

        boolean isEqualSize = oldSubtaskList.size() == newSubtaskList.size();
        if (!isEqualSize) {
            throw new IllegalArgumentException(EPICS_SUBTASKS_SHOULD_BE_EQUAL);
        }

        boolean isEqualIds = new HashSet<>(oldSubtaskList).containsAll(newSubtaskList);
        if (!isEqualIds) {
            throw new IllegalArgumentException(EPICS_SUBTASKS_SHOULD_BE_EQUAL);
        }
    }

    public void ensureSubtasksEpicsAreEqual(Subtask oldSubtask, Subtask newSubtask) {
        if (oldSubtask.getEpicId() != newSubtask.getEpicId()) {
            throw new IllegalArgumentException(SUBTASK_SHOULD_HAVE_EQUAL_EPICS);
        }
    }

    private void validateId(int id) {
        if (id != DEFAULT_ID) {
            throw new EntityAlreadyExistsException(ID_SHOULD_BE_DEFAULT + id);
        }
    }
}
