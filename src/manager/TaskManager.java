package manager;

import exception.NonexistentEntityException;
import model.Epic;
import model.Subtask;
import model.Task;
import util.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static exception.TaskExceptionMessage.EPICS_SUBTASKS_SHOULD_BE_EQUAL;
import static exception.TaskExceptionMessage.EPIC_DOES_NOT_EXIST;
import static exception.TaskExceptionMessage.SUBTASK_DOES_NOT_EXIST;
import static exception.TaskExceptionMessage.TASK_DOES_NOT_EXIST;

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


    public List<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NonexistentEntityException(TASK_DOES_NOT_EXIST + id);
        }
        return task;
    }

    public void addTask(Task task) {
        int id = generateId();
        Task newTask = new Task(id, task);
        tasks.put(id, newTask);
    }

    public void updateTask(Task task) {
        Task taskToUpdate = getTaskById(task.getId());
        taskToUpdate.update(task.getName(), task.getDescription(), task.getStatus());
    }

    public void deleteTask(int id) {
        Task removedTask = tasks.remove(id);
        if (removedTask == null) {
            throw new NonexistentEntityException(TASK_DOES_NOT_EXIST + id);
        }
    }

    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NonexistentEntityException(EPIC_DOES_NOT_EXIST + id);
        }
        return epic;
    }

    public void addEpic(Epic epic) {
        int id = generateId();
        Epic newEpic = new Epic(id, epic);
        epics.put(id, newEpic);
    }

    //не разрешаю обновлять сабтаски эпика через этот метод, для обновления сабтасок по ТЗ существуют отдельные методы
    public void updateEpic(Epic epic) {
        Epic epicToUpdate = epics.get(epic.getId());

        if (!compareEpicsSubtasks(epicToUpdate, epic)) {
            throw new IllegalArgumentException(EPICS_SUBTASKS_SHOULD_BE_EQUAL);
        }

        Status newStatus = calculateEpicStatus(epicToUpdate);
        epicToUpdate.update(epic.getName(), epic.getDescription(), newStatus);
    }

    public void deleteEpic(int id) {
        Epic removedEpic = epics.remove(id);
        if (removedEpic == null) {
            throw new NonexistentEntityException(EPIC_DOES_NOT_EXIST + id);
        }

        List<Integer> subtasksIdToRemove = removedEpic.getSubTaskList().stream().map(Subtask::getId).toList();
        for (Integer subtaskId : subtasksIdToRemove) {
            Subtask removedSubtask = subtasks.remove(subtaskId);
            //возможно излишняя проверка
            if (removedSubtask == null) {
                throw new NonexistentEntityException(SUBTASK_DOES_NOT_EXIST + subtaskId);
            }
        }
    }

    public List<Subtask> getEpicSubTasks(int id) {
        Epic epic = getEpicById(id);
        return epic.getSubTaskList();
    }

    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NonexistentEntityException(SUBTASK_DOES_NOT_EXIST + id);
        }
        return subtask;
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
        }
    }

    public void addSubtask(Subtask subtask, int epicId) {
        Epic epic = getEpicById(epicId);

        int id = generateId();
        Subtask newSubtask = new Subtask(id, subtask);

        epic.addSubtask(newSubtask);
        updateEpic(epic);
        subtasks.put(id, newSubtask);
    }

    public void updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = subtasks.get(subtask.getId());

        Optional<Epic> optionalEpic = updatedSubtask.getEpic();
        if (optionalEpic.isEmpty()) {
            throw new NonexistentEntityException(EPIC_DOES_NOT_EXIST + subtask);
        }

        Epic epic = optionalEpic.get();
        updatedSubtask.update(subtask.getName(), subtask.getDescription(), subtask.getStatus());
        updateEpic(epic);
    }

    public void deleteSubtask(int id) {
        Subtask removedSubtask = subtasks.remove(id);
        if (removedSubtask == null) {
            throw new NonexistentEntityException(SUBTASK_DOES_NOT_EXIST + id);
        }

        Optional<Epic> optionalEpic = removedSubtask.getEpic();
        if (optionalEpic.isEmpty()) {
            throw new NonexistentEntityException(EPIC_DOES_NOT_EXIST + removedSubtask);
        }

        Epic epic = optionalEpic.get();
        epic.removeSubtask(removedSubtask);
        updateEpic(epic);
    }

    private boolean compareEpicsSubtasks(Epic oldEpic, Epic newEpic) {
        List<Subtask> oldSubtaskList = oldEpic.getSubTaskList();
        List<Subtask> newSubtaskList = newEpic.getSubTaskList();

        return oldSubtaskList.size() == newSubtaskList.size() &&
                new HashSet<>(oldSubtaskList).containsAll(newSubtaskList);
    }

    private Status calculateEpicStatus(Epic epic) {
        List<Subtask> subTaskList = epic.getSubTaskList();
        if (subTaskList.isEmpty()) {
            return Status.NEW;
        }

        boolean hasNew = false;

        for (Subtask subtask : subTaskList) {
            Status subtaskStatus = subtask.getStatus();

            if (subtaskStatus == Status.IN_PROGRESS) {
                return Status.IN_PROGRESS;
            }

            if (subtaskStatus == Status.NEW) {
                hasNew = true;
            }
        }
        return hasNew ? Status.NEW : Status.DONE;
    }

    private int generateId() {
        return nextId++;
    }
}
