package project.manager;

import project.model.AbstractTask;

import java.util.List;

public interface HistoryManager {

    void add(AbstractTask task);

    List<AbstractTask> getDefaultHistory();

}
