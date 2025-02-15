package model;

import util.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Subtask> subTaskList;

    public Epic(int id, Epic epic, Status status) {
        super(id, epic.name, epic.description, status);
        this.subTaskList = epic.getSubTaskList();
    }

    public List<Subtask> getSubTaskList() {
        return List.copyOf(subTaskList);
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
