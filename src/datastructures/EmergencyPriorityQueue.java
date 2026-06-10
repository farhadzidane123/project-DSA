package datastructures;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import models.EmergencyCall;

/**
 * A custom wrapper class for the Priority Queue implementation.
 * Used exclusively for high-priority calls (Severity 1 and 2).
 */
public class EmergencyPriorityQueue {

    private PriorityQueue<EmergencyCall> heap;

    public EmergencyPriorityQueue() {
        // The PriorityQueue will automatically use the compareTo method in
        // EmergencyCall
        this.heap = new PriorityQueue<>();
    }

    public void enqueue(EmergencyCall call) {
        heap.add(call);
    }

    public EmergencyCall dequeue() {
        return heap.poll();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    public List<EmergencyCall> getCalls() {
        List<EmergencyCall> list = new ArrayList<>(heap);
        Collections.sort(list);
        return list;
    }
}