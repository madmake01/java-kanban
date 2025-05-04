package project.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import project.enums.Status;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;
import project.model.Epic;
import project.model.Subtask;
import project.model.Task;
import project.util.Managers;
import project.util.TaskUtility;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskManagerTasksTest {
    public static final int NONEXISTENT_ID = Integer.MIN_VALUE;
    private static TaskManager manager;
    private static HttpTaskServer taskServer;
    private static Gson gson;
    private static HttpClient client;
    HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

    @BeforeAll
    static void beforeAll() throws IOException {
        manager = Managers.getDefaultTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void afterAll() {
        taskServer.stop();
        client.close();
    }

    @BeforeEach
    void beforeEach() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
    }

    // ===== Task: GET All =====

    @Test
    void shouldReturnAddedTaskWhenGettingAllTasks() throws IOException, InterruptedException {
        Task task = TaskUtility.createTask();
        Task addedTask = manager.addTask(task);
        String expectedJson = gson.toJson(addedTask);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("tasks"))
                .build();

        HttpResponse<String> response = client.send(request, handler);
        String responseBody = response.body();

        assertEquals(200, response.statusCode());
        assertTrue(responseBody.contains(expectedJson));
    }

    @Test
    void shouldReturnEmptyListAfterClearingTasksViaManager() throws IOException, InterruptedException {
        manager.deleteTasks();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("tasks"))
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, handler);
        String responseBody = getResponse.body();

        assertEquals(200, getResponse.statusCode());
        assertEquals("[]", responseBody);
    }

    // ===== Epic: GET All =====

    @Test
    void shouldReturnAddedEpicWhenGettingAllEpics() throws IOException, InterruptedException {
        Epic epic = TaskUtility.createEpic();
        Epic addedEpic = manager.addEpic(epic);
        String expectedJson = gson.toJson(addedEpic);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("epics"))
                .build();

        HttpResponse<String> response = client.send(request, handler);
        String responseBody = response.body();

        assertEquals(200, response.statusCode());
        assertTrue(responseBody.contains(expectedJson));
    }

    @Test
    void shouldReturnEmptyEpicListAfterClearingViaManager() throws IOException, InterruptedException {
        manager.deleteEpics();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("epics"))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    // ===== Subtask: GET All =====

    @Test
    void shouldReturnAddedSubtaskWhenGettingAllSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();
        Subtask subtask = manager.addSubtask(TaskUtility.createSubtask(), epicId);
        String expectedJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("subtasks"))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(expectedJson));
    }

    @Test
    void shouldReturnEmptySubtaskListAfterClearingViaManager() throws IOException, InterruptedException {
        manager.deleteSubtasks();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("subtasks"))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }


    // ===== Task: GET by ID =====

    @ParameterizedTest
    @CsvSource({
            "tasks",
            "epics",
            "subtasks"
    })
    void shouldReturn404WhenGettingNonexistentEntityById(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri(endpoint, String.valueOf(NONEXISTENT_ID)))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnTaskByIdAfterSavingViaManager() throws IOException, InterruptedException {
        Task task = TaskUtility.createTask();
        Task addedTask = manager.addTask(task);
        String expectedJson = gson.toJson(addedTask);
        int taskId = addedTask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("tasks", String.valueOf(taskId)))
                .build();

        HttpResponse<String> response = client.send(request, handler);
        String responseBody = response.body();

        assertEquals(200, response.statusCode());
        assertEquals(expectedJson, responseBody);
    }


    // ===== Epic: GET by ID =====

    @Test
    void shouldReturnEpicByIdAfterSavingViaManager() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        int id = epic.getId();
        String expectedJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("epics", String.valueOf(id)))
                .build();

        HttpResponse<String> response = client.send(request, handler);
        String body = response.body();

        assertEquals(200, response.statusCode());
        assertEquals(expectedJson, body);
    }


    // ===== Subtask: GET by ID =====

    @Test
    void shouldReturnSubtaskByIdAfterSavingViaManager() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        int epicId = epic.getId();
        var subtask = manager.addSubtask(TaskUtility.createSubtask(), epicId);
        int id = subtask.getId();
        String expectedJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("subtasks", String.valueOf(id)))
                .build();

        HttpResponse<String> response = client.send(request, handler);
        String body = response.body();

        assertEquals(200, response.statusCode());
        assertEquals(expectedJson, body);
    }

    // ===== Task: POST create/update =====

    @Test
    void shouldCreateTaskAndMatchItWithManagerById() throws IOException, InterruptedException {
        Task originalTask = TaskUtility.createTaskBuilder()
                .setDuration(Duration.ofMinutes(1))
                .setStartTime(LocalDateTime.now())
                .setStatus(Status.DONE)
                .build();
        String json = gson.toJson(originalTask);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(buildUri("tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, handler);
        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int id = createdTask.getId();
        Task taskFromManager = manager.getTaskWithNotification(id);

        assertEquals(201, postResponse.statusCode());
        TaskUtility.assertAbstractTaskEquals(createdTask, taskFromManager);
    }

    @Test
    void shouldUpdateTaskAndReturnUpdatedVersion() throws IOException, InterruptedException {
        Task originalTask = manager.addTask(TaskUtility.createTask());
        int taskId = originalTask.getId();

        Task updated = new Task.Builder()
                .setId(taskId)
                .setName("Updated task")
                .setDescription("Updated desc")
                .setStatus(Status.IN_PROGRESS)
                .setStartTime(LocalDateTime.now().minusHours(100))
                .setDuration(Duration.ofHours(5))
                .build();
        String updatedJson = gson.toJson(updated);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(buildUri("tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, handler);
        Task updatedFromResponse = gson.fromJson(updateResponse.body(), Task.class);

        assertEquals(201, updateResponse.statusCode());
        TaskUtility.assertAbstractTaskEquals(updated, updatedFromResponse);
    }

    @Test
    void shouldReturn404WhenUpdatingNonexistentTask() throws IOException, InterruptedException {
        Task updated = TaskUtility.createTaskBuilder()
                .setId(NONEXISTENT_ID)
                .build();

        String json = gson.toJson(updated);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturn406WhenCreatingTaskWithConflictingStartTime() throws IOException, InterruptedException {
        LocalDateTime conflictTime = LocalDateTime.now().plusDays(1);
        Task firstTask = TaskUtility.createTaskBuilder()
                .setStartTime(conflictTime)
                .setDuration(Duration.ofMinutes(30))
                .build();
        Task conflictingTask = TaskUtility.createTaskBuilder()
                .setStartTime(conflictTime)
                .setDuration(Duration.ofMinutes(30))
                .build();
        HttpRequest firstRequest = HttpRequest.newBuilder()
                .uri(buildUri("tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(firstTask)))
                .build();
        client.send(firstRequest, handler);
        HttpRequest conflictRequest = HttpRequest.newBuilder()
                .uri(buildUri("tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(conflictingTask)))
                .build();

        HttpResponse<String> conflictResponse = client.send(conflictRequest, handler);

        assertEquals(406, conflictResponse.statusCode());
    }


    // ===== Epic: POST create/update (validation) =====
    @Test
    void shouldCreateEpicSuccessfully() throws IOException, InterruptedException {
        Epic epic = TaskUtility.createEpic(); // статус NEW, без сабтасок
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        Epic created = gson.fromJson(response.body(), Epic.class);
        Epic fromManager = manager.getEpicWithNotification(created.getId());
        assertEquals(201, response.statusCode());
        TaskUtility.assertAbstractTaskEquals(created, fromManager);
    }

    @Test
    void shouldRejectEpicWithNonNewStatus() throws IOException, InterruptedException {
        Epic epic = TaskUtility.createEpicBuilder().setStatus(Status.DONE).build();
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldRejectEpicWithSubtasksSet() throws IOException, InterruptedException {
        Epic epic = TaskUtility.createEpicBuilder().setSubtaskIds(List.of(1, 2)).build();
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(400, response.statusCode());
    }

    // ===== Subtask: POST create/update (validation) =====

    @Test
    void shouldCreateSubtaskSuccessfully() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        Subtask subtask = TaskUtility.createSubtaskBuilder()
                .setEpicId(epic.getId())
                .build();
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);
        Subtask created = gson.fromJson(response.body(), Subtask.class);
        Subtask fromManager = manager.getSubtaskWithNotification(created.getId());

        assertEquals(201, response.statusCode());
        TaskUtility.assertAbstractTaskEquals(created, fromManager);
    }

    @Test
    void shouldReturn404WhenCreatingSubtaskWithNonexistentEpic() throws IOException, InterruptedException {
        Subtask subtask = TaskUtility.createSubtaskBuilder()
                .setEpicId(NONEXISTENT_ID)
                .build();
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode());
    }


    // ===== Task: DELETE by ID =====
    @ParameterizedTest
    @CsvSource({
            "tasks",
            "epics",
            "subtasks"
    })
    void shouldReturn404WhenDeletingNonexistentEntity(String endpoint) throws IOException, InterruptedException {
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri(endpoint, String.valueOf(NONEXISTENT_ID)))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldDeleteExistingTaskById() throws IOException, InterruptedException {
        Task task = manager.addTask(TaskUtility.createTask());
        int id = task.getId();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri("tasks", String.valueOf(id)))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(200, response.statusCode());
        assertThrows(NonexistentEntityException.class, () -> manager.getTaskWithNotification(id));
    }


    // ===== Epic: DELETE by ID =====
    @Test
    void shouldDeleteExistingEpicById() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        int id = epic.getId();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri("epics", String.valueOf(id)))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(200, response.statusCode());
        assertThrows(NonexistentEntityException.class, () -> manager.getEpicWithNotification(id));
    }


    // ===== Subtask: DELETE by ID =====

    @Test
    void shouldDeleteExistingSubtaskById() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        Subtask subtask = manager.addSubtask(TaskUtility.createSubtask(), epic.getId());
        int id = subtask.getId();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri("subtasks", String.valueOf(id)))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(200, response.statusCode());
        assertThrows(NonexistentEntityException.class, () -> manager.getSubtaskWithNotification(id));
    }


    // ===== Task: DELETE ALL =====
    @Test
    void shouldDeleteTaskAndManagerShouldBeEmpty() throws IOException, InterruptedException {
        manager.addTask(TaskUtility.createTask());
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri("tasks"))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    // ===== Epic: DELETE ALL =====
    @Test
    void shouldDeleteAllEpicsAndManagerShouldBeEmpty() throws IOException, InterruptedException {
        manager.addEpic(TaskUtility.createEpic());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri("epics"))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
    }

    // ===== Subtask: DELETE ALL =====
    @Test
    void shouldDeleteAllSubtasksAndManagerShouldBeEmpty() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        manager.addSubtask(TaskUtility.createSubtask(), epic.getId());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(buildUri("subtasks"))
                .build();

        HttpResponse<String> response = client.send(deleteRequest, handler);

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    // ===== Epic: GET subtasks by epic ID =====

    @Test
    void shouldReturnSubtasksForExistingEpic() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        Subtask sub1 = new Subtask.Builder()
                .setName("Subtask A")
                .setDescription("Description A")
                .setEpicId(epic.getId())
                .setStatus(Status.NEW)
                .setStartTime(LocalDateTime.now().plusHours(1))
                .setDuration(Duration.ofMinutes(30))
                .build();
        Subtask sub2 = new Subtask.Builder()
                .setName("Subtask B")
                .setDescription("Description B")
                .setEpicId(epic.getId())
                .setStatus(Status.DONE)
                .setStartTime(LocalDateTime.now().plusHours(2))
                .setDuration(Duration.ofMinutes(45))
                .build();
        Subtask added1 = manager.addSubtask(sub1, epic.getId());
        Subtask added2 = manager.addSubtask(sub2, epic.getId());
        String expectedJson1 = gson.toJson(added1);
        String expectedJson2 = gson.toJson(added2);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("epics", String.valueOf(epic.getId()), "subtasks"))
                .build();


        HttpResponse<String> response = client.send(request, handler);
        String body = response.body();


        assertEquals(200, response.statusCode());
        assertTrue(body.contains(expectedJson1));
        assertTrue(body.contains(expectedJson2));
    }


    @Test
    void shouldReturn404WhenGettingSubtasksForNonexistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("epics", String.valueOf(NONEXISTENT_ID), "subtasks"))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode());
    }

