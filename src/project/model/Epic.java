package project.model;

import project.enums.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private static final Status DEFAULT_STATUS = Status.NEW;
    private final List<Integer> subTaskIds;

    //создание нового экземпляра у пользователя
    public Epic(String name, String description) {
        super(DEFAULT_ID, name, description, DEFAULT_STATUS);
        subTaskIds = new ArrayList<>();
    }

    //обновление у пользователя
    public Epic(Epic epic, String name, String description) {
        super(epic.getId(), name, description, epic.getStatus());
        this.subTaskIds = epic.getSubTaskIds();
    }

    //для метода addNew менеджера
    public Epic(Epic epic, int id) {
        super(id, epic.getName(), epic.getDescription(), epic.getStatus());
        this.subTaskIds = epic.getSubTaskIds();
    }

    //для метода update менеджера
    public Epic(Epic epic, Status status) {
        super(epic.getId(), epic.getName(), epic.getDescription(), status);
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
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subTaskIds +
                '}';
    }
}
