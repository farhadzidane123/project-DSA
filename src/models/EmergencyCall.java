package models;

public class EmergencyCall implements Comparable<EmergencyCall> {
    private int callId;
    private int severity;
    private Location location;
    private String description;

    public EmergencyCall(int callId, int severity, Location location, String description) {
        this.callId = callId;
        this.severity = severity;
        this.location = location;
        this.description = description;
    }

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

    @Override
    public int compareTo(EmergencyCall other) {
        return Integer.compare(this.severity, other.severity);
    }

}