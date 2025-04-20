package project.manager;

import project.enums.Status;
import project.exception.NonexistentEntityException;
import project.exception.TaskIntersectionException;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static project.exception.TaskExceptionMessage.EPIC_DOES_NOT_EXIST;
import static project.exception.TaskExceptionMessage.SUBTASK_DOES_NOT_EXIST;
import static project.exception.TaskExceptionMessage.TASKS_CANT_HAVE_SAME_ID;
import static project.exception.TaskExceptionMessage.TASKS_CANT_INTERSECT;
import static project.exception.TaskExceptionMessage.TASK_DOES_NOT_EXIST;

public class InMemoryTaskManager implements TaskManager {
    private final TaskValidator validator;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Set<AbstractTask> prioritizedTasks = new TreeSet<>((o1, o2) -> {
        if (o1.getStartTime().isEmpty() || o2.getStartTime().isEmpty()) {
            throw new NonexistentEntityException("StartTime is null");
        }
        return o1.getStartTime().get().compareTo(o2.getStartTime().get());
    });
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
        deleteTasksOfExactTypes(Task.class);
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
        deleteTasksOfExactTypes(Subtask.class);
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        deleteTasksOfExactTypes(Subtask.class);
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
        isIntersect(task);
        int taskId = generateId();
        Task newTask = new Task.Builder()
                .fromTask(task)
                .setId(taskId)
                .build();

        tasks.put(taskId, newTask);
        addToPriorityList(newTask);
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
        isIntersect(subtask);
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
        addToPriorityList(newSubtask);

        return newSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        int id = task.getId();
        Task oldTask = getTaskById(id);
        isIntersect(task);
        Task updatedTask = new Task.Builder()
                .fromTask(task)
                .build();
        tasks.put(id, updatedTask);
        updatePriorityList(oldTask, updatedTask);
        return updatedTask;
    }


    @Override
    public Epic updateEpic(Epic epic) {
        int epicId = epic.getId();
        getEpicById(epicId);

        Status updatedStatus = calculateStatus(epic.getSubtaskIds());


        TimeAggregate timeAggregate = aggregateTimeSummary(epic);

        Epic updatedEpic = new Epic.Builder()
                .fromEpic(epic)
                .setStatus(updatedStatus)
                .setStartTime(timeAggregate.startTime)
                .setDuration(timeAggregate.duration)
                .setEndTime(timeAggregate.endTime)
                .build();
        epics.put(epicId, updatedEpic);
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        Subtask oldSubtask = getSubtaskById(subtaskId);
        validator.ensureSubtasksEpicsAreEqual(oldSubtask, subtask);
        isIntersect(subtask);
        Epic epic = getEpicById(subtask.getEpicId());

        Subtask updatedSubtask = new Subtask.Builder()
                .fromSubtask(subtask)
                .build();

        subtasks.put(subtaskId, updatedSubtask);
        updatePriorityList(oldSubtask, updatedSubtask);
        updateEpic(epic);
        return updatedSubtask;
    }

    @Override
    public Task deleteTask(int id) {
        Task task = removeEntityById(tasks, id, TASK_DOES_NOT_EXIST);
        deleteFromPriorityList(task);
        return task;
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
        deleteFromPriorityList(removedSubtask);
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

    public List<AbstractTask> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    protected List<List<AbstractTask>> getAllTasks() {
        return List.of(
                new ArrayList<>(tasks.values()),
                new ArrayList<>(epics.values()),
                new ArrayList<>(subtasks.values())
        );
    }

    private boolean compareTaskTimeData(AbstractTask firstTask, AbstractTask secondTask) {
        Optional<LocalDateTime> firstStartTime = firstTask.getStartTime();
        Optional<LocalDateTime> secondStartTime = secondTask.getStartTime();
        Optional<LocalDateTime> firstEndTime = firstTask.getEndTime();
        Optional<LocalDateTime> secondEndTime = secondTask.getEndTime();

        if (firstStartTime.isPresent() && secondStartTime.isPresent()
                && firstEndTime.isPresent() && secondEndTime.isPresent()) {

            LocalDateTime start1 = firstStartTime.get();
            LocalDateTime end1 = firstEndTime.get();
            LocalDateTime start2 = secondStartTime.get();
            LocalDateTime end2 = secondEndTime.get();

            return start1.isBefore(end2) && start2.isBefore(end1);

        }

        return false;
    }

    private void isIntersect(AbstractTask task) {
        boolean anyMatch = prioritizedTasks.stream().anyMatch(prioritizedTask -> {
            if (prioritizedTask.equals(task)) {
                return false;
            }
            return compareTaskTimeData(prioritizedTask, task);
        });
        if (anyMatch) {
            throw new TaskIntersectionException(TASKS_CANT_INTERSECT);
        }
    }


    private void addToPriorityList(AbstractTask task) {
        task.getStartTime().ifPresent(st -> prioritizedTasks.add(task));
    }

    private void deleteFromPriorityList(AbstractTask task) {
        task.getStartTime().ifPresent(st -> prioritizedTasks.remove(task));
    }

    private void updatePriorityList(AbstractTask taskToDelete, AbstractTask taskToAdd) {
        deleteFromPriorityList(taskToDelete);
        addToPriorityList(taskToAdd);
    }

    private void deleteTasksOfExactTypes(Class<? extends AbstractTask> taskClass) {
        prioritizedTasks.removeIf(task -> task.getClass().equals(taskClass));
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

                case Subtask subtask -> {
                    subtasks.put(subtask.getId(), subtask);
                    addToPriorityList(subtask);
                }

                case Task task -> {
                    tasks.put(task.getId(), task);
                    addToPriorityList(task);
                }

                default -> throw new IllegalArgumentException("Unknown task type: " + abstractTask.getClass());
            }
        }
        nextId = ids.isEmpty() ? 1 : Collections.max(ids) + 1;
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

    private TimeAggregate aggregateTimeSummary(Epic epic) {
        List<Subtask> subtasksEpic = getSubtasksFromIds(epic.getSubtaskIds());

        return subtasksEpic.stream().collect(TimeAggregate::new, TimeAggregate::accept, TimeAggregate::merge);
    }

    private static class TimeAggregate {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration duration;

        private void accept(Subtask subtask) {
            subtask.getStartTime()
                    .filter(t -> startTime == null || t.isBefore(startTime))
                    .ifPresent(t -> startTime = t);

            subtask.getEndTime()
                    .filter(t -> endTime == null || t.isAfter(endTime))
                    .ifPresent(t -> endTime = t);

            subtask.getDuration()
                    .map(d -> duration == null ? d : duration.plus(d))
                    .ifPresent(d -> duration = d);
        }

        private void merge(TimeAggregate other) {
            if (other.startTime != null && (startTime == null || other.startTime.isBefore(startTime))) {
                startTime = other.startTime;
            }

            if (other.endTime != null && (endTime == null || other.endTime.isAfter(endTime))) {
                endTime = other.endTime;
            }

            if (other.duration != null) {
                if (duration == null) {
                    duration = other.duration;
                } else {
                    duration = duration.plus(other.duration);
                }
            }
        }
    }
}
