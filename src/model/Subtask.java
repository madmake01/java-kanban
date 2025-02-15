package model;

import util.Status;

public class Subtask extends Task {
    private final Epic epic;

    protected Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    protected Subtask(String name, String description, Epic epic, Status status) {
        super(name, description, status);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }
}
