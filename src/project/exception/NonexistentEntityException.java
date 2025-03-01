package project.exception;

public class NonexistentEntityException extends RuntimeException {

    public NonexistentEntityException(String message) {
        super(message);
    }

    public NonexistentEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