// ===== History: GET =====

    @Test
    void shouldReturnAllEntityTypesInHistoryAfterAccessingById() throws IOException, InterruptedException {
        Task task = manager.addTask(TaskUtility.createTask());
        Epic epic = manager.addEpic(TaskUtility.createEpic());
        Subtask subtask = manager.addSubtask(
                new Subtask.Builder()
                        .setName("Sub 1")
                        .setDescription("Sub description")
                        .setEpicId(epic.getId())
                        .build(),
                epic.getId()
        );
        epic = manager.getEpicWithNotification(epic.getId());

        HttpRequest getTask = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("tasks", String.valueOf(task.getId())))
                .build();

        HttpRequest getEpic = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("epics", String.valueOf(epic.getId())))
                .build();

        HttpRequest getSubtask = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("subtasks", String.valueOf(subtask.getId())))
                .build();

        client.send(getTask, handler);
        client.send(getEpic, handler);
        client.send(getSubtask, handler);

        HttpRequest getHistory = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("history"))
                .build();


        HttpResponse<String> response = client.send(getHistory, handler);
        String body = response.body();


        assertEquals(200, response.statusCode());
        assertTrue(body.contains(gson.toJson(task)));
        assertTrue(body.contains(gson.toJson(epic)));
        assertTrue(body.contains(gson.toJson(subtask)));
    }


