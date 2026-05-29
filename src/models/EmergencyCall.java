package models;

public class EmergencyCall implements Comparable<EmergencyCall> {

    private static int idCounter = 0; // Static counter to generate unique call IDs

    private int callId;              // Unique identifier for each call
    private int severity;            // Severity level (1 = Critical, 2 = Medium, 3 = Low)
    private Location location;       // Location of the emergency
    private String description;      // Description of the emergency (What happened, How many involed, etc.)
    private long incomingCallTime;   // Timestamp when the call was received (in milliseconds)

    public EmergencyCall(int severity, Location location, String description) {
        this.callId = ++idCounter;
        this.severity = severity;
        this.location = location;
        this.description = description;
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
    return "Call #: " + callId + " [ Severity: " + level +  "] " + description + " | Location: " + location.getLocationName();
}

    @Override
    public int compareTo(EmergencyCall other) {
        //Priority is based on severity first, then incoming call time first
        if (this.severity != other.severity){
            return Integer.compare(other.severity, this.severity);
        }
         else {
            return Long.compare(this.incomingCallTime, other.incomingCallTime);
    
        }

    }

}