package project.manager;

import project.exception.ManagerSaveException;
import project.mapper.AbstractTaskSerializer;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskValidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static project.exception.TaskExceptionMessage.ERROR_SAVING_DATA;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(TaskValidator validator, HistoryManager historyManager, File file) {
        super(validator, historyManager);
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


    private void save() {
        List<List<? extends AbstractTask>> taskList = getAllTasks();

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (List<? extends AbstractTask> list : taskList) {
                for (AbstractTask task : list) {
                    String serializedTask = AbstractTaskSerializer.serialize(task);
                    writer.write(serializedTask);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException(ERROR_SAVING_DATA, e);
        }
    }

    private List<List<? extends AbstractTask>> getAllTasks() {
        List<Task> tasks = getTasks();
        List<Epic> epics = getEpics();
        List<Subtask> subtasks = getSubtasks();

        return List.of(tasks, epics, subtasks);
    }
}
