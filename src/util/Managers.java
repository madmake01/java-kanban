package util;

import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;

public class Managers {

    private TaskManager defaultTaskManager;
    private HistoryManager defaultHistoryManager;

    private Managers() {
        initializeDefaultTaskManager();
    }

    public static Managers getInstance() {
        return Holder.INSTANCE;
    }

    public TaskManager getDefaultTaskManager() {
        return defaultTaskManager;
    }

    public HistoryManager getDefaultHistoryManager() {
        return defaultHistoryManager;
    }

    private void initializeDefaultTaskManager() {
        TaskValidator validator = new TaskValidator();

        this.defaultHistoryManager = new InMemoryHistoryManager();
        this.defaultTaskManager = new InMemoryTaskManager(validator, defaultHistoryManager);

    }

    private static class Holder {
        private static final Managers INSTANCE = new Managers();
    }
}
