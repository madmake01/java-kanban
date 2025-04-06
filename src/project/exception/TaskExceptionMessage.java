package project.exception;

public class TaskExceptionMessage {
    public static final String ID_SHOULD_BE_DEFAULT = "Task does not have a default id";

    public static final String TASK_DOES_NOT_EXIST = "Task does not exist";

    public static final String EPIC_DOES_NOT_EXIST = "Epic does not exist";
    public static final String NEW_EPIC_SHOULD_BE_EMPTY = "New Epic should be empty";
    public static final String NEW_EPIC_SHOULD_HAVE_DEFAULT_STATUS = "New Epic should have default status";

    public static final String SUBTASK_DOES_NOT_EXIST = "Subtask does not exist";
    public static final String SUBTASK_SHOULD_HAVE_EQUAL_EPICS = "Subtasks should have equal epics";

    public static final String ERROR_SAVING_DATA = "Error saving data";
    public static final String ERROR_READING_FILE = "Error reading file";
    public static final String TASKS_CANT_HAVE_SAME_ID = "Tasks cannot have the same id";
    public static final String FILE_SHOULD_START_WITH = "File to parse should start with: ";

    private TaskExceptionMessage() {
    }
}
