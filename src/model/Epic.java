package model;

import enums.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Integer> subTaskIds;
    private static final Status DEFAULT_STATUS = Status.NEW;
    //создание нового экземпляра у пользователя
    public Epic(String name, String description) {
        super(DEFAULT_ID, name, description, DEFAULT_STATUS);
        subTaskIds = new ArrayList<>();
    }

    //обновление у пользователя
    public Epic(Epic epic, String name, String description) {
        super(epic.id, name, description, epic.status);
        this.subTaskIds = epic.getSubTaskIds();
    }

    //для метода addNew менеджера
    public Epic(Epic epic, int id) {
        super(id, epic.name, epic.description, epic.status);
        this.subTaskIds = epic.getSubTaskIds();
    }

    //для метода update менеджера
    public Epic(Epic epic, Status status) {
        super(epic.id, epic.name, epic.description, status);
        this.subTaskIds = epic.getSubTaskIds();
    }

    public List<Integer> getSubTaskIds() {
        return new ArrayList<>(subTaskIds);
    }

    public void addSubtask(int subtaskId) {
        subTaskIds.add(subtaskId);
    }

    public void removeSubtask(Integer subtaskId) {
        subTaskIds.remove(subtaskId);
    }

    public void removeAllSubtasks() {
        subTaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subTaskIds +
                '}';
    }
}
