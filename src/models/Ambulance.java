package models;

public class Ambulance {
    private String ambulanceId;
    private AmbulanceStatus status;
    private Location currentLocation;
    private EmergencyCall currentCall;

    public Ambulance(String ambulanceId, Location currentLocation) {
        this.ambulanceId = ambulanceId;
        this.currentLocation = currentLocation;
        this.status = AmbulanceStatus.AVAILABLE;
        this.currentCall = null;
    }

    public boolean dispatchTo(EmergencyCall call) {
        if (this.status == AmbulanceStatus.AVAILABLE) {
            this.currentCall = call;
            this.status = AmbulanceStatus.BUSY;
            return true;
        }
        return false;
    }

    public void completeCall() {
        if (this.status == AmbulanceStatus.BUSY) {
            this.currentCall = null;
            this.status = AmbulanceStatus.AVAILABLE;
        }
    }

    public void setMaintenance() {
        this.status = AmbulanceStatus.MAINTENANCE;
        this.currentCall = null;
    }

    public void setOffline() {
        this.status = AmbulanceStatus.OFFLINE;
        this.currentCall = null;
    }

    public void setAvailable() {
        this.status = AmbulanceStatus.AVAILABLE;
        this.currentCall = null;
    }

    // ============= Getters and Setters =============

    public String getAmbulanceId() {
        return ambulanceId;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public AmbulanceStatus getStatus() {
        return status;
    }

    public EmergencyCall getCurrentCall() {
        return currentCall;
    }

    public boolean isAvailable() {
        return this.status == AmbulanceStatus.AVAILABLE;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public String toString() {
        String info = "Ambulance ID: " + ambulanceId + " [" + status + "]";
        if (currentCall != null) {
            info += " | Serving: " + currentCall.getDescription() + " at "
                    + currentCall.getLocation().getLocationName();
        }
        return info;
    }
}
