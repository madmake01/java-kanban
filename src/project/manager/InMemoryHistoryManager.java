package project.manager;

import project.model.AbstractTask;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<AbstractTask> history;

    public InMemoryHistoryManager() {
        this.history = new LinkedList<>();
    }

    @Override
    public void add(AbstractTask task) {
        history.add(task);
    }

    @Override
    public List<AbstractTask> getDefaultHistory() {
        return List.copyOf(this.history);
    }
}
