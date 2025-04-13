package project.manager;

import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskFileRepository;
import project.util.TaskValidator;

import java.io.File;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(TaskValidator validator, HistoryManager historyManager, File file) {
        super(validator, historyManager);
        this.file = file;
    }

    public FileBackedTaskManager(TaskValidator validator, HistoryManager historyManager,
                                 File file, List<AbstractTask> taskStorage) {
        super(validator, historyManager, taskStorage);
        this.file = file;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Task addTask(Task task) {
        Task addedTask = super.addTask(task);
        save();
        return addedTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic addedEpic = super.addEpic(epic);
        save();
        return addedEpic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask, int epicId) {
        Subtask addedSubtask = super.addSubtask(subtask, epicId);
        save();
        return addedSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        save();
        return updatedSubtask;
    }

    @Override
    public Task deleteTask(int id) {
        Task deletedTask = super.deleteTask(id);
        save();
        return deletedTask;
    }

    @Override
    public Epic deleteEpic(int id) {
        Epic deletedEpic = super.deleteEpic(id);
        save();
        return deletedEpic;
    }

    @Override
    public Subtask deleteSubtask(int id) {
        Subtask deletedSubtask = super.deleteSubtask(id);
        save();
        return deletedSubtask;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        List<AbstractTask> taskStorage = TaskFileRepository.getTasks(file);
        return new FileBackedTaskManager(new TaskValidator(), new InMemoryHistoryManager(), file, taskStorage);
    }

    private void save() {
        TaskFileRepository.saveTasks(file, getAllTasks());
    }
}
