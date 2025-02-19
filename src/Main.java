import enums.Status;
import exception.EntityAlreadyExistsException;
import exception.NonexistentEntityException;
import manager.TaskManager;
import manager.TaskValidator;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        testPracticumFull();
    }

    private static void testEpic() {
        TaskManager manager = new TaskManager(new TaskValidator());
        Epic epic = new Epic("Я эпик", "Очень эпичный");
        manager.addEpic(epic);
        updateEpic(manager, manager.getEpicById(1));
    }
    private static void updateEpic(TaskManager manager, Epic epic) {
        Epic notEpicEpic = new Epic(epic, "Но не очень эпичный", "Тот же эпик");
        try {
            manager.addEpic(notEpicEpic);
            throw new RuntimeException("epic already exists");
        } catch (EntityAlreadyExistsException e) {
            System.out.println("expected exception was thrown");
        }

        manager.updateEpic(notEpicEpic);
        Epic updatedEpic = manager.getEpicById(1);
        System.out.println(updatedEpic == notEpicEpic); //should be false
    }

    private static void addEpicSubtask() {
        TaskManager manager = new TaskManager(new TaskValidator());
        Epic epic = new Epic("Я эпик", "Очень эпичный");
        manager.addEpic(epic);
    }

    private static void testEmptyGetAll() {
        TaskManager manager = new TaskManager(new TaskValidator());
        checkList(manager.getTasks(), true);
        checkList(manager.getSubtasks(), true);
        checkList(manager.getEpics(), true);
    }

    private static void testGetThrowsException() {
        TaskManager manager = new TaskManager(new TaskValidator());
        try {
            Task task = manager.getTaskById(144);

            throw new RuntimeException("Task was found");
        } catch (NonexistentEntityException e) {
            System.out.println("expected exception was thrown");
        }
    }

    private static <T> void checkList(List<T> list, boolean isEmpty) {
        if (isEmpty) {
            if (!list.isEmpty()) {
                throw new RuntimeException("List should be empty" + list);
            }
        } else {
            if (list.isEmpty()) {
                throw new RuntimeException("List should not be empty" + list);
            }
        }
    }

    private static void testPracticumFull() {
        TaskValidator taskValidator = new TaskValidator();
        TaskManager taskManager = new TaskManager(taskValidator);
        Task firstTask = new Task("Первая задача", "Найти что за собака писала ТЗ", Status.IN_PROGRESS);
        Task secondTask = new Task("Вторая задача", "Извиниться за собаку", Status.NEW);

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);

        List<Task> tasks = taskManager.getTasks();
        System.out.println("Тут должно быть две таски");
        for (Task task : tasks) {
            System.out.println(task);
        }


        System.out.println("\nТут должна остаться только вторая таска");
        taskManager.deleteTask(1);
        tasks = taskManager.getTasks();
        for (Task task : tasks) {
            System.out.println(task);
        }

        Epic oneSubtaskEpic = new Epic("Первый эпик", "Это очень маленький эпик");
        Epic twoSubtaskEpic = new Epic("Второй эпик", "Он чуть побольше");
        taskManager.addEpic(oneSubtaskEpic);
        taskManager.addEpic(twoSubtaskEpic);

        System.out.println("""
                
                Тут пустые эпики:
                """);
        List<Epic> epics = taskManager.getEpics();

        for (Epic epic : epics) {
            System.out.println(epic);
        }

        Subtask subtaskFirstEpic = new Subtask("Выполненная сабтаска",
                "Я должна поменять статус эпика на выполненный эпик", Status.DONE);
        System.out.println("""
                
                Тут первый обновленный эпик:
                """);
        taskManager.addSubtask(subtaskFirstEpic, 3);
        List<Subtask> subtasks = taskManager.getSubtasks();
        System.out.println(subtasks);
        Epic doneEpic = taskManager.getEpicById(3);
        System.out.println(doneEpic);

        System.out.println("""
                
                Тут второй обновленный эпик после добавления первой сабтасочки и второй:
                """);

        Subtask subtaskSecondEpicOne = new Subtask("Просто нью сабтасочка",
                "Ни на что не влияю, лежу тут маленькая", Status.NEW);
        taskManager.addSubtask(subtaskSecondEpicOne, 4);
        System.out.println(taskManager.getEpicById(4));
        Subtask subtaskSecondEpicTwo = new Subtask("Ин прогресс сабтаска",
                "Своим существованием я обрекаю эпик быть инпрогресс", Status.IN_PROGRESS);
        taskManager.addSubtask(subtaskSecondEpicTwo, 4);
        System.out.println(taskManager.getEpicById(4));
        System.out.println("""
                
                Тут смотрим все вместе
                Должно быть 2 эпика и 3 сабтасочки
                """);

        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println("""
                
                Тут удалим маленький эпик и сабтасочку из второго эпика и посмотрим что поменялось.
                """);

        taskManager.deleteEpic(3);
        taskManager.deleteSubtask(7);

        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());


        System.out.println("""
                
                поменяем таску, эпик и сабтаску
                """);
        Task changedTask = new Task(taskManager.getTaskById(2), "Новая вторая таска",
                "Хочу больше не писать гадости",
                Status.DONE);

        Epic changedEpic = new Epic(taskManager.getEpicById(4), "Новый второй эпик", "я уже забыла что тут должно быть"
        );
        Subtask changedSubtask = new Subtask(taskManager.getSubtaskById(6), "Я новая сабтасочка",
                "Меняю эпик на done",
                Status.DONE);


        taskManager.updateTask(changedTask);
        taskManager.updateEpic(changedEpic);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        taskManager.updateSubtask(changedSubtask);
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpics());
    }
}
