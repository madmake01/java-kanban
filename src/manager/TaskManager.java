package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import util.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return List.copyOf(tasks.values());
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public Optional<Task> getTaskById(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public void addTask(Task task) {
        int id = generateId();
        Task newTask = new Task(id, task);
        tasks.put(id, newTask);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    public void deleteEpics() {
        epics.clear();
    }

    public Optional<Epic> getEpicById(int id) {
        return Optional.ofNullable(epics.get(id));
    }

    public void addEpic(Epic epic) {
        int id = generateId();
        Status newStatus = epic.checkStatus();
        Epic newEpic = new Epic(id, epic, newStatus);
        epics.put(id, newEpic);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void deleteEpic(int id) {
        epics.remove(id);
    }

    //не очень понятно что именно должно было быть аргументом: id ли Epic
    public Optional<List<Subtask>> getEpicSubTasks(int id) {
        Optional<Epic> optionalEpic = getEpicById(id);
        if (optionalEpic.isEmpty()) {
            return Optional.empty();
        }

        Epic epic = optionalEpic.get();
        return Optional.of(epic.getSubTaskList());
    }

    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    public void deleteSubtasks() {
        subtasks.clear();
    }

    public Optional<Subtask> getSubtaskById(int id) {
        return Optional.ofNullable(subtasks.get(id));
    }

    public void addSubtask(Subtask subtask) {
        int id = generateId();
        Subtask newSubtask = new Subtask(id, subtask);
        subtasks.put(id, newSubtask);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }
}
