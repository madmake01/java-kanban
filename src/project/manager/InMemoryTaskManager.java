package project.manager;

import project.enums.Status;
import project.exception.NonexistentEntityException;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static project.exception.TaskExceptionMessage.EPIC_DOES_NOT_EXIST;
import static project.exception.TaskExceptionMessage.SUBTASK_DOES_NOT_EXIST;
import static project.exception.TaskExceptionMessage.TASKS_CANT_HAVE_SAME_ID;
import static project.exception.TaskExceptionMessage.TASK_DOES_NOT_EXIST;

public class InMemoryTaskManager implements TaskManager {
    private final TaskValidator validator;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private int nextId = 1;

    public InMemoryTaskManager(TaskValidator validator, HistoryManager historyManager) {
        this.validator = validator;
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager(TaskValidator validator, HistoryManager historyManager,
                               List<AbstractTask> taskStorage) {
        this.validator = validator;
        this.historyManager = historyManager;

        initialize(taskStorage);
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
            Epic emptyEpic = new Epic.Builder()
                    .fromEpic(epic)
                    .setSubtaskIds(new ArrayList<>())
                    .build();

            updateEpic(emptyEpic);
        }
    }

    @Override
    public Task getTaskWithNotification(int id) {
        Task task = getTaskById(id);
        addToHistoryManager(task);
        return task;
    }

    @Override
    public Epic getEpicWithNotification(int id) {
        Epic epic = getEpicById(id);
        addToHistoryManager(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskWithNotification(int id) {
        Subtask subtask = getSubtaskById(id);
        addToHistoryManager(subtask);
        return subtask;
    }

    @Override
    public Task addTask(Task task) {
        validator.validateNewTask(task);

        int taskId = generateId();
        Task newTask = new Task.Builder()
                .fromTask(task)
                .setId(taskId)
                .build();

        tasks.put(taskId, newTask);
        return newTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        validator.validateNewEpic(epic);

        int epicId = generateId();
        Epic newEpic = new Epic.Builder()
                .fromEpic(epic)
                .setId(epicId)
                .build();
        epics.put(epicId, newEpic);
        return newEpic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask, int epicId) {
        validator.validateNewSubTask(subtask);

        int subtaskId = generateId();

        Subtask newSubtask = new Subtask.Builder()
                .fromSubtask(subtask)
                .setId(subtaskId)
                .setEpicId(epicId)
                .build();

        Epic epic = getEpicById(epicId);
        subtasks.put(subtaskId, newSubtask);

        List<Integer> updatedSubtaskIds = new ArrayList<>(epic.getSubtaskIds());
        updatedSubtaskIds.add(subtaskId);

        Epic updatedEpic = new Epic.Builder()
                .fromEpic(epic)
                .setSubtaskIds(updatedSubtaskIds)
                .build();
        updateEpic(updatedEpic);

        return newSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        int id = task.getId();
        getTaskById(id);

        Task updatedTask = new Task.Builder()
                .fromTask(task)
                .build();
        tasks.put(id, updatedTask);
        return updatedTask;
    }

    /*
     все еще не защищена от того, чтобы тут передать эпик со случайным списком
    */
    @Override
    public Epic updateEpic(Epic epic) {
        int epicId = epic.getId();
        getEpicById(epicId);

        Status updatedStatus = calculateStatus(epic.getSubtaskIds());

        Epic updatedEpic = new Epic.Builder()
                .fromEpic(epic)
                .setStatus(updatedStatus)
                .build();
        epics.put(epicId, updatedEpic);
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        Subtask oldSubtask = getSubtaskById(subtaskId);
        validator.ensureSubtasksEpicsAreEqual(oldSubtask, subtask);

        Epic epic = getEpicById(subtask.getEpicId());

        Subtask updatedSubtask = new Subtask.Builder()
                .fromSubtask(subtask)
                .build();

        subtasks.put(subtaskId, updatedSubtask);
        updateEpic(epic);
        return updatedSubtask;
    }

    @Override
    public Task deleteTask(int id) {
        return removeEntityById(tasks, id, TASK_DOES_NOT_EXIST);
    }

    @Override
    public Epic deleteEpic(int id) {
        Epic removedEpic = removeEntityById(epics, id, EPIC_DOES_NOT_EXIST);

        List<Integer> subtasksIdToRemove = removedEpic.getSubtaskIds();

        for (Integer subtaskId : subtasksIdToRemove) {
            removeEntityById(subtasks, subtaskId, SUBTASK_DOES_NOT_EXIST);
        }
        return removedEpic;
    }

    @Override
    public Subtask deleteSubtask(int id) {
        Subtask removedSubtask = removeEntityById(subtasks, id, SUBTASK_DOES_NOT_EXIST);

        int subtaskEpicId = removedSubtask.getEpicId();
        Epic epic = getEpicById(subtaskEpicId);

        List<Integer> updatedSubtaskIds = new ArrayList<>(epic.getSubtaskIds());
        updatedSubtaskIds.remove((Integer) id);

        Epic updatedEpic = new Epic.Builder()
                .fromEpic(epic)
                .setSubtaskIds(updatedSubtaskIds)
                .build();

        updateEpic(updatedEpic);
        return removedSubtask;
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) {
        Epic epic = getEpicById(id);
        return getSubtasksFromIds(epic.getSubtaskIds());
    }

    @Override
    public List<AbstractTask> getHistory() {
        return historyManager.getDefaultHistory();
    }

    protected List<List<AbstractTask>> getAllTasks() {
        return List.of(
                new ArrayList<>(tasks.values()),
                new ArrayList<>(epics.values()),
                new ArrayList<>(subtasks.values())
        );
    }


    private List<Subtask> getSubtasksFromIds(List<Integer> subtaskIds) {
        return subtaskIds.stream().map(this::getSubtaskById).toList();
    }

    private Task getTaskById(int id) {
        return getEntityById(tasks, id, TASK_DOES_NOT_EXIST);
    }

    private Epic getEpicById(int id) {
        return getEntityById(epics, id, EPIC_DOES_NOT_EXIST);
    }

    private Subtask getSubtaskById(int id) {
        return getEntityById(subtasks, id, SUBTASK_DOES_NOT_EXIST);
    }

    private void addToHistoryManager(AbstractTask task) {
        historyManager.add(task);
    }

    private void removeFromHistoryManager(int id) {
        historyManager.remove(id);
    }

    private int generateId() {
        return nextId++;
    }

    private void initialize(List<AbstractTask> taskStorage) {
        Set<Integer> ids = new HashSet<>();

        for (AbstractTask abstractTask : taskStorage) {
            if (!ids.add(abstractTask.getId())) {
                throw new IllegalStateException(TASKS_CANT_HAVE_SAME_ID);
            }

            switch (abstractTask) {
                case Epic epic -> epics.put(epic.getId(), epic);

                case Subtask subtask -> subtasks.put(subtask.getId(), subtask);

                case Task task -> tasks.put(task.getId(), task);

                default -> throw new IllegalArgumentException("Unknown task type: " + abstractTask.getClass());
            }

            nextId = ids.isEmpty() ? 1 : Collections.max(ids) + 1;
        }
    }

    private Status calculateStatus(List<Integer> subtaskIds) {
        List<Subtask> subtasksFromEpic = getSubtasksFromIds(subtaskIds);
        List<Status> uniqueStatuses = subtasksFromEpic.stream().map(Subtask::getStatus)
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
        removeFromHistoryManager(id);
        T entity = storage.remove(id);
        if (entity == null) {
            throw new NonexistentEntityException(errorMessage + id);
        }
        return entity;
    }
}
