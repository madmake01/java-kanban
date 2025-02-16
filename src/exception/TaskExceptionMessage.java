package exception;

public class TaskExceptionMessage {
    public static final String TASK_DOES_NOT_EXIST = "Task does not exist: ";

    public static final String EPIC_DOES_NOT_EXIST = "Epic does not exist: ";

    public static final String SUBTASK_DOES_NOT_EXIST = "Subtask does not exist: ";
    public static final String SUBTASK_DOES_NOT_ASSOCIATED = "Subtask does not associated: ";
    public static final String SUBTASK_ALREADY_ASSOCIATED = "Subtask already associated: ";
    public static final String ASSOCIATED_EPIC_DOES_NOT_EXIST = "Associated with Subtask Epic does not exist: ";
    public static final String SUBTASK_ALREADY_EXISTS = "Subtask already exists: ";

    private TaskExceptionMessage() {
    }
}
