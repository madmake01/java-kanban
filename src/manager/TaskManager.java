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

//Я все переделала. Надеюсь, что хуже не стало :'(
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
            updateEpic(epic);
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

    /*
    Первые 4 шага одинаковые, можно какой-нибудь умный Map сделать с consumer
    */
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
        Subtask newSubtask = new Subtask(subtask, subtaskId, epicId);

        subtasks.put(subtaskId, newSubtask);

        Epic epic = getEpicById(epicId);
        epic.addSubtask(subtaskId);

        updateEpic(epic);
    }

    //выбивается из стиля, по идее можно сделать также, как и другие update, пусть и получится менее оптимально
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
    Сделать аля-DTO для передачи обновлений, который в принципе может содержать только id + имя + описание + статус
    Заодно получится избавиться от кучи ненужных конструкторов в model
    */

    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        getEpicById(epicId);

        /*
        validator.ensureEpicSubtasksAreEqual(oldEpic, epic)
        все равно не спасет, ведь в get я
        возвращаю ссылки и там меняют, что хотят. Подумать об альтернативах
        */

        Status newStatus = calculateEpicStatus(epic);
        epics.put(epicId, new Epic(epic, newStatus));
    }

    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        getSubtaskById(subtaskId);

        /*
        validator.ensureSubtasksEpicsAreEqual(updatedSubtask, subtask);
        В принципе аналогично предыдущему пункту
        */
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
        List<Status> uniqueStatuses = subTaskList.stream().map(this::getSubtaskById).map(Subtask::getStatus)
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
