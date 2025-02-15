package model;

import util.Status;

import java.util.Objects;


public abstract class AbstractTask {
    private static int nextId = 1;
    protected final int id;
    protected final String name;
    protected final String description;
    protected Status status;

    protected AbstractTask(String name, String description) {
        this.name = name;
        this.description = description;
        this.id = generateId();
        this.status = Status.NEW;
    }

    private static int generateId() {
        return nextId++;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "AbstractTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    //В теории не было instanceof, но как иначе переопределить этот метод без переопределения его в наследниках, если
    // задачи с одним id должны считаться одинаковыми по тз?
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractTask task)) {
            return false;
        }
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}