package project.manager;

import project.model.AbstractTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> links = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(AbstractTask task) {
        int taskId = task.getId();

        remove(taskId);

        Node node = new Node(null, task, null);
        linkLast(node);

        links.put(taskId, node);
    }

    @Override
    public List<AbstractTask> getDefaultHistory() {
        List<AbstractTask> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(current.item);
            current = current.next;
        }

        return tasks;
    }

    @Override
    public void remove(int id) {
        Node node = links.remove(id);

        if (node != null) {
            removeNode(node);
        }
    }

    private void linkLast(Node node) {
        if (head == null) {
            head = node;
        } else {
            node.prev = tail;
            tail.next = node;
        }

        tail = node;
    }

    private void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        if (head == node) {
            head = next;
        }

        if (tail == node) {
            tail = prev;
        }

        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
    }

    private static class Node {
        private final AbstractTask item;
        private Node next;
        private Node prev;

        Node(Node prev, AbstractTask element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
