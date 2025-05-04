package project.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import project.enums.Endpoint;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;
import project.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TasksController extends BaseHttpHandler {

    public TasksController(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    public TasksController(Gson gson) {
        super(gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        String[] pathParts = Arrays.stream(requestPath.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        Endpoint endpoint = getTaskEndpoint(pathParts, requestMethod);

        switch (endpoint) {
            case GET_ALL -> sendText(exchange, getTasks());
            case GET -> sendText(exchange, getTask(Integer.parseInt(pathParts[1])));
            case POST -> {
                handlePost(exchange);
                sendEmptyResponseWithCode(exchange, 201);
            }
            case DELETE_ALL -> {
                deleteTasks();
                sendEmptyResponseWithCode(exchange, 200);
            }
            case DELETE -> {
                deleteTask(Integer.parseInt(pathParts[1]));
                sendEmptyResponseWithCode(exchange, 200);
            }
            default -> throw new NonexistentEntityException("Endpoint not found: " + requestMethod + " " + requestPath);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);
        if (task.getId() == 0) {
            createTask(task);
        } else {
            updateTask(task);
        }
    }

    private String getTasks() {
        return gson.toJson(taskManager.getTasks());
    }

    private String getTask(int id) {
        return gson.toJson(taskManager.getTaskWithNotification(id));
    }

    private void createTask(Task task) {
        taskManager.addTask(task);
    }

    private void updateTask(Task task) {
        taskManager.updateTask(task);
    }

    private void deleteTasks() {
        taskManager.deleteTasks();
    }

    private void deleteTask(int id) {
        taskManager.deleteTask(id);
    }
}
