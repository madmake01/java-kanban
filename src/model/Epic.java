package model;

import util.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Subtask> subTaskList;

    public Epic(String name, String description) {
        super(name, description);
        subTaskList = new ArrayList<>();
    }

    public void addSubtask(String name, String description, Status status) {
        Subtask subtask = new Subtask(name, description, this, status);
        subTaskList.add(subtask);
        checkStatus();
    }

    public void addSubtask(String name, String description) {
        Subtask subtask = new Subtask(name, description, this);
        subTaskList.add(subtask);
    }

    public List<Subtask> getSubTaskList() {
        return List.copyOf(subTaskList);
    }

    public void checkStatus() {
        if (subTaskList.isEmpty()) {
            this.status = Status.NEW;
            return;
        }

        boolean hasNew = false;

        for (Subtask subtask : subTaskList) {
            Status subtaskStatus = subtask.getStatus();

            if (subtaskStatus == Status.IN_PROGRESS) {
                this.status = Status.IN_PROGRESS;
                return;
            }

            if (subtaskStatus == Status.NEW) {
                hasNew = true;
            }
        }

        this.status = hasNew ? Status.NEW : Status.DONE;
    }

}
