package project.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import project.controller.BaseHttpHandler;
import project.exception.EntityAlreadyExistsException;
import project.exception.ManagerSaveException;
import project.exception.NonexistentEntityException;
import project.exception.TaskOverlapException;

import java.io.IOException;
import java.security.InvalidParameterException;

public class ExceptionHandler extends Filter {
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } catch (NonexistentEntityException e) {
            BaseHttpHandler.sendError(exchange, e.getMessage(), 404);
        } catch (EntityAlreadyExistsException e) {
            BaseHttpHandler.sendError(exchange, e.getMessage(), 409);
        } catch (TaskOverlapException e) {
            BaseHttpHandler.sendError(exchange, e.getMessage(), 406);
        } catch (ManagerSaveException e) {
            BaseHttpHandler.sendError(exchange, e.getMessage(), 500);
        } catch (InvalidParameterException e) {
            BaseHttpHandler.sendError(exchange, e.getMessage(), 400);
        } catch (Exception e) {
            String message = "Interval server error: " + e.getClass().getSimpleName();
            BaseHttpHandler.sendError(exchange, message, 500);
            e.printStackTrace();
        }
    }

    @Override
    public String description() {
        return "exception handler";
    }
}
