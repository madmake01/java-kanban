package project.model;

import project.util.AbstractTaskBuilder;

import java.util.ArrayList;
import java.util.List;

public class Epic extends AbstractTask {
    private final List<Integer> subTaskIds;

    private Epic(Builder builder) {
        super(builder);
        this.subTaskIds = builder.subTaskIds;
    }

    public List<Integer> getSubTaskIds() {
        return new ArrayList<>(subTaskIds);
    }

    public void addSubtask(int subtaskId) {
        subTaskIds.add(subtaskId);
    }

    public void removeSubtask(Integer subtaskId) {
        subTaskIds.remove(subtaskId);
    }

    public void removeAllSubtasks() {
        subTaskIds.clear();
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
        private List<Integer> subTaskIds = new ArrayList<>();

        public Builder setSubTaskIds(List<Integer> subTaskIds) {
            this.subTaskIds = subTaskIds;
            return this;
        }

        public Builder fromEpic(Epic epic) {
            copyFromAbstractTask(epic);
            this.subTaskIds = epic.getSubTaskIds();

            return this;
        }

        public Epic build() {
            validate();
            return new Epic(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
