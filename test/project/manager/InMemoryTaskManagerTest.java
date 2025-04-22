package project.manager;

import org.junit.jupiter.api.BeforeEach;
import project.util.TaskValidator;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void setup() {
        taskManager = new InMemoryTaskManager(new TaskValidator(), new InMemoryHistoryManager());
    }
}
