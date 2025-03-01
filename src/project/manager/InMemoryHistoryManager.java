package project.manager;

import project.model.AbstractTask;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<AbstractTask> history;

    public InMemoryHistoryManager() {
        this.history = new LinkedList<>();
    }

    @Override
    public void add(AbstractTask task) {
        if (history.size() == MAX_HISTORY_SIZE) {
            history.removeLast();
        }

        history.addFirst(task);
    }

    @Override
    public List<AbstractTask> getHistory() {
        return List.copyOf(this.history);
    }
}
