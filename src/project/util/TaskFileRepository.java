package project.util;

import project.exception.ManagerSaveException;
import project.mapper.AbstractTaskSerializer;
import project.model.AbstractTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static project.exception.TaskExceptionMessage.ERROR_READING_FILE;
import static project.exception.TaskExceptionMessage.ERROR_SAVING_DATA;

public class TaskFileRepository {

    private TaskFileRepository() {
    }

    public static void saveTasks(File file, List<List<AbstractTask>> taskList) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            for (List<AbstractTask> list : taskList) {
                for (AbstractTask task : list) {
                    String serializedTask = AbstractTaskSerializer.serialize(task);
                    writer.write(serializedTask);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException(ERROR_SAVING_DATA, e);
        }
    }

    public static List<AbstractTask> getTasks(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<AbstractTask> list = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                list.add(AbstractTaskSerializer.deserialize(line));
            }

            return list;
        } catch (IOException e) {
            throw new ManagerSaveException(ERROR_READING_FILE, e);
        }
    }
}
