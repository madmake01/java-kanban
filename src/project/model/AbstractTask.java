package project.model;

import project.enums.Status;
import project.util.AbstractTaskBuilder;

import java.util.Objects;

public abstract class AbstractTask {
    private final int id;
    private final String name;
    private final String description;
    private final Status status;

    protected AbstractTask(AbstractTaskBuilder<?, ?> builder) {
        this.id = builder.getId();
        this.name = builder.getName();
        this.description = builder.getDescription();
        this.status = builder.getStatus();
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