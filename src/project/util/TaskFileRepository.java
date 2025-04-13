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
import static project.exception.TaskExceptionMessage.FILE_SHOULD_START_WITH;

public class TaskFileRepository {
    public static final String CSV_HEADER = "id,type,name,status,description,additional";

    private TaskFileRepository() {
    }

    public static void saveTasks(File file, List<List<AbstractTask>> taskList) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write(CSV_HEADER);

            for (List<AbstractTask> list : taskList) {
                for (AbstractTask task : list) {
                    String serializedTask = AbstractTaskSerializer.serialize(task);
                    writer.newLine();
                    writer.write(serializedTask);
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException(ERROR_SAVING_DATA, e);
        }
    }

    public static List<AbstractTask> getTasks(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<AbstractTask> list = new ArrayList<>();

            String line = reader.readLine();

            if (line != null && line.trim().equals(CSV_HEADER)) {
                line = reader.readLine();
            } else {
                throw new ManagerSaveException(FILE_SHOULD_START_WITH + CSV_HEADER);
            }

            while (line != null && !line.isEmpty()) {
                list.add(AbstractTaskSerializer.deserialize(line));
                line = reader.readLine();
            }

            return list;
        } catch (IOException e) {
            throw new ManagerSaveException(ERROR_READING_FILE, e);
        }
    }
}
