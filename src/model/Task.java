package model;

import enums.Status;

public class Task extends AbstractTask {

    //Создание у пользователя
    public Task(String name, String description, Status status) {
        super(DEFAULT_ID, name, description, status);
    }

    //Обновление у пользователя
    public Task(Task task, String name, String description, Status status) {
        super(task.id, name, description, status);
    }

    //для метода addNew и update менеджера
    public Task(Task task, int id) {
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
