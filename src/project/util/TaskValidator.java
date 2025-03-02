package project.util;

import project.enums.Status;
import project.exception.EntityAlreadyExistsException;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;

import static project.exception.TaskExceptionMessage.EPICS_SUBTASKS_SHOULD_BE_EQUAL;
import static project.exception.TaskExceptionMessage.ID_SHOULD_BE_DEFAULT;
import static project.exception.TaskExceptionMessage.NEW_EPIC_SHOULD_BE_EMPTY;
import static project.exception.TaskExceptionMessage.NEW_EPIC_SHOULD_HAVE_DEFAULT_STATUS;
import static project.exception.TaskExceptionMessage.SUBTASK_SHOULD_HAVE_EQUAL_EPICS;
import static project.util.AbstractTaskBuilder.DEFAULT_ID;
import static project.util.AbstractTaskBuilder.DEFAULT_STATUS;

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
        if(epic.getStatus() != DEFAULT_STATUS) {
            throw new InvalidParameterException(NEW_EPIC_SHOULD_HAVE_DEFAULT_STATUS);
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
