package project.model;

import project.util.AbstractTaskBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Integer> subTaskIds;

    private Epic(Builder builder) {
        super(builder);
        this.subTaskIds = builder.subTaskIds;
    }

    public List<Integer> getSubTaskIds() {
        return Collections.unmodifiableList(subTaskIds);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subTaskIds +
                '}';
    }

    public static class Builder extends AbstractTaskBuilder<Epic, Builder> {
        private List<Integer> subTaskIds;

        public Builder fromEpic(Epic epic) {
            copyFromAbstractTask(epic);
            this.subTaskIds = new ArrayList<>(epic.getSubTaskIds());
            return self();
        }

        public Builder fromEpicWithNewSubtasks(Epic epic, List<Integer> subTaskIds) {
            copyFromAbstractTask(epic);
            this.subTaskIds = subTaskIds;
            return self();
        }

        public Epic build() {
            validate();
            if (subTaskIds == null) {
                subTaskIds = new ArrayList<>();
            }
            return new Epic(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
