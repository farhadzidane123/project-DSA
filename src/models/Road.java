package models;


 //Represents a road connection (edge) between two locations.
 //Improved version with both from and to locations.
 
public class Road {
    private  Location from;
    private  Location to;
    private  int travelTime;   // in minutes

    public Road(Location from, Location to, int travelTime) {
        this.from = from;
        this.to = to;
        this.travelTime = travelTime;
    }

    // Getters
    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public int getTravelTime() {
        return travelTime;
    }

    // For backward compatibility (in case you used getDestination before)
    public Location getDestination() {
        return to;
    }

    @Override
    public String toString() {
        return from.getLocationName() + " --> " + to.getLocationName() 
               + " (" + travelTime + " min)";
    }
}