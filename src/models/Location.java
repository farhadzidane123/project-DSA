package models;

import java.util.Objects;

public class Location {
    private String locationName;

    public Location(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationName() {
        return locationName;
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

        Location otherLocation = (Location) obj;

        return Objects.equals(this.locationName, otherLocation.locationName);
    }

    /*
     * Force Java to generate hash code based purely on the locationName instead of
     * the memory address
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.locationName);
    }
}