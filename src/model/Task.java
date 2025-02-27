package model;

import enums.Status;

public class Task extends AbstractTask {

    //Создание у пользователя
    public Task(String name, String description, Status status) {
        super(DEFAULT_ID, name, description, status);
    }

    //Обновление у пользователя
    public Task(Task task, String name, String description, Status status) {
        super(task.getId(), name, description, status);
    }

    //для метода addNew и update менеджера
    public Task(Task task, int id) {
        super(id, task.getName(), task.getDescription(), task.getStatus());
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}
