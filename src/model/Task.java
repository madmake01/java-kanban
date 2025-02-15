package model;

import util.Status;

public class Task extends AbstractTask {

    public Task(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Task(int id, Task task) {
        super(id, task.name,  task.description, task.status);
    }
}
