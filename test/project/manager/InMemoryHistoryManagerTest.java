package project.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import project.model.AbstractTask;
import project.model.Task;
import project.util.TaskUtility;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddSingleTaskToHistory() {
        AbstractTask task = TaskUtility.createEpic();

        historyManager.add(task);
        List<AbstractTask> tasks = historyManager.getDefaultHistory();

        assertEquals(1, tasks.size());
        TaskUtility.assertAbstractTaskEquals(task, tasks.getFirst());
    }

    @ParameterizedTest(name = "Adding {0} unique tasks should result in {0} history size")
    @ValueSource(ints = {0, 1, 2, 50, 10000})
    void shouldAddMultipleUniqueTasksToHistory(int size) {
        for (int i = 1; i <= size; i++) {
            historyManager.add(TaskUtility.createTaskBuilder().setId(i).build());
        }

        List<AbstractTask> tasks = historyManager.getDefaultHistory();
        assertEquals(size, tasks.size());
    }

    @ParameterizedTest(name = "Remove task with ID={1} from {0} tasks -> expect size={2}")
    @CsvSource({
            "0,  1,  0",
            "1,  1,  0",
            "5,  1,  4",
            "5,  3,  4",
            "5,  5,  4",
            "5, 10,  5"
    })
    void shouldRemoveTaskFromHistoryCorrectly(int tasksAmount, int idToRemove, int expectedSize) {
        for (int i = 1; i <= tasksAmount; i++) {
            historyManager.add(TaskUtility.createTaskBuilder().setId(i).build());
        }

        historyManager.remove(idToRemove);
        List<AbstractTask> tasks = historyManager.getDefaultHistory();

        assertAll("After removal",
                () -> assertEquals(expectedSize, tasks.size(), "Unexpected number of tasks"),
                () -> assertFalse(tasks.stream().anyMatch(t -> t.getId() == idToRemove))
        );
    }

    @Test
    void shouldMoveExistingTaskToEndOnReAdd() {
        Task task1 = TaskUtility.createTaskBuilder().setId(1).build();
        Task task2 = TaskUtility.createTaskBuilder().setId(2).build();

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<AbstractTask> tasks = historyManager.getDefaultHistory();
        assertEquals(2, tasks.size());
        assertEquals(task2, tasks.get(0));
        assertEquals(task1, tasks.get(1));
    }

    @Test
    void shouldNotDuplicateTaskOnRepeatedAccess() {
        Task task = TaskUtility.createTaskBuilder().setId(1).build();

        for (int i = 0; i < 10; i++) {
            historyManager.add(task);
        }

        List<AbstractTask> tasks = historyManager.getDefaultHistory();
        assertEquals(1, tasks.size(), "History should contain only one instance of the task");
        assertEquals(task, tasks.getFirst());
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        List<AbstractTask> tasks = historyManager.getDefaultHistory();
        assertTrue(tasks.isEmpty(), "History should be empty after initialization");
    }

}