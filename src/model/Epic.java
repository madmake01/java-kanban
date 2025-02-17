package model;

import exception.EntityAlreadyExistsException;
import exception.NonexistentEntityException;
import util.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static exception.TaskExceptionMessage.SUBTASK_ALREADY_ASSOCIATED;
import static exception.TaskExceptionMessage.SUBTASK_ALREADY_EXISTS;
import static exception.TaskExceptionMessage.SUBTASK_DOES_NOT_EXIST;

public class Epic extends AbstractTask {
    private final List<Subtask> subTaskList;

    public Epic(String name, String description) {
        super(-1, name, description, Status.NEW);
        subTaskList = new ArrayList<>();
    }

    public Epic(int id, Epic epic, Status status) {
        super(id, epic.name, epic.description, status);
        this.subTaskList = epic.getSubTaskList();
    }

    public List<Subtask> getSubTaskList() {
        return new ArrayList<>(subTaskList);
    }

    public void addSubtask(Subtask subtask) {
        if (subTaskList.contains(subtask)) {
            throw new EntityAlreadyExistsException(SUBTASK_ALREADY_EXISTS);
        }

        Optional<Epic> optionalEpic = subtask.getEpic();
        if (optionalEpic.isPresent()) {
            throw new EntityAlreadyExistsException(SUBTASK_ALREADY_ASSOCIATED);
        }

        subtask.setEpic(this);
        subTaskList.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        if (!subTaskList.contains(subtask)) {
            throw new NonexistentEntityException(SUBTASK_DOES_NOT_EXIST + subtask);
        }
        subTaskList.remove(subtask);
        subtask.setEpic(null);
    }
}
