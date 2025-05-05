package project.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import project.enums.Endpoint;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;
import project.model.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EpicsController extends BaseHttpHandler {

    public EpicsController(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    public EpicsController(Gson gson) {
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
            case GET_ALL -> sendText(exchange, getEpics());
            case GET -> sendText(exchange, getEpic(Integer.parseInt(pathParts[1])));
            case GET_EPIC_SUBTASKS -> sendText(exchange, getEpicSubtasks(Integer.parseInt(pathParts[1])));
            case POST -> send(exchange, handlePost(exchange), 201);
            case DELETE_ALL -> {
                deleteEpics();
                sendEmptyResponseWithCode(exchange, 200);
            }
            case DELETE -> {
                deleteEpic(Integer.parseInt(pathParts[1]));
                sendEmptyResponseWithCode(exchange, 200);
            }

            default -> throw new NonexistentEntityException("Endpoint not found: " + requestMethod + " " + requestPath);
        }
    }

    private String handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getId() == 0) {
            return createEpic(epic);
        } else {
            return updateEpic(epic);
        }
    }

    private String getEpics() {
        return gson.toJson(taskManager.getEpics());
    }

    private String getEpic(int id) {
        return gson.toJson(taskManager.getEpicWithNotification(id));
    }

    private String getEpicSubtasks(int id) {
        return gson.toJson(taskManager.getEpicSubtasks(id));
    }

    private String createEpic(Epic epic) {
        Epic added = taskManager.addEpic(epic);
        return gson.toJson(added);
    }

    private String updateEpic(Epic epic) {
        Epic updated = taskManager.updateEpic(epic);
        return gson.toJson(updated);
    }

    private void deleteEpics() {
        taskManager.deleteEpics();
    }

    private void deleteEpic(int id) {
        taskManager.deleteEpic(id);
    }
}
