package model;

import util.Status;

import java.util.Objects;

//abstactTask существует чтобы не нарушать принцип Лисков (update task, например)
public abstract class AbstractTask {
    protected final int id;
    protected String name;
    protected String description;
    protected Status status;

    protected AbstractTask(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
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

    //В теории не было instanceof, но как иначе переопределить если
    // задачи с одним id должны считаться одинаковыми по тз? Три раза класс сравнивать ?:)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
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