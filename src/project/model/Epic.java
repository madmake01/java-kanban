package project.model;

import project.util.AbstractTaskBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Epic extends AbstractTask {
    private final List<Integer> subtaskIds;
    private final LocalDateTime endTime;

    private Epic(Builder builder) {
        super(builder);
        this.subtaskIds = builder.subtaskIds;
        this.endTime = builder.endTime;
    }

    public List<Integer> getSubtaskIds() {
        return Collections.unmodifiableList(subtaskIds);
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

    public static class Builder extends AbstractTaskBuilder<Epic, Builder> {
        private List<Integer> subtaskIds;
        private LocalDateTime endTime;

        public Builder fromEpic(Epic epic) {
            copyFromAbstractTask(epic);
            this.subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            this.endTime = epic.endTime;
            return self();
        }

        public Builder setSubtaskIds(List<Integer> subtaskIds) {
            this.subtaskIds = subtaskIds;
            return self();
        }

        public Builder setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return self();
        }

        @Override
        public Epic build() {
            validate();
            if (subtaskIds == null) {
                subtaskIds = new ArrayList<>();
            }
            return new Epic(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
