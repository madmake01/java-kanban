package project.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import project.enums.Endpoint;
import project.exception.NonexistentEntityException;
import project.manager.TaskManager;
import project.model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SubtasksController extends BaseHttpHandler {

    public SubtasksController(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    public SubtasksController(Gson gson) {
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
            case GET_ALL -> sendText(exchange, getSubtasks());
            case GET -> sendText(exchange, getSubtask(Integer.parseInt(pathParts[1])));

            case CREATE -> {
                createSubtask(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                sendEmptyResponseWithCode(exchange, 201);
            }
            case UPDATE -> {
                updateSubtask(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                sendEmptyResponseWithCode(exchange, 201);
            }
            case DELETE_ALL -> {
                deleteSubtasks();
                sendEmptyResponseWithCode(exchange, 200);
            }
            case DELETE -> {
                deleteSubtask(Integer.parseInt(pathParts[1]));
                sendEmptyResponseWithCode(exchange, 200);
            }
            default -> throw new NonexistentEntityException("Endpoint not found: " + requestMethod + " " + requestPath);
        }
    }

    private String getSubtasks() {
        return gson.toJson(taskManager.getSubtasks());
    }

    private String getSubtask(int id) {
        return gson.toJson(taskManager.getSubtaskWithNotification(id));
    }

    private void createSubtask(String subtaskJson) {
        JsonObject root = JsonParser.parseString(subtaskJson).getAsJsonObject();
        Subtask subtask = gson.fromJson(root.get("subtask"), Subtask.class);
        int epicId = root.get("epicId").getAsInt();

        taskManager.addSubtask(subtask, epicId);
    }

    private void updateSubtask(String subtaskJson) {
        Subtask subtask = gson.fromJson(subtaskJson, Subtask.class);
        taskManager.updateSubtask(subtask);
    }

    private void deleteSubtasks() {
        taskManager.deleteSubtasks();
    }

    private void deleteSubtask(int id) {
        taskManager.deleteSubtask(id);
    }
}
