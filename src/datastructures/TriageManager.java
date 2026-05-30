package datastructures;

import java.util.*;
import models.EmergencyCall;

public class TriageManager {
    public TriageManager() {
    }

    // Write the logic to check severity and put it into the right queue
    public void addCallToQueue(EmergencyCall call) {
    }

    /*
     * Write logic to check the priority queue first
     * Pull from the standard queue if the priority one is empty
     */
    public EmergencyCall getNextHighestPriorityCall() {
        return null; // Implement queue polling logic
    }

    public boolean hasWaitingCalls() {
        return false; // Check if both queues are empty
    }
}