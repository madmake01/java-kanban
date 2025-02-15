package model;

import util.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Subtask> subTaskList;

    public Epic(String name, String description, Status status) {
        super(-1, name, description, status);
        subTaskList = new ArrayList<>();
    }

    public Epic(int id, Epic epic, Status status) {
        super(id, epic.name, epic.description, status);
        this.subTaskList = epic.getSubTaskList();
    }

    public List<Subtask> getSubTaskList() {
        return List.copyOf(subTaskList);
    }

    @Override
    public Status getStatus() {
        this.status = checkStatus();
        return status;
    }

    public Status checkStatus() {
        if (subTaskList.isEmpty()) {
            return Status.NEW;
        }

        boolean hasNew = false;

        for (Subtask subtask : subTaskList) {
            Status subtaskStatus = subtask.getStatus();

            if (subtaskStatus == Status.IN_PROGRESS) {
                return Status.IN_PROGRESS;
            }

            if (subtaskStatus == Status.NEW) {
                hasNew = true;
            }
        }
        return hasNew ? Status.NEW : Status.DONE;
    }
}
