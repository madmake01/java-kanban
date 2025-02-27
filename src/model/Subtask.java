package model;

import enums.Status;

public class Subtask extends AbstractTask {
    private final int epicId;

    //создание нового экземпляра у пользователя
    public Subtask(String name, String description, Status status) {
        super(DEFAULT_ID, name, description, status);
        this.epicId = DEFAULT_ID;
    }

    //обновление у пользователя
    public Subtask(Subtask subtask, String name, String description, Status status) {
        super(subtask.getId(), name, description, status);
        this.epicId = subtask.epicId;
    }

    //для метода addNew и update менеджера
    public Subtask(Subtask subtask, int id, int epicId) {
        super(id, subtask.getName(), subtask.getDescription(), subtask.getStatus());
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epic=" + epicId +
                '}';
    }
}
