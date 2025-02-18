import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import util.Status;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
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
        Task changedTask = new Task(2, "Новая вторая таска", "Хочу больше не писать гадости",
                Status.DONE);
        Epic changedEpic = new Epic(4, "Новый второй эпик", "В нем должны лежать те же сабтаски",
                taskManager.getEpicSubTasks(4));
        Subtask changedSubtask = new Subtask(6, "Я новая сабтасочка", "Меняю эпик на done",
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
