package project.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.model.AbstractTask;
import project.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager list;

    @BeforeEach
    void setUp() {
        list = new InMemoryHistoryManager();
    }

    @Test
    void testAddSingleTask() {
        Task task = new Task.Builder()
                .setId(1)
                .setName("Task 1")
                .build();

        list.add(task);
        List<AbstractTask> tasks = list.getDefaultHistory();

        assertEquals(1, tasks.size());
        assertEquals(task, tasks.getFirst());
    }

    @Test
    void testAddMultipleTasks() {
        for (int i = 1; i <= 3; i++) {
            list.add(new Task.Builder()
                    .setId(i)
                    .setName("Task " + i)
                    .build());
        }

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(3, tasks.size());
        assertEquals("Task 1", tasks.get(0).getName());
        assertEquals("Task 3", tasks.get(2).getName());
    }

    @Test
    void testRemoveMiddleTask() {
        Task task1 = new Task.Builder().setId(1).setName("Task 1").build();
        Task task2 = new Task.Builder().setId(2).setName("Task 2").build();
        Task task3 = new Task.Builder().setId(3).setName("Task 3").build();

        list.add(task1);
        list.add(task2);
        list.add(task3);

        list.remove(2);

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(2, tasks.size());
        assertFalse(tasks.contains(task2));
        assertEquals(task1, tasks.get(0));
        assertEquals(task3, tasks.get(1));
    }

    @Test
    void testRemoveHead() {
        Task task1 = new Task.Builder().setId(1).setName("Task 1").build();
        Task task2 = new Task.Builder().setId(2).setName("Task 2").build();

        list.add(task1);
        list.add(task2);

        list.remove(1);

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(1, tasks.size());
        assertEquals(task2, tasks.getFirst());
    }

    @Test
    void testRemoveTail() {
        Task task1 = new Task.Builder().setId(1).setName("Task 1").build();
        Task task2 = new Task.Builder().setId(2).setName("Task 2").build();

        list.add(task1);
        list.add(task2);

        list.remove(2);

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(1, tasks.size());
        assertEquals(task1, tasks.getFirst());
    }

    @Test
    void testRemoveSingleElement() {
        Task task = new Task.Builder().setId(1).setName("Task 1").build();
        list.add(task);

        list.remove(1);

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testReAddExistingTask() {
        Task task1 = new Task.Builder().setId(1).setName("Task 1").build();
        Task task2 = new Task.Builder().setId(2).setName("Task 2").build();

        list.add(task1);
        list.add(task2);
        list.add(task1);

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(2, tasks.size());
        assertEquals(task2, tasks.get(0));
        assertEquals(task1, tasks.get(1));
    }

    @Test
    void testRemoveNonExistentTask() {
        Task task = new Task.Builder().setId(1).setName("Task 1").build();
        list.add(task);

        list.remove(2);

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(1, tasks.size());
        assertEquals(task, tasks.getFirst());
    }

    @Test
    void testRepeatedViewDoesNotIncreaseSize() {
        Task task = new Task.Builder().setId(1).setName("Task 1").build();

        for (int i = 0; i < 10; i++) {
            list.add(task);
        }

        List<AbstractTask> tasks = list.getDefaultHistory();
        assertEquals(1, tasks.size(), "Список должен содержать только одну задачу");
        assertEquals(task, tasks.getFirst(), "Задача должна быть той же самой");
    }
}
