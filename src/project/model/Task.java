package project.model;

import project.util.AbstractTaskBuilder;

public class Task extends AbstractTask {

    private Task(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }

    public static class Builder extends AbstractTaskBuilder<Task, Builder> {

        public Builder fromTask(Task task) {
            copyFromAbstractTask(task);
            return this;
        }

        @Override
        public Task build() {
            return new Task(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