// ===== Task: GET /prioritized =====

    @Test
    void shouldReturnTasksSortedByStartTime() throws IOException, InterruptedException {
        Task early = new Task.Builder()
                .setName("Early task")
                .setDescription("First")
                .setStartTime(LocalDateTime.now().plusHours(1))
                .setDuration(Duration.ofMinutes(30))
                .build();

        Task late = new Task.Builder()
                .setName("Late task")
                .setDescription("Second")
                .setStartTime(LocalDateTime.now().plusHours(2))
                .setDuration(Duration.ofMinutes(45))
                .build();

        Task t1 = manager.addTask(early);
        Task t2 = manager.addTask(late);

        HttpRequest getPrioritized = HttpRequest.newBuilder()
                .GET()
                .uri(buildUri("prioritized"))
                .build();


        HttpResponse<String> response = client.send(getPrioritized, handler);
        String body = response.body();


        assertEquals(200, response.statusCode());

        int firstIndex = body.indexOf(gson.toJson(t1));
        int secondIndex = body.indexOf(gson.toJson(t2));

        assertTrue(firstIndex >= 0);
        assertTrue(secondIndex >= 0);
        assertTrue(firstIndex < secondIndex);
    }


    private static URI buildUri(String... pathSegments) {
        String path = String.join("/", pathSegments);
        try {
            return new URI("http://localhost:8080/" + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}