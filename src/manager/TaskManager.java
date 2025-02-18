package manager;

import enums.Status;
import exception.NonexistentEntityException;
import model.AbstractTask;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static exception.TaskExceptionMessage.EPIC_DOES_NOT_EXIST;
import static exception.TaskExceptionMessage.SUBTASK_DOES_NOT_EXIST;
import static exception.TaskExceptionMessage.TASK_DOES_NOT_EXIST;

public class TaskManager {
    private final TaskValidator validator;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private int nextId = 1;

    public TaskManager(TaskValidator validator) {
        this.validator = validator;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public List<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
        }
    }

    public Task getTaskById(int id) {
        return getEntityById(tasks, id, TASK_DOES_NOT_EXIST);
    }

    public Epic getEpicById(int id) {
        return getEntityById(epics, id, EPIC_DOES_NOT_EXIST);
    }

    public Subtask getSubtaskById(int id) {
        return getEntityById(subtasks, id, SUBTASK_DOES_NOT_EXIST);
    }

    public void addTask(Task task) {
        validator.validateNewTask(task);

        int taskId = generateId();
        Task newTask = new Task(task, taskId);
        tasks.put(taskId, newTask);
    }

    public void addEpic(Epic epic) {
        validator.validateNewEpic(epic);

        int epicId = generateId();
        Epic newEpic = new Epic(epic, epicId);
        epics.put(epicId, newEpic);
    }

    public void addSubtask(Subtask subtask, int epicId) {
        validator.validateNewSubTask(subtask);

        int subtaskId = generateId();
        Epic epic = getEpicById(epicId);
        Subtask newSubtask = new Subtask(subtask, subtaskId, epicId);

        epic.addSubtask(subtaskId);
        subtasks.put(subtaskId, newSubtask);
        updateEpic(epic);
    }

    public void updateTask(Task task) {
        int id = task.getId();
        tasks.compute(id, (k, v) -> {
            if (v == null) {
                throw new NonexistentEntityException(TASK_DOES_NOT_EXIST + id);
            }
            return new Task(task, id);
        });
    }

    /*
    Очень хочется переопределить equals в Epic и Subtask, чтобы избавиться от лишних методов validator, которые по сути
    equals
    Или сделать аля-DTO для передачи обновлений, который в принципе может содержать только id + имя + описание + статус
    */

    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        Epic oldEpic = getEpicById(epicId);

        validator.ensureEpicSubtasksAreEqual(oldEpic, epic);

        Status newStatus = calculateEpicStatus(epic);
        epics.put(epicId, new Epic(epic, newStatus));
    }

    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        Subtask updatedSubtask = getSubtaskById(subtaskId);

        validator.ensureSubtasksEpicsAreEqual(updatedSubtask, subtask);

        int subtaskEpicId = subtask.getEpicId();
        Epic epic = getEpicById(subtaskEpicId);


        subtasks.put(subtaskId, new Subtask(subtask, subtaskId, subtaskEpicId));
        updateEpic(epic);
    }

    public void deleteTask(int id) {
        removeEntityById(tasks, id, TASK_DOES_NOT_EXIST);
    }

    public void deleteEpic(int id) {
        Epic removedEpic = removeEntityById(epics, id, EPIC_DOES_NOT_EXIST);

        List<Integer> subtasksIdToRemove = removedEpic.getSubTaskIdList();

        for (Integer subtaskId : subtasksIdToRemove) {
            removeEntityById(subtasks, subtaskId, SUBTASK_DOES_NOT_EXIST);
        }
    }

    public void deleteSubtask(int id) {
        Subtask removedSubtask = removeEntityById(subtasks, id, SUBTASK_DOES_NOT_EXIST);

        int subtaskEpicId = removedSubtask.getEpicId();
        Epic epic = getEpicById(subtaskEpicId);

        epic.removeSubtask(id);
        updateEpic(epic);
    }

    public List<Subtask> getEpicSubTasks(int id) {
        Epic epic = getEpicById(id);
        return epic.getSubTaskIdList().stream().map(this::getSubtaskById).toList();
    }

    private int generateId() {
        return nextId++;
    }

    private Status calculateEpicStatus(Epic epic) {
        List<Integer> subTaskList = epic.getSubTaskIdList();
        boolean hasNew = false;

        for (Integer subtaskId : subTaskList) {
            Subtask subtask = getSubtaskById(subtaskId);
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

    private <T extends AbstractTask> T getEntityById(Map<Integer, T> storage, int id, String errorMessage) {
        T entity = storage.get(id);
        if (entity == null) {
            throw new NonexistentEntityException(errorMessage + id);
        }
        return entity;
    }

    private <T extends AbstractTask> T removeEntityById(Map<Integer, T> storage, int id, String errorMessage) {
        T entity = storage.remove(id);
        if (entity == null) {
            throw new NonexistentEntityException(errorMessage + id);
        }
        return entity;
    }
}
