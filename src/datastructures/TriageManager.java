package datastructures;

import models.EmergencyCall;

/*
 * Acts as the controller/coordinator for the underlying queue data structures.
 */
public class TriageManager {

    // Explicitly define the two data structure classes
    private EmergencyPriorityQueue priorityQueue;
    private StandardEmergencyQueue standardQueue;

    public TriageManager() {
        this.priorityQueue = new EmergencyPriorityQueue();
        this.standardQueue = new StandardEmergencyQueue();
    }

    /*
     * Checks severity and routes the call to the appropriate data structure.
     */
    public void addCallToQueue(EmergencyCall call) {
        int severity = call.getSeverity();

        // Route Critical (1) and Medium (2) to the Priority Queue
        if (severity == 1 || severity == 2) {
            System.out.println("Triage: Adding Call #" + call.getCallId() + " to Priority Queue.");
            priorityQueue.enqueue(call);
        }
        // Route Low (3) to the Standard FIFO Queue
        else {
            System.out.println("Triage: Adding Call #" + call.getCallId() + " to Standard FIFO Queue.");
            standardQueue.enqueue(call);
        }
    }

    /*
     * Pulls the next call. Priority queue is ALWAYS checked first.
     */
    public EmergencyCall getNextHighestPriorityCall() {
        // If there are high-priority calls waiting, serve them first
        if (!priorityQueue.isEmpty()) {
            return priorityQueue.dequeue();
        }
        // Only if the priority queue is entirely empty, serve from the standard queue
        else if (!standardQueue.isEmpty()) {
            return standardQueue.dequeue();
        }

        return null; // Both queues are empty
    }

    /*
     * Returns true if either of the underlying queues have pending calls.
     */
    public boolean hasWaitingCalls() {
        return !priorityQueue.isEmpty() || !standardQueue.isEmpty();
    }
}