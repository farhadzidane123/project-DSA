package models;

import java.util.concurrent.atomic.AtomicInteger;

public class EmergencyCall implements Comparable<EmergencyCall> {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final int callId; // Unique identifier for each call
    private final int severity; // Severity level (1 = Critical, 2 = Medium, 3 = Low)
    private final Location location; // Location of the emergency
    private final String description; // Description of the emergency
    private final long incomingCallTime; // Timestamp when the call was received (in milliseconds)

    public EmergencyCall(int severity, Location location, String description) {
        if (severity < 1 || severity > 3) {
            throw new IllegalArgumentException("Severity must be 1, 2, or 3.");
        }
        if (location == null) {
            throw new IllegalArgumentException("Emergency location is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Emergency description is required.");
        }

        this.callId = ID_COUNTER.incrementAndGet();
        this.severity = severity;
        this.location = location;
        this.description = description.trim();
        this.incomingCallTime = System.currentTimeMillis();
    }

    // ============= Getters =============
    public int getCallId() {
        return callId;
    }

    public int getSeverity() {
        return severity;
    }

    public Location getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    // Return the time when the call was received (in milliseconds)
    public long getIncomingCallTime() {
        return incomingCallTime;
    }

    @Override
    public String toString() {
        String level = switch (severity) {
            case 1 -> "Critical";
            case 2 -> "Medium";
            case 3 -> "Low";
            default -> "Unknown";
        };
        return "Call #: " + callId + " [ Severity: " + level + "] " + description + " | Location: "
                + location.getLocationName();
    }

    @Override
    public int compareTo(EmergencyCall other) {
        // Priority is based on severity first (1 is highest priority in a Min-Heap)
        if (this.severity != other.severity) {
            return Integer.compare(this.severity, other.severity);
        }
        if (this.incomingCallTime != other.incomingCallTime) {
            return Long.compare(this.incomingCallTime, other.incomingCallTime);
        }
        return Integer.compare(this.callId, other.callId);
    }

}
