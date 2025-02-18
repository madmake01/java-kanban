package model;

import enums.Status;

import java.util.Optional;

public class Subtask extends AbstractTask {
    private Epic epic;

    public Subtask(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Subtask(String name, String description, Status status) {
        super(-1, name, description, status);
    }

    public Subtask(int id, Subtask subtask) {
        super(id, subtask.name, subtask.description, subtask.status);
        this.epic = subtask.epic;
    }

    public Optional<Epic> getEpic() {
        return Optional.ofNullable(epic);
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epic=" + epic.getName() +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
