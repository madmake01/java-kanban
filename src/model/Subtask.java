package model;

import enums.Status;

public class Subtask extends AbstractTask {
    private final int epicId;

    public Subtask(int id, String name, String description, Status status) {
        super(id, name, description, status);
        this.epicId = DEFAULT_ID;
    }

    public Subtask(String name, String description, Status status) {
        this(DEFAULT_ID, name, description, status);
    }

    public Subtask(Subtask subtask, int id, int epicId) {
        super(id, subtask.name, subtask.description, subtask.status);
        this.epicId = epicId;
    }

    public Subtask(Subtask subtask, String name, String description, Status status) {
        super(subtask.id, name, description, status);
        this.epicId = subtask.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epic=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
