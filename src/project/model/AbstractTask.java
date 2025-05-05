package project.model;

import project.enums.Status;
import project.util.AbstractTaskBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractTask {
    private final int id;
    private final String name;
    private final String description;
    private final Status status;

    private final LocalDateTime startTime;
    private final Duration duration;


    protected AbstractTask(AbstractTaskBuilder<?, ?> builder) {
        this.id = builder.getId();
        this.name = builder.getName();
        this.description = builder.getDescription();
        this.status = builder.getStatus();
        this.startTime = builder.getStartTime() == null
                ? null
                : builder.getStartTime().truncatedTo(ChronoUnit.SECONDS);
        this.duration = builder.getDuration();
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

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public Optional<Duration> getDuration() {
        return Optional.ofNullable(duration);
    }

    public Optional<LocalDateTime> getEndTime() {
        if (Objects.isNull(startTime) || Objects.isNull(duration)) {
            return Optional.empty();
        }
        return Optional.of(startTime.plus(duration));
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