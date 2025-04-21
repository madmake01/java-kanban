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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AbstractTaskSerializer {

    private static final String FIELD_DELIMITER = ",";
    private static final Map<String, Function<String[], AbstractTask>> TYPE_DESERIALIZERS = Map.of(
            Epic.class.getSimpleName(), AbstractTaskSerializer::deserializeEpic,
            Task.class.getSimpleName(), AbstractTaskSerializer::deserializeTask,
            Subtask.class.getSimpleName(), AbstractTaskSerializer::deserializeSubtask
    );

    private AbstractTaskSerializer() {
    }

    public static AbstractTask deserialize(String serialized) {
        String[] fields = serialized.split(FIELD_DELIMITER, -1);
        String type = fields[BaseField.TYPE.idx()];
        return Optional.ofNullable(TYPE_DESERIALIZERS.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Unknown task type: " + type))
                .apply(fields);
    }

    public static String serialize(AbstractTask task) {
        List<String> fields = new ArrayList<>(extractBasicFields(task));

        switch (task) {
            case Epic epic -> epic.getSubtaskIds().forEach(id -> fields.add(String.valueOf(id)));
            case Subtask subtask -> fields.add(String.valueOf(subtask.getEpicId()));
            case Task ignored -> fields.add("");
            default -> throw new IllegalArgumentException("Unknown task type: " + task);
        }

        return String.join(FIELD_DELIMITER, fields);
    }

    private static Task deserializeTask(String[] array) {
        return buildTask(array, Task.Builder::new).build();
    }

    private static Epic deserializeEpic(String[] array) {
        Epic.Builder builder = buildTask(array, Epic.Builder::new)
                .setSubtaskIds(extractSubtaskIds(array));

        String endTime = array[BaseField.END_TIME.idx()];
        if (!endTime.isEmpty()) {
            builder.setEndTime(LocalDateTime.parse(endTime));
        }

        return builder.build();
    }

    private static Subtask deserializeSubtask(String[] array) {
        return buildTask(array, Subtask.Builder::new)
                .setEpicId(Integer.parseInt(array[BaseField.RELATED_IDS.idx()]))
                .build();
    }

    private static <T extends AbstractTask, B extends AbstractTaskBuilder<T, B>> B buildTask(
            String[] array,
            Supplier<B> builderSupplier
    ) {
        LocalDateTime startTime = parseDateTime(array[BaseField.START_TIME.idx()]);
        Duration duration = parseDuration(array[BaseField.DURATION.idx()]);

        return builderSupplier.get()
                .setId(Integer.parseInt(array[BaseField.ID.idx()]))
                .setName(array[BaseField.NAME.idx()])
                .setDescription(array[BaseField.DESCRIPTION.idx()])
                .setStatus(Status.valueOf(array[BaseField.STATUS.idx()].toUpperCase()))
                .setStartTime(startTime)
                .setDuration(duration);
    }

    private static List<String> extractBasicFields(AbstractTask task) {
        return List.of(
                String.valueOf(task.getId()),
                task.getClass().getSimpleName(),
                task.getName(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getStartTime().map(LocalDateTime::toString).orElse(""),
                task.getDuration().map(Duration::toString).orElse(""),
                task.getEndTime().map(LocalDateTime::toString).orElse("")
        );
    }

    private static List<Integer> extractSubtaskIds(String[] array) {
        List<Integer> ids = new ArrayList<>();
        for (int i = BaseField.RELATED_IDS.idx(); i < array.length; i++) {
            ids.add(Integer.parseInt(array[i]));
        }
        return ids;
    }

    private static LocalDateTime parseDateTime(String value) {
        return value.isEmpty() ? null : LocalDateTime.parse(value);
    }

    private static Duration parseDuration(String value) {
        return value.isEmpty() ? null : Duration.parse(value);
    }

    public enum BaseField {
        ID(0),
        TYPE(1),
        NAME(2),
        DESCRIPTION(3),
        STATUS(4),
        START_TIME(5),
        DURATION(6),
        END_TIME(7),
        RELATED_IDS(8);

        private final int position;

        BaseField(int position) {
            this.position = position;
        }

        public int idx() {
            return position;
        }
    }
}
