package project.exception;

public class TaskOverlapException extends RuntimeException {
    public TaskOverlapException(String message) {
        super(message);
    }
}
