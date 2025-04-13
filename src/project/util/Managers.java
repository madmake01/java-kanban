package project.util;

import project.manager.HistoryManager;
import project.manager.InMemoryHistoryManager;
import project.manager.InMemoryTaskManager;
import project.manager.TaskManager;

public class Managers {
    private static final TaskValidator validator = new TaskValidator();

    private Managers() {
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager(validator, getDefaultHistoryManager());
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
