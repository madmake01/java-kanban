package project.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import project.controller.EpicsController;
import project.controller.TasksController;
import project.manager.InMemoryHistoryManager;
import project.manager.InMemoryTaskManager;
import project.manager.TaskManager;
import project.model.Epic;
import project.util.DurationAdapter;
import project.util.EpicDeserializer;
import project.util.LocalDateTimeAdapter;
import project.util.Managers;
import project.util.TaskValidator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer(HttpServer server, TaskManager taskManager) {
        this.httpServer = server;
        this.taskManager = taskManager;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Epic.class, new EpicDeserializer())
                .create();

        TaskManager defaultTaskManager = Managers.getDefaultTaskManager();
        server.createContext("/tasks", new TasksController(defaultTaskManager, gson));
        server.createContext("/epics", new EpicsController(defaultTaskManager, gson));
        server.createContext("/subtasks", new EpicsController(defaultTaskManager, gson));
        HttpTaskServer httpTaskServer = new HttpTaskServer(server, new InMemoryTaskManager(new TaskValidator(),
                new InMemoryHistoryManager()));
        httpTaskServer.start();
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
