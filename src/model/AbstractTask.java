package model;

import util.Status;

import java.util.Objects;

public abstract class AbstractTask {
    protected final int id;
    protected String name;
    protected String description;
    protected Status status;

    protected AbstractTask(int id, String name, String description, Status status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is null");
        }
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

    public void update(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

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