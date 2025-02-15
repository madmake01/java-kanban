package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import util.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private int nextId = 1;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    private int generateId() {
        return nextId++;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        //todo validate or optional
        return tasks.get(id);
    }

    public void addTask(Task task) {
        //todo validate
        int id = generateId();
        Task newTask = new Task(id, task);
        tasks.put(id, newTask);
    }

    public void updateTask(Task task) {
        //todo validate
        tasks.put(task.getId(), task);
    }

    public void deleteTask(int id) {
        //todo validate ?
        tasks.remove(id);
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteEpics() {
        epics.clear();
    }

    public Epic getEpicById(int id) {
        //todo validate or optional
        return epics.get(id);
    }

    public void addEpic(Epic epic) {
        //todo validate
        int id = generateId();
        Status newStatus = epic.checkStatus();
        Epic newEpic = new Epic(id, epic,  newStatus);
        epics.put(id, newEpic);
    }

    public void updateEpic(Epic epic) {
        //todo validate
        epics.put(epic.getId(), epic);
    }

    public void deleteEpic(int id) {
        //todo validate ?
       epics.remove(id);
        //todo delete epic subtasks
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteSubtasks() {
        //todo удалить из каждого эпика этот subtask
        subtasks.clear();
    }

    public Subtask getSubtaskById(int id) {
        //todo validate or optional
        return subtasks.get(id);
    }

    public void addSubtask(Subtask subtask) {
        int id = generateId();
        Subtask newSubtask = new Subtask(id, subtask);
        //todo проверить что epic есть в списке?
        subtasks.put(id, newSubtask);
    }

    public void updateSubtask(Subtask subtask) {
        //todo validate
        subtasks.put(subtask.getId(), subtask);
    }

    public void deleteSubtask(int id) {
        //todo remove subtask from epic
        subtasks.remove(id);
    }
}
