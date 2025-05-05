package project.util;

import project.enums.Status;
import project.model.AbstractTask;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class AbstractTaskBuilder<T extends AbstractTask, B extends AbstractTaskBuilder<T, B>> {
    public static final int DEFAULT_ID = 0;
    public static final Status DEFAULT_STATUS = Status.NEW;

    private int id = DEFAULT_ID;
    private String name;
    private String description;
    private Status status = DEFAULT_STATUS;
    private LocalDateTime startTime;
    private Duration duration;

    public int getId() {
        return id;
    }

    public B setId(int id) {
        this.id = id;
        return self();
    }

    public String getName() {
        return name;
    }

    public B setName(String name) {
        this.name = name;
        return self();
    }

    public String getDescription() {
        return description;
    }

    public B setDescription(String description) {
        this.description = description;
        return self();
    }

    public Status getStatus() {
        return status;
    }

    public B setStatus(Status status) {
        this.status = status;
        return self();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public B setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return self();
    }

    public Duration getDuration() {
        return duration;
    }

    public B setDuration(Duration duration) {
        this.duration = duration;
        return self();
    }

    public abstract T build();

    protected void copyFromAbstractTask(AbstractTask abstractTask) {
        this.id = abstractTask.getId();
        this.name = abstractTask.getName();
        this.description = abstractTask.getDescription();
        this.status = abstractTask.getStatus();
        this.startTime = abstractTask.getStartTime().orElse(null);
        this.duration = abstractTask.getDuration().orElse(null);
    }

    protected abstract B self();

    protected void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalStateException("Description is required");
        }
    }
}

