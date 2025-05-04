package project.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import project.enums.Endpoint;
import project.manager.TaskManager;
import project.util.Managers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    protected BaseHttpHandler(Gson gson) {
        this.taskManager = Managers.getDefaultTaskManager();
        this.gson = gson;
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected Endpoint getTaskEndpoint(String[] pathParts, String requestMethod) {


        final int pathLength = pathParts.length;
        return switch (requestMethod) {
            case "GET" -> {
                if (pathLength == 1) {
                    yield Endpoint.GET_ALL;
                }
                if (pathLength == 2 && isInteger(pathParts[1])) {
                    yield Endpoint.GET;
                }
                yield Endpoint.UNKNOWN;
            }

            case "POST" -> {
                if (pathLength == 1) {
                    yield Endpoint.CREATE;
                }
                if (pathLength == 2 && isInteger(pathParts[1])) {
                    yield Endpoint.UPDATE;
                }
                yield Endpoint.UNKNOWN;
            }

            case "DELETE" -> {
                if (pathLength == 1) {
                    yield Endpoint.DELETE_ALL;
                }
                if (pathLength == 2 && isInteger(pathParts[1])) {
                    yield Endpoint.DELETE;
                }
                yield Endpoint.UNKNOWN;
            }

            default -> Endpoint.UNKNOWN;
        };
    }


    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}