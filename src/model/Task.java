package model;

import enums.Status;

public class Task extends AbstractTask {

    public Task(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Task(String name, String description, Status status) {
        super(-1, name, description, status);
    }

    public Task(int id, Task task) {
        super(id, task.name, task.description, task.status);
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
