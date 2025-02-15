package model;

import util.Status;

public class Subtask extends AbstractTask {
    private final Epic epic;

    public Subtask(int id, String name, String description, Status status, Epic epic) {
        super(id, name, description, status);
        this.epic = epic;
    }

    public Subtask(int id, Subtask subtask) {
        super(id, subtask.getName(), subtask.getDescription(), subtask.getStatus());
        this.epic = subtask.getEpic();
    }

    public Epic getEpic() {
        return epic;
    }
}
