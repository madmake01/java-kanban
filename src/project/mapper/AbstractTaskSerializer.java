package project.mapper;

import project.enums.Status;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.AbstractTaskBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractTaskSerializer {

    public static final int TASK_TYPE = 0;
    public static final int ID_INDEX = 1;
    public static final int NAME_INDEX = 2;
    public static final int DESCRIPTION_INDEX = 3;
    public static final int STATUS_INDEX = 4;
    public static final int ADDITIONAL_INFO = 5;

    private static final String FIELD_DELIMITER = ",";

    private static final Map<String, Function<String[], AbstractTask>> mapper;


    static {
        mapper = new HashMap<>();

        mapper.put(Epic.class.getSimpleName(), (array -> {
            List<Integer> subtaskId = extractSubtaskId(array);
            Epic.Builder builder = getTaskBuilder(array, Epic.Builder::new);
            return builder.setSubtaskIds(subtaskId)
                    .build();
        }));

        mapper.put(Task.class.getSimpleName(), (array -> {
            Task.Builder builder = getTaskBuilder(array, Task.Builder::new);
            return builder.build();
        }));

        mapper.put(Subtask.class.getSimpleName(), (array -> {
            Subtask.Builder builder = getTaskBuilder(array, Subtask.Builder::new);
            int epicId = Integer.parseInt(array[ADDITIONAL_INFO]);
            return builder.setEpicId(epicId)
                    .build();

        }));
    }

    private AbstractTaskSerializer() {
    }

    public static AbstractTask deserialize(String serialized) {
        String[] split = serialized.split(FIELD_DELIMITER);
        Function<String[], AbstractTask> taskFactory = mapper.get(split[TASK_TYPE]);

        if (taskFactory == null) {
            throw new IllegalArgumentException("Unknown task type: " + split[TASK_TYPE]);
        }
        return taskFactory.apply(split);
    }

    public static String serialize(AbstractTask task) {
        List<String> fields = extractBasicFields(task);

        if (task instanceof Epic epic) {
            fields.addAll(epic.getSubtaskIds().stream()
                    .map(String::valueOf)
                    .toList());
        } else if (task instanceof Subtask subtask) {
            fields.add(String.valueOf(subtask.getEpicId()));
        }

        return String.join(FIELD_DELIMITER, fields);
    }


    public static <T extends AbstractTask, B extends AbstractTaskBuilder<T, B>> B getTaskBuilder(
            String[] array,
            Supplier<B> builderSupplier
    ) {
        return builderSupplier.get()
                .setId(Integer.parseInt(array[ID_INDEX]))
                .setName(array[NAME_INDEX])
                .setDescription(array[DESCRIPTION_INDEX])
                .setStatus(Status.valueOf(array[STATUS_INDEX].toUpperCase()));
    }


    private static List<Integer> extractSubtaskId(String[] array) {
        List<Integer> subtasksId = new ArrayList<>();
        for (int i = ADDITIONAL_INFO; i < array.length; i++) {
            subtasksId.add(Integer.parseInt(array[i]));
        }
        return subtasksId;
    }


    private static List<String> extractBasicFields(AbstractTask task) {
        List<String> fields = new ArrayList<>();

        fields.add(TASK_TYPE, task.getClass().getSimpleName());
        fields.add(ID_INDEX, String.valueOf(task.getId()));
        fields.add(NAME_INDEX, task.getName());
        fields.add(DESCRIPTION_INDEX, task.getDescription());
        fields.add(STATUS_INDEX, task.getStatus().toString());

        return fields;
    }

}
