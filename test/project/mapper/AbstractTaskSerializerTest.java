package project.mapper;

import org.junit.jupiter.api.Test;
import project.enums.Status;
import project.model.AbstractTask;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;

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

        String expected = String.format("%d,Task,%s,%s,%s", id, name, description, status);
        assertEquals(expected, serialized);
    }

    @Test
    void deserializeTask() {
        int id = 123;
        String name = "test";
        String description = "description";
        Status status = Status.IN_PROGRESS;

        String serialized = String.format("%d,Task,%s,%s,%s", id, name, description, status);

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
        String expected = String.format("%d,Epic,%s,%s,%s,10,20,30", id, name, description, status);

        assertEquals(expected, serialized);
    }

    @Test
    void deserializeEpic() {
        String serialized = "1,Epic,epic name,epic description,NEW,10,20,30";

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
        String expected = String.format("%d,Subtask,%s,%s,%s,%d", id, name, description, status, epicId);

        assertEquals(expected, serialized);
    }

    @Test
    void deserializeSubtask() {
        String serialized = "2,Subtask,subtask name,subtask description,DONE,1";

        AbstractTask deserialized = AbstractTaskSerializer.deserialize(serialized);
        assertInstanceOf(Subtask.class, deserialized);

        Subtask subtask = (Subtask) deserialized;
        assertEquals(2, subtask.getId());
        assertEquals("subtask name", subtask.getName());
        assertEquals("subtask description", subtask.getDescription());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals(1, subtask.getEpicId());
    }

}