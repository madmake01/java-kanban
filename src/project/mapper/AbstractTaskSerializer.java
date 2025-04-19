package project.mapper;

import project.enums.Status;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.AbstractTaskBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractTaskSerializer {

    private static final String FIELD_DELIMITER = ",";
    private static final Map<String, Function<String[], AbstractTask>> mapper = new HashMap<>();

    static {
        mapper.put(Epic.class.getSimpleName(), array -> {
            List<Integer> subtaskIds = extractSubtaskIds(array);

            LocalDateTime end = null;
            String endString = array[7];
            if (!endString.isEmpty()) {
                end = LocalDateTime.parse(endString);
            }

            Epic.Builder builder = getTaskBuilder(array, Epic.Builder::new)
                    .setSubtaskIds(subtaskIds)
                    .setEndTime(end);
            return builder.build();
        });

        mapper.put(Task.class.getSimpleName(), array ->
                getTaskBuilder(array, Task.Builder::new).build()
        );

        mapper.put(Subtask.class.getSimpleName(), array ->
                getTaskBuilder(array, Subtask.Builder::new)
                        .setEpicId(Integer.parseInt(array[array.length - 1]))
                        .build()
        );
    }

    private AbstractTaskSerializer() {
    }

    public static AbstractTask deserialize(String serialized) {
        String[] split = serialized.split(FIELD_DELIMITER, -1);
        Function<String[], AbstractTask> factory = mapper.get(split[1]);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown task type: " + split[1]);
        }
        return factory.apply(split);
    }

    public static String serialize(AbstractTask task) {
        List<String> fields = extractBasicFields(task);

        switch (task) {
            case Epic epic -> fields.addAll(epic.getSubtaskIds().stream()
                    .map(String::valueOf)
                    .toList());
            case Subtask subtask -> fields.add(String.valueOf(subtask.getEpicId()));
            case Task ignored -> fields.add("");
            default -> throw new IllegalArgumentException("Unknown task type: " + task);
        }

        return String.join(FIELD_DELIMITER, fields);
    }


    private static <T extends AbstractTask, B extends AbstractTaskBuilder<T, B>> B getTaskBuilder(
            String[] array,
            Supplier<B> builderSupplier
    ) {
        String startTimeString = array[5];

        LocalDateTime startTime = startTimeString.isEmpty() ? null : LocalDateTime.parse(startTimeString);
        String durationString = array[6];
        Duration duration = durationString.isEmpty() ? null : Duration.parse(durationString);
        return builderSupplier.get()
                .setId(Integer.parseInt(array[0]))
                .setName(array[2])
                .setDescription(array[3])
                .setStatus(Status.valueOf(array[4].toUpperCase()))
                .setStartTime(startTime)
                .setDuration(duration);
    }

    private static List<Integer> extractSubtaskIds(String[] array) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 8; i < array.length; i++) {
            ids.add(Integer.parseInt(array[i]));
        }
        return ids;
    }

    private static List<String> extractBasicFields(AbstractTask task) {
        List<String> fields = new ArrayList<>();

        fields.add(String.valueOf(task.getId()));
        fields.add(task.getClass().getSimpleName());
        fields.add(task.getName());
        fields.add(task.getDescription());
        fields.add(task.getStatus().toString());
        fields.add(task.getStartTime().map(LocalDateTime::toString).orElse(""));
        fields.add(task.getDuration().map(Duration::toString).orElse(""));
        fields.add(task.getEndTime().map(LocalDateTime::toString).orElse(""));

        return fields;
    }

}
