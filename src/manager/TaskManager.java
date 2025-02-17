package manager;

import exception.NonexistentEntityException;
import model.Epic;
import model.Subtask;
import model.Task;
import util.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static exception.TaskExceptionMessage.ASSOCIATED_EPIC_DOES_NOT_EXIST;
import static exception.TaskExceptionMessage.EPIC_DOES_NOT_EXIST;
import static exception.TaskExceptionMessage.SUBTASK_DOES_NOT_ASSOCIATED;
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

    private int generateId() {
        return nextId++;
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
        int id = task.getId();
        tasks.compute(id, (k, v) -> {
            if (v == null) {
                throw new NonexistentEntityException(TASK_DOES_NOT_EXIST + id);
            }
            return new Task(id, task);
        });
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

    public void deleteEpicsAndSubtasks() {
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
        Status newStatus = calculateEpicStatus(epic);
        Epic newEpic = new Epic(id, epic, newStatus);
        epics.put(id, newEpic);

        List<Subtask> newSubtasks = epic.getSubTaskList();
        for (Subtask subtask : newSubtasks) {
            addNewEpicSubtask(subtask);
        }
    }

    //todo рассчитать статус, разрешить обновлять только имена / описание или ебаться с листами сабтасок
    public void updateEpic(Epic epic) {
        int idToUpdate = epic.getId();

        if (!epics.containsKey(idToUpdate)) {
            throw new NonexistentEntityException(EPIC_DOES_NOT_EXIST + idToUpdate);
        }

        Epic oldEpic = epics.get(idToUpdate);

        List<Subtask> oldEpicSubtasksId = oldEpic.getSubTaskList();
        for (Subtask subtask : oldEpicSubtasksId) {
            subtasks.remove(subtask.getId());
        }

        List<Subtask> newEpicSubtasks = epic.getSubTaskList();
        for (Subtask subtask : newEpicSubtasks) {
        //    addNewEpicSubtask(subtask);
        }

        epics.put(epic.getId(), epic);
    }

    //todo удалить subtask
    public void deleteEpic(int id) {
        Epic removedEpic = epics.remove(id);
        if (removedEpic == null) {
            throw new NonexistentEntityException(EPIC_DOES_NOT_EXIST + id);
        }
    }

    //не очень понятно что именно должно было быть аргументом: id ли Epic
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

    //todo посмотреть еще раз, как будно хуйня какая-то с эпиком
    private void addNewEpicSubtask(Subtask subtask) {
        int id = generateId();
        Subtask newSubtask = new Subtask(id, subtask);
        int subtaskEpicId = subtask.getEpic().map(Epic::getId)
                .orElseThrow(() -> new NonexistentEntityException(SUBTASK_DOES_NOT_ASSOCIATED + subtask));
        if (!epics.containsKey(subtaskEpicId)) {
            throw new NonexistentEntityException(ASSOCIATED_EPIC_DOES_NOT_EXIST + subtask);
        }
        subtasks.put(id, newSubtask);
    }

    public void addSubtask(Subtask subtask, int epicId) {
        Epic epic = getEpicById(epicId);
        int id = generateId();
        Subtask newSubtask = new Subtask(id, subtask);
        epic.addSubtask(newSubtask);
        subtasks.put(id, newSubtask);
    }
//todo хуево
    public void updateSubtask(Subtask subtask) {
        int idToUpdate = subtask.getId();
        Epic changedEpic = deleteSubtask(idToUpdate);

    }

    public Epic deleteSubtask(int id) {
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
        return epic;
    }

    public Status calculateEpicStatus(Epic epic) {
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
}
