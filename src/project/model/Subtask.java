package project.model;

import project.util.AbstractTaskBuilder;

public class Subtask extends AbstractTask {
    private final int epicId;

    private Subtask(Builder builder) {
        super(builder);
        this.epicId = builder.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epic=" + epicId +
                '}';
    }

    public static class Builder extends AbstractTaskBuilder<Subtask, Builder> {
        private int epicId = DEFAULT_ID;

        public Builder setEpicId(int epicId) {
            this.epicId = epicId;
            return self();
        }

        public Builder fromSubtask(Subtask subtask) {
            copyFromAbstractTask(subtask);
            this.epicId = subtask.getEpicId();

            return self();
        }

        @Override
        public Subtask build() {
            validate();
            return new Subtask(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
