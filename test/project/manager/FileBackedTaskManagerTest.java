package project.manager;

import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.Managers;
import project.util.TaskFileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {

    @Test
    void saveAndLoadFromFileShouldPreserveAllEntities() throws IOException {

        File tempFile = File.createTempFile("test_tasks", ".csv");
        tempFile.deleteOnExit();
        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        FileBackedTaskManager manager = Managers.getDefaultFileBackedTaskManager(tempFile);

        Epic epicToAdd = new Epic.Builder()
                .setName("Epic")
                .setDescription("Epic description")
                .build();
        Epic savedEpic = manager.addEpic(epicToAdd);

        Subtask subtaskToAdd = new Subtask.Builder()
                .setName("Subtask")
                .setDescription("Subtask description")
                .setStatus(Status.IN_PROGRESS)
                .build();
        Subtask savedSubtask = manager.addSubtask(subtaskToAdd, savedEpic.getId());

        Task taskToAdd = new Task.Builder()
                .setName("Task")
                .setDescription("Task description")
                .setStatus(Status.DONE)
                .build();
        Task savedTask = manager.addTask(taskToAdd);

        FileBackedTaskManager loadedManager = Managers.loadFromFile(tempFile);

        List<Epic> loadedEpics = loadedManager.getEpics();
        assertEquals(1, loadedEpics.size(), "There should be exactly one epic loaded");
        Epic loadedEpic = loadedEpics.getFirst();
        assertEquals(savedEpic.getId(), loadedEpic.getId(), "Epic ID should match");
        assertEquals(savedEpic.getName(), loadedEpic.getName(), "Epic name should match");
        assertEquals(savedEpic.getDescription(), loadedEpic.getDescription(), "Epic description should match");
        assertEquals(savedSubtask.getStatus(), loadedEpic.getStatus(), "Epic status should match subtask status");

        List<Subtask> loadedSubtasks = loadedManager.getSubtasks();
        assertEquals(1, loadedSubtasks.size(), "There should be exactly one subtask loaded");
        Subtask loadedSubtask = loadedSubtasks.getFirst();
        assertEquals(savedSubtask.getId(), loadedSubtask.getId(), "Subtask ID should match");
        assertEquals(savedSubtask.getName(), loadedSubtask.getName(), "Subtask name should match");
        assertEquals(savedSubtask.getDescription(), loadedSubtask.getDescription(), "Subtask description should match");
        assertEquals(savedSubtask.getStatus(), loadedSubtask.getStatus(), "Subtask status should match");
        assertEquals(savedSubtask.getEpicId(), loadedSubtask.getEpicId(), "Subtask epic ID should match");

        List<Task> loadedTasks = loadedManager.getTasks();
        assertEquals(1, loadedTasks.size(), "There should be exactly one task loaded");
        Task loadedTask = loadedTasks.getFirst();
        assertEquals(savedTask.getId(), loadedTask.getId(), "Task ID should match");
        assertEquals(savedTask.getName(), loadedTask.getName(), "Task name should match");
        assertEquals(savedTask.getDescription(), loadedTask.getDescription(), "Task description should match");
        assertEquals(savedTask.getStatus(), loadedTask.getStatus(), "Task status should match");
    }

    @Test
    void loadFromEmptyFileShouldCreateEmptyManager() throws IOException {
        File tempFile = File.createTempFile("empty", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = Managers.loadFromFile(tempFile);

        assertNotNull(manager, "Manager should not be null");
        assertEquals(0, manager.getTasks().size(), "Tasks should be empty");
        assertEquals(0, manager.getEpics().size(), "Epics should be empty");
        assertEquals(0, manager.getSubtasks().size(), "Subtasks should be empty");
        assertEquals(0, manager.getHistory().size(), "History should be empty");
    }

    @Test
    void saveEmptyManagerShouldProduceNonCorruptedFile() throws IOException {
        File tempFile = File.createTempFile("empty_save", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = Managers.getDefaultFileBackedTaskManager(tempFile);
        manager.deleteSubtasks(); // method to force save

        assertTrue(tempFile.exists(), "File should exist");
        assertTrue(tempFile.length() > 0, "File should not be completely empty (should at least contain a header)");

        FileBackedTaskManager loadedManager = Managers.loadFromFile(tempFile);

        assertNotNull(loadedManager, "Loaded manager should not be null");
        assertEquals(0, loadedManager.getTasks().size(), "Tasks should be empty");
        assertEquals(0, loadedManager.getEpics().size(), "Epics should be empty");
        assertEquals(0, loadedManager.getSubtasks().size(), "Subtasks should be empty");
        assertEquals(0, loadedManager.getHistory().size(), "History should be empty");

        try (Stream<String> lines = Files.lines(tempFile.toPath())) {
            List<String> list = lines.toList();
            assertEquals(1, list.size(), "File should contain only one line (the header)");
            assertEquals(TaskFileRepository.CSV_HEADER, list.getFirst(), "File header should match the expected CSV header");
        }
    }
}
