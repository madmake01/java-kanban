package project.mapper;

import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AbstractTaskSerializerTest {

    @Test
    void serializeTask() {
        int id = 123;
        String name = "test";
        String description = "description";
        Status status = Status.IN_PROGRESS;

        Task task = new Task.Builder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .build();

        String serialized = AbstractTaskSerializer.serialize(task);

        String expected = String.format("%d,Task,%s,%s,%s,,,,", id, name, description, status);
        assertEquals(expected, serialized);
    }

    @Test
    void deserializeTask() {
        int id = 123;
        String name = "test";
        String description = "description";
        Status status = Status.IN_PROGRESS;

        String serialized = String.format("%d,Task,%s,%s,%s,,,,", id, name, description, status);

        AbstractTask deserialized = AbstractTaskSerializer.deserialize(serialized);

        assertInstanceOf(Task.class, deserialized);
        Task taskDeserialized = (Task) deserialized;

        assertEquals(id, taskDeserialized.getId());
        assertEquals(name, taskDeserialized.getName());
        assertEquals(description, taskDeserialized.getDescription());
        assertEquals(status, taskDeserialized.getStatus());
    }

    @Test
    void serializeEpic() {
        int id = 1;
        String name = "epic name";
        String description = "epic description";
        Status status = Status.NEW;
        List<Integer> subtaskIds = List.of(10, 20, 30);

        Epic epic = new Epic.Builder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .setSubtaskIds(subtaskIds)
                .build();

        String serialized = AbstractTaskSerializer.serialize(epic);
        String expected = String.format("%d,Epic,%s,%s,%s,,,,10,20,30", id, name, description, status);

        assertEquals(expected, serialized);
    }

    @Test
    void deserializeEpic() {
        String serialized = "1,Epic,epic name,epic description,NEW,,,,10,20,30";

        AbstractTask deserialized = AbstractTaskSerializer.deserialize(serialized);
        assertInstanceOf(Epic.class, deserialized);

        Epic epic = (Epic) deserialized;
        assertEquals(1, epic.getId());
        assertEquals("epic name", epic.getName());
        assertEquals("epic description", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertEquals(List.of(10, 20, 30), epic.getSubtaskIds());
    }

    @Test
    void serializeSubtask() {
        int id = 2;
        String name = "subtask name";
        String description = "subtask description";
        Status status = Status.DONE;
        int epicId = 1;

        Subtask subtask = new Subtask.Builder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .setEpicId(epicId)
                .build();

        String serialized = AbstractTaskSerializer.serialize(subtask);
        String expected = String.format("%d,Subtask,%s,%s,%s,,,,%d", id, name, description, status, epicId);

        assertEquals(expected, serialized);
    }

    @Test
    void deserializeSubtask() {
        String serialized = "2,Subtask,subtask name,subtask description,DONE,,,,,1";

        AbstractTask deserialized = AbstractTaskSerializer.deserialize(serialized);
        assertInstanceOf(Subtask.class, deserialized);

        Subtask subtask = (Subtask) deserialized;
        assertEquals(2, subtask.getId());
        assertEquals("subtask name", subtask.getName());
        assertEquals("subtask description", subtask.getDescription());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals(1, subtask.getEpicId());
    }

    @Test
    void taskShouldPreserveFieldsAfterSerializationAndDeserialization() {
        int id = 123;
        String name = "test";
        String description = "description";
        Status status = Status.IN_PROGRESS;
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(1);
        Task task = new Task.Builder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .setStartTime(startTime)
                .setDuration(duration)
                .build();

        String serialized = AbstractTaskSerializer.serialize(task);
        AbstractTask deserializedAbstract = AbstractTaskSerializer.deserialize(serialized);

        assertInstanceOf(Task.class, deserializedAbstract);
        Task deserializedTask = (Task) deserializedAbstract;

        assertEquals(id, deserializedTask.getId());
        assertEquals(name, deserializedTask.getName());
        assertEquals(description, deserializedTask.getDescription());
        assertEquals(status, deserializedTask.getStatus());
        assertEquals(startTime, deserializedTask.getStartTime().get());
        assertEquals(duration, deserializedTask.getDuration().get());
        assertEquals(task.getEndTime(), deserializedTask.getEndTime());
    }

    @Test
    void subtaskShouldPreserveFieldsAfterSerializationAndDeserialization() {
        int id = 456;
        String name = "subtask-test";
        String description = "subtask description";
        Status status = Status.DONE;
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);
        Integer epicId = 123;

        Subtask subtask = new Subtask.Builder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .setStartTime(startTime)
                .setDuration(duration)
                .setEpicId(epicId)
                .build();

        String serialized = AbstractTaskSerializer.serialize(subtask);
        AbstractTask deserializedAbstract = AbstractTaskSerializer.deserialize(serialized);

        assertInstanceOf(Subtask.class, deserializedAbstract);
        Subtask deserializedSubtask = (Subtask) deserializedAbstract;

        assertEquals(id, deserializedSubtask.getId());
        assertEquals(name, deserializedSubtask.getName());
        assertEquals(description, deserializedSubtask.getDescription());
        assertEquals(status, deserializedSubtask.getStatus());
        assertEquals(startTime, deserializedSubtask.getStartTime().get());
        assertEquals(duration, deserializedSubtask.getDuration().get());
        assertEquals(epicId, deserializedSubtask.getEpicId());
        assertEquals(subtask.getEndTime(), deserializedSubtask.getEndTime());
    }

    @Test
    void epicShouldPreserveFieldsAfterSerializationAndDeserialization() {
        int id = 789;
        String name = "epic-test";
        String description = "epic description";
        Status status = Status.NEW;
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(5);
        List<Integer> subtaskIds = List.of(456, 457, 458);

        Epic epic = new Epic.Builder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setStatus(status)
                .setStartTime(startTime)
                .setDuration(duration)
                .setSubtaskIds(subtaskIds)
                .build();

        String serialized = AbstractTaskSerializer.serialize(epic);
        AbstractTask deserializedAbstract = AbstractTaskSerializer.deserialize(serialized);

        assertInstanceOf(Epic.class, deserializedAbstract);
        Epic deserializedEpic = (Epic) deserializedAbstract;

        assertEquals(id, deserializedEpic.getId());
        assertEquals(name, deserializedEpic.getName());
        assertEquals(description, deserializedEpic.getDescription());
        assertEquals(status, deserializedEpic.getStatus());
        assertEquals(startTime, deserializedEpic.getStartTime().get());
        assertEquals(duration, deserializedEpic.getDuration().get());
        assertEquals(subtaskIds, deserializedEpic.getSubtaskIds());
        assertEquals(epic.getEndTime(), deserializedEpic.getEndTime());
    }

}