package models;

import java.util.Objects;

public class Location {
    private final String locationName;
    private final double xCoordinate;
    private final double yCoordinate;

    public Location(String locationName, double xCoordinate, double yCoordinate) {
        if (locationName == null || locationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is required.");
        }
        this.locationName = locationName.trim();
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getXCoordinate() {
        return xCoordinate;
    }

    public double getYCoordinate() {
        return yCoordinate;
    }

    /*
     * Override Object.equals() method to check the data instead of memory location
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;
        return Objects.equals(this.locationName, other.locationName);
    }

    /*
     * Force Java to generate hash code based purely on the locationName instead of
     * the memory address
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.locationName);
    }

    @Override
    public String toString() {
        return locationName + " (" + xCoordinate + ", " + yCoordinate + ")";
    }
}
