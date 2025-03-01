package project.manager;

import project.enums.Status;
import project.exception.NonexistentEntityException;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static project.exception.TaskExceptionMessage.EPIC_DOES_NOT_EXIST;
import static project.exception.TaskExceptionMessage.SUBTASK_DOES_NOT_EXIST;
import static project.exception.TaskExceptionMessage.TASK_DOES_NOT_EXIST;

public class InMemoryTaskManager implements TaskManager {
    private final TaskValidator validator;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;
    private int nextId = 1;

    public InMemoryTaskManager(TaskValidator validator, HistoryManager historyManager) {
        this.validator = validator;
        this.historyManager = historyManager;

        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    @Override
    public List<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
            updateEpic(epic);
        }
    }

    @Override
    public Task getTaskById(int id) {
        return getEntityById(tasks, id, TASK_DOES_NOT_EXIST);
    }

    @Override
    public Epic getEpicById(int id) {
        return getEntityById(epics, id, EPIC_DOES_NOT_EXIST);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return getEntityById(subtasks, id, SUBTASK_DOES_NOT_EXIST);
    }

    /*
    Первые 4 шага одинаковые, можно какой-нибудь умный Map сделать с consumer
    */
    @Override
    public void addTask(Task task) {
        validator.validateNewTask(task);

        int taskId = generateId();
        Task newTask = new Task.Builder()
                .fromTask(task)
                .setId(taskId)
                .build();

        tasks.put(taskId, newTask);
    }

    @Override
    public void addEpic(Epic epic) {
        validator.validateNewEpic(epic);

        int epicId = generateId();
        Epic newEpic = new Epic.Builder()
                .fromEpic(epic)
                .setId(epicId)
                .build();
        epics.put(epicId, newEpic);
    }

    @Override
    public void addSubtask(Subtask subtask, int epicId) {
        validator.validateNewSubTask(subtask);

        int subtaskId = generateId();

        Subtask newSubtask = new Subtask.Builder()
                .fromSubtask(subtask)
                .setId(subtaskId)
                .setEpicId(epicId)
                .build();

        Epic epic = getEpicByIdInternal(epicId);
        subtasks.put(subtaskId, newSubtask);

        Epic epic = getEpicById(epicId);
        epic.addSubtask(subtaskId);

        updateEpic(epic);
    }

    //выбивается из стиля, по идее можно сделать также, как и другие update, пусть и получится менее оптимально
    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        tasks.compute(id, (k, v) -> {
            if (v == null) {
                throw new NonexistentEntityException(TASK_DOES_NOT_EXIST + id);
            }
            return new Task.Builder()
                    .fromTask(task)
                    .build();
        });
    }

    /*
    Сделать аля-DTO для передачи обновлений, который в принципе может содержать только id + имя + описание + статус
    Заодно получится избавиться от кучи ненужных конструкторов в model
    */

    @Override
    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        getEpicById(epicId);

        Status newStatus = calculateEpicStatus(epicId);
        Epic updatedEpic = new Epic.Builder()
                .fromEpic(epic)
                .setStatus(newStatus)
                .build();
        epics.put(epicId, updatedEpic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        getSubtaskById(subtaskId);

        int subtaskEpicId = subtask.getEpicId();
        Epic epic = getEpicById(subtaskEpicId);

        Subtask updatedSubtask = new Subtask.Builder()
                .fromSubtask(subtask)
                .build();

        subtasks.put(subtaskId, updatedSubtask);
        updateEpic(epic);
    }

    @Override
    public void deleteTask(int id) {
        removeEntityById(tasks, id, TASK_DOES_NOT_EXIST);
    }

    @Override
    public void deleteEpic(int id) {
        Epic removedEpic = removeEntityById(epics, id, EPIC_DOES_NOT_EXIST);

        List<Integer> subtasksIdToRemove = removedEpic.getSubTaskIds();

        for (Integer subtaskId : subtasksIdToRemove) {
            removeEntityById(subtasks, subtaskId, SUBTASK_DOES_NOT_EXIST);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask removedSubtask = removeEntityById(subtasks, id, SUBTASK_DOES_NOT_EXIST);

        int subtaskEpicId = removedSubtask.getEpicId();
        Epic epic = getEpicById(subtaskEpicId);

        epic.removeSubtask(id);
        updateEpic(epic);
    }

    @Override
    public List<Subtask> getEpicSubTasks(int id) {
        Epic epic = getEpicById(id);
        return epic.getSubTaskIds().stream().map(this::getSubtaskById).toList();
    }

    private void notifyHistoryMananager(AbstractTask task) {
        historyManager.add(task);
    }

    private int generateId() {
        return nextId++;
    }

    private Status calculateEpicStatus(int epicId) {
        List<Subtask> subTaskIds = getEpicSubTasks(epicId);
        List<Status> uniqueStatuses = subTaskIds.stream().map(Subtask::getStatus)
                .distinct().toList();

        if (uniqueStatuses.size() > 1 || uniqueStatuses.contains(Status.IN_PROGRESS)) {
            return Status.IN_PROGRESS;
        }

        if (uniqueStatuses.contains(Status.DONE)) {
            return Status.DONE;
        }

        return Status.NEW;
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
