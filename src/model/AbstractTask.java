package model;

import enums.Status;

import java.util.Objects;

public abstract class AbstractTask {
    public static final int DEFAULT_ID = -1;
    private final int id;
    private final String name;
    private final String description;
    private final Status status;

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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractTask obj = (AbstractTask) o;
        return id == obj.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}