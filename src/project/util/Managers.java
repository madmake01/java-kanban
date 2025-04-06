package project.util;

import project.manager.FileBackedTaskManager;
import project.manager.HistoryManager;
import project.manager.InMemoryHistoryManager;
import project.manager.InMemoryTaskManager;
import project.manager.TaskManager;
import project.model.AbstractTask;

import java.io.File;
import java.util.List;

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

    public static FileBackedTaskManager loadFromFile(File file) {
        List<AbstractTask> taskStorage = TaskFileRepository.getTasks(file);
        return new FileBackedTaskManager(validator, new InMemoryHistoryManager(), file, taskStorage);
    }

    public static FileBackedTaskManager getDefaultFileBackedTaskManager(File file) {
        return new FileBackedTaskManager(validator, getDefaultHistoryManager(), file);
    }
}
