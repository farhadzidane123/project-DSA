package datastructures;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import models.EmergencyCall;

/**
 * A custom wrapper class for a standard FIFO queue.
 * Used exclusively for non-urgent calls (Severity 3).
 */
public class StandardEmergencyQueue {

    private Queue<EmergencyCall> queue;

    public StandardEmergencyQueue() {
        this.queue = new LinkedList<>();
    }

    public void enqueue(EmergencyCall call) {
        queue.add(call);
    }

    public EmergencyCall dequeue() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public List<EmergencyCall> getCalls() {
        return new ArrayList<>(queue);
    }
}