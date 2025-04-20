package project.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.exception.ManagerSaveException;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.TaskUtility;
import project.util.TaskValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static project.util.TaskFileRepository.CSV_HEADER;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test_tasks", ".csv");
        tempFile.deleteOnExit();
        taskManager = new FileBackedTaskManager(new TaskValidator(), new InMemoryHistoryManager(), tempFile);
    }

    @Test
    void shouldPreserveEpicOnLoad() {
        Epic savedEpic = taskManager.addEpic(TaskUtility.createEpic());
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpics().getFirst();

        assertAll("Epic properties should be preserved",
                () -> assertEquals(savedEpic.getId(), loadedEpic.getId()),
                () -> assertEquals(savedEpic.getName(), loadedEpic.getName()),
                () -> assertEquals(savedEpic.getDescription(), loadedEpic.getDescription())
        );
    }

    @Test
    void shouldPreserveSubtaskOnLoad() {
        Epic epic = taskManager.addEpic(TaskUtility.createEpic());
        Subtask savedSubtask = taskManager.addSubtask(TaskUtility.createSubtask(), epic.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Subtask loadedSubtask = loadedManager.getSubtasks().getFirst();

        TaskUtility.assertAbstractTaskEquals(savedSubtask, loadedSubtask);
    }

    @Test
    void shouldPreserveTaskOnLoad() {
        Task savedTask = taskManager.addTask(TaskUtility.createTask());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTasks().getFirst();

        TaskUtility.assertAbstractTaskEquals(savedTask, loadedTask);
    }

    @Test
    void shouldThrowExceptionForIncorrectHeader() throws IOException {
        Files.writeString(tempFile.toPath(), "wrong header");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
    }

    @Test
    void shouldLoadEmptyManagerFromCorrectHeader() throws IOException {
        Files.writeString(tempFile.toPath(), CSV_HEADER);
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll("All collections must be empty",
                () -> assertNotNull(manager),
                () -> assertTrue(manager.getTasks().isEmpty()),
                () -> assertTrue(manager.getEpics().isEmpty()),
                () -> assertTrue(manager.getSubtasks().isEmpty()),
                () -> assertTrue(manager.getHistory().isEmpty())
        );
    }

    @Test
    void saveEmptyManagerShouldCreateFileWithOnlyHeader() throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(new TaskValidator(), new InMemoryHistoryManager(), tempFile);
        manager.deleteSubtasks(); // trigger save

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);

        try (Stream<String> lines = Files.lines(tempFile.toPath())) {
            List<String> content = lines.toList();
            assertEquals(1, content.size());
            assertEquals(CSV_HEADER, content.getFirst());
        }
    }

    @Test
    void shouldLoadEmptyManagerCorrectlyFromSavedEmptyFile() {
        FileBackedTaskManager manager = new FileBackedTaskManager(new TaskValidator(), new InMemoryHistoryManager(), tempFile);
        manager.deleteTasks();
        manager.deleteEpics();
        manager.deleteSubtasks();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll("All entities and history must remain empty after loading",
                () -> assertNotNull(loaded),
                () -> assertTrue(loaded.getTasks().isEmpty()),
                () -> assertTrue(loaded.getEpics().isEmpty()),
                () -> assertTrue(loaded.getSubtasks().isEmpty()),
                () -> assertTrue(loaded.getHistory().isEmpty())
        );
    }

    @Test
    void loadFromFile_shouldThrowOnMalformedLine() throws IOException {
        Files.writeString(tempFile.toPath(), CSV_HEADER + "malformed,line");

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
    }

    @Test
    void loadFromFile_shouldThrowOnUnreadableFile() throws IOException {
        File unreadable = File.createTempFile("unreadable", ".csv");
        unreadable.setReadable(false);

        try {
            assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(unreadable));
        } finally {
            unreadable.delete();
        }
    }
}