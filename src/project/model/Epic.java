package project.model;

import project.util.AbstractTaskBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Integer> subtaskIds;

    private Epic(Builder builder) {
        super(builder);
        this.subtaskIds = builder.subtaskIds;
    }

    public List<Integer> getSubtaskIds() {
        return Collections.unmodifiableList(subtaskIds);
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

        public Builder fromEpic(Epic epic) {
            copyFromAbstractTask(epic);
            this.subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            return self();
        }

        public Builder fromEpicWithNewSubtasks(Epic epic, List<Integer> subtaskIds) {
            copyFromAbstractTask(epic);
            this.subtaskIds = subtaskIds;
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
