package datastructures;

import models.Location;
import models.Road;

import java.util.*;

/**
 * Handles the Graph data structure (nodes/roads/weights) and pathfinding
 * calculations.
 * Implements Dijkstra's Algorithm to find the shortest path and return travel
 * time ETAs.
 * Fully integrated with your team's Location, Road, and DispatchManager models.
 */
public class CityGraph {
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
        if (location != null && !nodes.containsKey(location.getLocationName())) {
            nodes.put(location.getLocationName(), location);
            adjacencyList.putIfAbsent(location, new ArrayList<>());
        }
    }

    /**
     * Connects two registered locations with a directed Road edge and its travel
     * time.
     */
    public void addEdge(String sourceName, String targetName, int travelTime) {
        Location source = nodes.get(sourceName);
        Location target = nodes.get(targetName);

        if (source != null && target != null) {
            Road road = new Road(source, target, travelTime);
            adjacencyList.get(source).add(road);
        }
    }

    /**
     * Retrieves a location object by its unique name string.
     */
    public Location getLocation(String name) {
        return nodes.get(name);
    }

    /**
     * Core Integration Method used directly by DispatchManager.java.
     * Computes Dijkstra's algorithm and returns the final total integer travel
     * time[cite: 16, 21].
     * * @param start The current Location of the ambulance
     * 
     * @param end The emergency destination Location (retrieved from the
     *            EmergencyCall)
     * @return Total ETA travel time in minutes, or Integer.MAX_VALUE if
     *         unreachable.
     */
    public int calculateEta(Location start, Location end) {
        if (start == null || end == null) {
            return 0;
        }

        // If the ambulance is already at the call location, ETA is zero
        if (start.equals(end)) {
            return 0;
        }

        Map<Location, Integer> distances = runDijkstra(start);
        return distances.getOrDefault(end, Integer.MAX_VALUE);
    }

    /**
     * Pathfinding Sequence Method: Returns the full ordered sequence list of
     * Locations
     * making up the shortest route (essential for documenting project test
     * cases)[cite: 21, 22].
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
                int weight = road.getTravelTime();
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
     * Internal Dijkstra logic runner calculating minimum weights from a start node
     * to all nodes[cite: 16].
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
                int weight = road.getTravelTime();
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
     * trajectory[cite: 16].
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
     * queue[cite: 16].
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
