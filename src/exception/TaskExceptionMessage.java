package exception;

public class TaskExceptionMessage {
    public static final String TASK_DOES_NOT_EXIST = "Task does not exist: ";

    public static final String EPIC_DOES_NOT_EXIST = "Epic does not exist: ";
    public static final String NEW_EPIC_SHOULD_BE_EMPTY = "New Epic should be empty: ";
    public static final String EPICS_SUBTASKS_SHOULD_BE_EQUAL = "Epic subtasks should be equal: ";

    public static final String SUBTASK_DOES_NOT_EXIST = "Subtask does not exist: ";
    public static final String SUBTASK_ALREADY_ASSOCIATED = "Subtask already associated: ";
    public static final String SUBTASK_ALREADY_EXISTS = "Subtask already exists: ";

    private TaskExceptionMessage() {
    }
}
