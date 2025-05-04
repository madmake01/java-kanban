package project.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import project.controller.EpicsController;
import project.controller.HistoryController;
import project.controller.PrioritizedTasksController;
import project.controller.SubtasksController;
import project.controller.TasksController;
import project.filter.ExceptionHandler;
import project.manager.TaskManager;
import project.model.Epic;
import project.util.Managers;
import project.util.gson.DurationAdapter;
import project.util.gson.EpicDeserializer;
import project.util.gson.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServer {

    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(HttpServer server, TaskManager taskManager) {
        this.server = server;
        this.taskManager = taskManager;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpTaskServer httpTaskServer = new HttpTaskServer(server, Managers.getDefaultTaskManager());
        httpTaskServer.start();
    }

    public void start() {
        Gson gson = createGson();

        List<HttpContext> contexts = List.of(
                server.createContext("/tasks", new TasksController(taskManager, gson)),
                server.createContext("/epics", new EpicsController(taskManager, gson)),
                server.createContext("/subtasks", new SubtasksController(taskManager, gson)),
                server.createContext("/history", new HistoryController(taskManager, gson)),
                server.createContext("/prioritized", new PrioritizedTasksController(taskManager, gson))
        );

        Filter commonFilter = new ExceptionHandler();

        for (HttpContext context : contexts) {
            context.getFilters().add(commonFilter);
        }

        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Epic.class, new EpicDeserializer())
                .create();
    }
}
