package datastructures;

import models.Location;
import models.Road;

import java.util.*;

/**
 * Handles the Graph data structure (nodes/roads/weights) and pathfinding
 * calculations.
 * Implements Dijkstra's Algorithm to find the shortest path by road distance.
 * Fully integrated with your team's Location, Road, and DispatchManager models.
 */
public class CityGraph {
    private static final int AMBULANCE_AVERAGE_SPEED_KMH = 80;

    // Maps a unique location name string to its corresponding Location object
    private final Map<String, Location> nodes;

    // Adjacency list: Maps a Location to its outgoing Road connections
    private final Map<Location, List<Road>> adjacencyList;

    public CityGraph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Adds a location node to the city graph network.
     */
    public void addLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location is required.");
        }
        if (nodes.containsKey(location.getLocationName())) {
            throw new IllegalArgumentException("Duplicate location: " + location.getLocationName());
        }
        nodes.put(location.getLocationName(), location);
        adjacencyList.putIfAbsent(location, new ArrayList<>());
    }

    /**
     * Connects two registered locations with a directed Road edge and its distance.
     */
    public void addEdge(String sourceName, String targetName, int distanceKm) {
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Road distance must be positive.");
        }
        Location source = nodes.get(sourceName);
        Location target = nodes.get(targetName);

        if (source == null || target == null) {
            throw new IllegalArgumentException("Both locations must exist before adding a road: "
                    + sourceName + " -> " + targetName);
        }
        Road road = new Road(source, target, distanceKm);
        adjacencyList.get(source).add(road);
    }

    /**
     * Retrieves a location object by its unique name string.
     */
    public Location getLocation(String name) {
        return nodes.get(name);
    }

    /**
     * Core Integration Method used directly by DispatchManager.java.
     * Computes Dijkstra's algorithm and returns the final total distance in km.
     * * @param start The current Location of the ambulance
     * 
     * @param end The emergency destination Location (retrieved from the
     *            EmergencyCall)
     * @return Total shortest route distance in km, or Integer.MAX_VALUE if
     *         unreachable.
     */
    public int calculateShortestDistanceKm(Location start, Location end) {
        if (start == null || end == null) {
            return Integer.MAX_VALUE;
        }

        // If the ambulance is already at the call location, distance is zero.
        if (start.equals(end)) {
            return 0;
        }

        Map<Location, Integer> distances = runDijkstra(start);
        return distances.getOrDefault(end, Integer.MAX_VALUE);
    }

    /**
     * Backward-compatible ETA helper. Graph weights are kilometers; ETA is derived
     * from the shortest route distance using the system's average ambulance speed.
     */
    public int calculateEta(Location start, Location end) {
        int distanceKm = calculateShortestDistanceKm(start, end);
        if (distanceKm == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (distanceKm == 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round((distanceKm / (double) AMBULANCE_AVERAGE_SPEED_KMH) * 60 * 0.85));
    }

    /**
     * Pathfinding Sequence Method: Returns the full ordered sequence list of
     * Locations
     * making up the shortest route (essential for documenting project test
     * cases)
     */
    public List<Location> findShortestPath(String startName, String endName) {
        Location start = nodes.get(startName);
        Location end = nodes.get(endName);

        if (start == null || end == null) {
            return Collections.emptyList();
        }

        Map<Location, Integer> distances = new HashMap<>();
        Map<Location, Location> parentMap = new HashMap<>();

        // Min-Priority Queue for sorting node exploration path costs
        PriorityQueue<NodeDistancePair> pq = new PriorityQueue<>(
                Comparator.comparingInt(NodeDistancePair::getDistance));

        // Initialize distances
        for (Location loc : nodes.values()) {
            distances.put(loc, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.add(new NodeDistancePair(start, 0));

        while (!pq.isEmpty()) {
            NodeDistancePair currentPair = pq.poll();
            Location current = currentPair.getLocation();

            if (current.equals(end))
                break;
            if (currentPair.getDistance() > distances.get(current))
                continue;

            for (Road road : adjacencyList.getOrDefault(current, Collections.emptyList())) {
                Location neighbor = road.getTo();
                int weight = road.getDistanceKm();
                int newDist = distances.get(current) + weight;

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    parentMap.put(neighbor, current);
                    pq.add(new NodeDistancePair(neighbor, newDist));
                }
            }
        }

        return reconstructPath(parentMap, end, start);
    }

    /**
     * Internal Dijkstra logic runner calculating minimum distance from a start node
     * to all nodes
     */
    private Map<Location, Integer> runDijkstra(Location start) {
        Map<Location, Integer> distances = new HashMap<>();
        PriorityQueue<NodeDistancePair> pq = new PriorityQueue<>(
                Comparator.comparingInt(NodeDistancePair::getDistance));

        for (Location loc : nodes.values()) {
            distances.put(loc, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.add(new NodeDistancePair(start, 0));

        while (!pq.isEmpty()) {
            NodeDistancePair currentPair = pq.poll();
            Location current = currentPair.getLocation();

            if (currentPair.getDistance() > distances.get(current))
                continue;

            for (Road road : adjacencyList.getOrDefault(current, Collections.emptyList())) {
                Location neighbor = road.getTo();
                int weight = road.getDistanceKm();
                int newDist = distances.get(current) + weight;

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    pq.add(new NodeDistancePair(neighbor, newDist));
                }
            }
        }
        return distances;
    }

    /**
     * Backtracks using parent mappings to piece together the visual route
     * trajectory
     */
    private List<Location> reconstructPath(Map<Location, Location> parentMap, Location end, Location start) {
        if (!parentMap.containsKey(end) && !end.equals(start)) {
            return Collections.emptyList();
        }

        LinkedList<Location> path = new LinkedList<>();
        Location current = end;
        while (current != null) {
            path.addFirst(current);
            current = parentMap.get(current);
        }
        return path;
    }

    /**
     * Node pair container class used for tracking paths inside Dijkstra's priority
     * queue
     */
    private static class NodeDistancePair {
        private final Location location;
        private final int distance;

        public NodeDistancePair(Location location, int distance) {
            this.location = location;
            this.distance = distance;
        }

        public Location getLocation() {
            return location;
        }

        public int getDistance() {
            return distance;
        }
    }

    // UI Integration Getters
    public Map<String, Location> getNodes() {
        return nodes;
    }

    public Map<Location, List<Road>> getAdjacencyList() {
        return adjacencyList;
    }
}
