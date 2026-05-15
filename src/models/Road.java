package models;

public class Road {
    private Location destination;
    private int travelTime;

    public Road(Location destination, int travelTime) {
        this.destination = destination;
        this.travelTime = travelTime;
    }

    public Location getDestination() {
        return destination;
    }

    public int getTraveltime() {
        return travelTime;
    }

}