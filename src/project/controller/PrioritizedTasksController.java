package project.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;

import java.io.IOException;
import java.util.Arrays;

public class PrioritizedTasksController extends BaseHttpHandler {
    public PrioritizedTasksController(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    public PrioritizedTasksController(Gson gson) {
        super(gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        String[] pathParts = Arrays.stream(requestPath.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (pathParts.length == 1 && requestMethod.equals("GET")) {
            sendText(exchange, getPrioritizedTasks());
            return;
        }
        throw new NonexistentEntityException("Endpoint not found: " + requestMethod + " " + requestPath);
    }

    private String getPrioritizedTasks() {
        return gson.toJson(taskManager.getPrioritizedTasks());
    }
}
