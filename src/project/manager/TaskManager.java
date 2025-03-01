package project.manager;

import project.model.Epic;
import project.model.Subtask;
import project.model.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    Task getTaskWithNotification(int id);

    Epic getEpicWithNotification(int id);

    Subtask getSubtaskWithNotification(int id);

    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask, int epicId);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    Task deleteTask(int id);

    Epic deleteEpic(int id);

    Subtask deleteSubtask(int id);

    List<Subtask> getEpicSubTasks(int id);
}
