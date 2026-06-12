package controller;

import models.Ambulance;
import models.EmergencyCall;
import models.Location;
import models.Road;
import datastructures.CityGraph;
import datastructures.TriageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DispatchManager {

    // --- CORE VARIABLES ---
    private List<Ambulance> fleet;
    private CityGraph cityMap;
    private TriageManager triageSystem;

    // --- CONSTRUCTOR ---
    public DispatchManager(CityGraph cityMap, TriageManager triageSystem) {
        this.fleet = new ArrayList<>();
        this.cityMap = cityMap;
        this.triageSystem = triageSystem;
    }

    // --- SYSTEM METHODS ---

    /**
     * Adds a newly created ambulance to the fleet list.
     */
    public void registerAmbulance(Ambulance amb) {
        fleet.add(amb);
        System.out.println(
                "System: Registered " + amb.getAmbulanceId() + " at " + amb.getCurrentLocation().getLocationName());
    }

    /**
     * The main logic block for when a 911 call is created.
     */
    public DispatchResult handleIncomingCall(EmergencyCall call) {
        System.out.println("\n--- NEW EMERGENCY RECEIVED ---");
        System.out.println(call.toString());

        if (!hasAvailableAmbulance()) {
            // Step 3: If NO ambulances are available -> Send to queues
            System.out.println("Status: All ambulances busy. Routing to Triage Queues...");
            triageSystem.addCallToQueue(call);
            return DispatchResult.queued(call);
        }

        DispatchResult result = dispatchNearestAvailableAmbulance(call, false);
        if (result.getStatus() == DispatchStatus.NO_ROUTE) {
            System.out.println("Status: Available ambulances cannot reach this location.");
        }
        return result;
    }

    /**
     * Triggered when an ambulance completes an emergency.
     */
    public DispatchResult finishAmbulanceJob(String ambulanceId) {
        return finishAmbulanceJob(ambulanceId, null);
    }

    public DispatchResult finishAmbulanceJob(String ambulanceId, Location finalLocation) {
        Ambulance amb = getAmbulanceById(ambulanceId);

        if (amb != null) {
            System.out.println("\n--- AMBULANCE FREED ---");
            System.out.println(amb.getAmbulanceId() + " has completed its job and is available.");

            EmergencyCall completedCall = amb.getCurrentCall();
            if (finalLocation != null) {
                amb.setCurrentLocation(finalLocation);
            } else if (completedCall != null) {
                amb.setCurrentLocation(completedCall.getLocation());
            }

            // Step 2: Use custom model method to set availability back to true
            amb.completeCall();

            // Step 3 & 4: Check triage queues and dispatch if there are waiting calls
            if (triageSystem.hasWaitingCalls()) {
                System.out.println("Status: Pulling next priority call from Triage...");
                return dispatchNextWaitingCall();
            } else {
                System.out.println("Status: No calls waiting. " + amb.getAmbulanceId() + " is on standby.");
                return DispatchResult.completedNoWaitingCall(amb);
            }
        } else {
            System.out.println("Error: Ambulance ID '" + ambulanceId + "' not found in fleet.");
            return DispatchResult.ambulanceNotFound(ambulanceId);
        }
    }

    public DispatchResult dispatchNextWaitingCall() {
        if (!triageSystem.hasWaitingCalls()) {
            return DispatchResult.noWaitingCall();
        }
        if (!hasAvailableAmbulance()) {
            return DispatchResult.noAvailableAmbulance();
        }

        EmergencyCall nextCall = triageSystem.getNextHighestPriorityCall();
        DispatchResult result = dispatchNearestAvailableAmbulance(nextCall, true);
        if (result.getStatus() == DispatchStatus.NO_ROUTE) {
            triageSystem.addCallToQueue(nextCall);
        }
        return result;
    }

    // --- HELPER METHODS ---

    private DispatchResult dispatchNearestAvailableAmbulance(EmergencyCall call, boolean pulledFromQueue) {
        List<DispatchCandidate> candidates = findDispatchCandidates(call.getLocation());
        if (candidates.isEmpty()) {
            return DispatchResult.noRoute(call);
        }

        DispatchCandidate best = candidates.get(0);
        return dispatchAmbulance(best, call, candidates, pulledFromQueue);
    }

    private DispatchResult dispatchAmbulance(DispatchCandidate candidate, EmergencyCall call,
            List<DispatchCandidate> candidates, boolean pulledFromQueue) {
        Ambulance amb = candidate.getAmbulance();
        System.out.println("DISPATCHING: " + amb.getAmbulanceId() + " to " + call.getLocation().getLocationName());

        // Calculate the route using the Graph Developer's Dijkstra method
        int eta = candidate.getEtaMinutes();
        System.out.println("Estimated Time of Arrival: " + eta + " minutes.");

        // Update the Ambulance state using your custom method
        amb.dispatchTo(call);

        return DispatchResult.assigned(call, candidate, candidates, pulledFromQueue);
    }

    /**
     * Builds nearest-ambulance candidates for a location using Dijkstra routes.
     */
    public List<DispatchCandidate> findDispatchCandidates(Location target) {
        List<DispatchCandidate> candidates = new ArrayList<>();
        for (Ambulance amb : fleet) {
            if (!amb.isAvailable()) {
                continue;
            }

            List<Location> path = cityMap.findShortestPath(
                    amb.getCurrentLocation().getLocationName(),
                    target.getLocationName());
            int eta = cityMap.calculateEta(amb.getCurrentLocation(), target);
            if (eta != Integer.MAX_VALUE && !path.isEmpty()) {
                candidates.add(new DispatchCandidate(amb, target, path, routeDistanceKm(path), eta));
            }
        }

        candidates.sort(Comparator.comparingDouble(DispatchCandidate::getDistanceKm));
        return candidates;
    }

    private boolean hasAvailableAmbulance() {
        for (Ambulance amb : fleet) {
            if (amb.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    private double routeDistanceKm(List<Location> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += roadDistanceKm(path.get(i), path.get(i + 1));
        }
        return total;
    }

    private double roadDistanceKm(Location from, Location to) {
        for (Road road : cityMap.getAdjacencyList().getOrDefault(from, Collections.emptyList())) {
            if (road.getTo().equals(to)) {
                return road.getTravelTime();
            }
        }
        return 1;
    }

    /**
     * Finds a specific ambulance in the fleet by its ID.
     */
    private Ambulance getAmbulanceById(String id) {
        for (Ambulance amb : fleet) {
            if (amb.getAmbulanceId().equalsIgnoreCase(id)) {
                return amb;
            }
        }
        return null;
    }

    // UI Integration Getter
    public List<Ambulance> getFleet() {
        return fleet;
    }

    // UI Integration Getter
    public TriageManager getTriageSystem() {
        return triageSystem;
    }

    public enum DispatchStatus {
        ASSIGNED,
        QUEUED,
        COMPLETED_NO_WAITING_CALL,
        NO_WAITING_CALL,
        NO_AVAILABLE_AMBULANCE,
        NO_ROUTE,
        AMBULANCE_NOT_FOUND
    }

    public static final class DispatchCandidate {
        private final Ambulance ambulance;
        private final Location target;
        private final List<Location> path;
        private final double distanceKm;
        private final int etaMinutes;

        private DispatchCandidate(Ambulance ambulance, Location target, List<Location> path,
                double distanceKm, int etaMinutes) {
            this.ambulance = ambulance;
            this.target = target;
            this.path = path;
            this.distanceKm = distanceKm;
            this.etaMinutes = etaMinutes;
        }

        public Ambulance getAmbulance() {
            return ambulance;
        }

        public Location getTarget() {
            return target;
        }

        public List<Location> getPath() {
            return path;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public int getEtaMinutes() {
            return etaMinutes;
        }
    }

    public static final class DispatchResult {
        private final DispatchStatus status;
        private final EmergencyCall call;
        private final DispatchCandidate assignedCandidate;
        private final List<DispatchCandidate> candidates;
        private final boolean pulledFromQueue;
        private final String ambulanceId;

        private DispatchResult(DispatchStatus status, EmergencyCall call, DispatchCandidate assignedCandidate,
                List<DispatchCandidate> candidates, boolean pulledFromQueue, String ambulanceId) {
            this.status = status;
            this.call = call;
            this.assignedCandidate = assignedCandidate;
            this.candidates = candidates;
            this.pulledFromQueue = pulledFromQueue;
            this.ambulanceId = ambulanceId;
        }

        private static DispatchResult assigned(EmergencyCall call, DispatchCandidate assignedCandidate,
                List<DispatchCandidate> candidates, boolean pulledFromQueue) {
            return new DispatchResult(DispatchStatus.ASSIGNED, call, assignedCandidate,
                    new ArrayList<>(candidates), pulledFromQueue, assignedCandidate.getAmbulance().getAmbulanceId());
        }

        private static DispatchResult queued(EmergencyCall call) {
            return new DispatchResult(DispatchStatus.QUEUED, call, null, Collections.emptyList(), false, null);
        }

        private static DispatchResult completedNoWaitingCall(Ambulance ambulance) {
            return new DispatchResult(DispatchStatus.COMPLETED_NO_WAITING_CALL, null, null,
                    Collections.emptyList(), false, ambulance.getAmbulanceId());
        }

        private static DispatchResult noRoute(EmergencyCall call) {
            return new DispatchResult(DispatchStatus.NO_ROUTE, call, null, Collections.emptyList(), false, null);
        }

        private static DispatchResult noWaitingCall() {
            return new DispatchResult(DispatchStatus.NO_WAITING_CALL, null, null, Collections.emptyList(), false, null);
        }

        private static DispatchResult noAvailableAmbulance() {
            return new DispatchResult(DispatchStatus.NO_AVAILABLE_AMBULANCE, null, null,
                    Collections.emptyList(), false, null);
        }

        private static DispatchResult ambulanceNotFound(String ambulanceId) {
            return new DispatchResult(DispatchStatus.AMBULANCE_NOT_FOUND, null, null,
                    Collections.emptyList(), false, ambulanceId);
        }

        public DispatchStatus getStatus() {
            return status;
        }

        public EmergencyCall getCall() {
            return call;
        }

        public DispatchCandidate getAssignedCandidate() {
            return assignedCandidate;
        }

        public List<DispatchCandidate> getCandidates() {
            return candidates;
        }

        public boolean isPulledFromQueue() {
            return pulledFromQueue;
        }

        public String getAmbulanceId() {
            return ambulanceId;
        }
    }
}
