package model;

import enums.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Integer> subTaskIdList;

    public Epic(Epic epic, String description, String name) {
        super(epic.id, name, description, epic.status);
        this.subTaskIdList = epic.getSubTaskIdList();
    }

    public Epic(String name, String description) {
        super(DEFAULT_ID, name, description, Status.NEW);
        subTaskIdList = new ArrayList<>();
    }

    public Epic(Epic epic, int id) {
        super(id, epic.name, epic.description, Status.NEW);
        subTaskIdList = new ArrayList<>();
    }

    public Epic (Epic epic, Status status) {
        super(epic.id, epic.name, epic.description, status);
        this.subTaskIdList = epic.getSubTaskIdList();
    }
    public List<Integer> getSubTaskIdList() {
        return new ArrayList<>(subTaskIdList);
    }

    public void addSubtask(int subtaskId) {
        subTaskIdList.add(subtaskId);
    }

    public void removeSubtask(Integer subtaskId) {
        subTaskIdList.remove(subtaskId);
    }

    public void removeAllSubtasks() {
        subTaskIdList.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subTaskList=" + subTaskIdList +
                '}';
    }
}
