package models;


 //Represents a road connection (edge) between two locations.
 //Improved version with both from and to locations.
 
public class Road {
    private final Location from;
    private final Location to;
    private final int distanceKm;

    public Road(Location from, Location to, int distanceKm) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Road endpoints are required.");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Road distance must be positive.");
        }
        this.from = from;
        this.to = to;
        this.distanceKm = distanceKm;
    }

    // Getters
    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    // For backward compatibility (in case you used getDestination before)
    public Location getDestination() {
        return to;
    }

    @Override
    public String toString() {
        return from.getLocationName() + " --> " + to.getLocationName() 
               + " (" + distanceKm + " km)";
    }
}
