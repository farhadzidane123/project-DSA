package controller;

import models.Ambulance;
import models.EmergencyCall;
import datastructures.CityGraph;
import datastructures.TriageManager;
import java.util.ArrayList;
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
    public void handleIncomingCall(EmergencyCall call) {
        System.out.println("\n--- NEW EMERGENCY RECEIVED ---");
        System.out.println(call.toString());

        Ambulance availableAmbulance = findAvailableAmbulance();

        if (availableAmbulance != null) {
            // Step 2: If an ambulance IS available -> Dispatch it
            dispatchAmbulance(availableAmbulance, call);
        } else {
            // Step 3: If NO ambulances are available -> Send to queues
            System.out.println("Status: All ambulances busy. Routing to Triage Queues...");
            triageSystem.addCallToQueue(call);
        }
    }

    /**
     * Triggered when an ambulance completes an emergency.
     */
    public void finishAmbulanceJob(String ambulanceId) {
        Ambulance amb = getAmbulanceById(ambulanceId);

        if (amb != null) {
            System.out.println("\n--- AMBULANCE FREED ---");
            System.out.println(amb.getAmbulanceId() + " has completed its job and is available.");

            // Step 2: Use your custom model method to set availability back to true
            amb.completeCall();

            // Step 3 & 4: Check triage queues and dispatch if there are waiting calls
            if (triageSystem.hasWaitingCalls()) {
                System.out.println("Status: Pulling next priority call from Triage...");
                EmergencyCall nextCall = triageSystem.getNextHighestPriorityCall();
                dispatchAmbulance(amb, nextCall);
            } else {
                System.out.println("Status: No calls waiting. " + amb.getAmbulanceId() + " is on standby.");
            }
        } else {
            System.out.println("Error: Ambulance ID '" + ambulanceId + "' not found in fleet.");
        }
    }

    // --- HELPER METHODS ---

    private void dispatchAmbulance(Ambulance amb, EmergencyCall call) {
        System.out.println("DISPATCHING: " + amb.getAmbulanceId() + " to " + call.getLocation().getLocationName());

        // Calculate the route using the Graph Developer's Dijkstra method
        int eta = cityMap.calculateEta(amb.getCurrentLocation(), call.getLocation());
        System.out.println("Estimated Time of Arrival: " + eta + " minutes.");

        // Update the Ambulance state using your custom method
        amb.dispatchTo(call);

        // The ambulance's location physically updates to the emergency site
        amb.setCurrentLocation(call.getLocation());
    }

    /**
     * Iterates through the fleet to find the first ambulance where isAvailable ==
     * true.
     * Returns null if everyone is busy.
     */
    private Ambulance findAvailableAmbulance() {
        for (Ambulance amb : fleet) {
            // Utilizes the isAvailable() helper method you built in Ambulance.java
            if (amb.isAvailable()) {
                return amb;
            }
        }
        return null;
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
}
