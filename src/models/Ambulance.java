package models;

public class Ambulance {
    private String ambulanceId;
    private Location currentLocation;
    private boolean isAvailable;

    public Ambulance(String ambulanceId, Location currentLocation) {
        this.ambulanceId = ambulanceId;
        this.currentLocation = currentLocation;
        isAvailable = true;
    }

    public String getAmbulanceId() {
        return ambulanceId;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public boolean getAvailability() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

}