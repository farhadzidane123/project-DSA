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

    // --- SYSTEM METHODS (TODOs for your teammate) ---

    /**
     * Adds a newly created ambulance to the fleet list.
     */
    public void registerAmbulance(Ambulance amb) {
        // TODO: Add the passed ambulance to the 'fleet' ArrayList.
    }

    /**
     * The main logic block for when a 911 call is created.
     */
    public void handleIncomingCall(EmergencyCall call) {
        // TODO Step 1: Find if there is an available ambulance in the fleet.
        // TODO Step 2: If an ambulance IS available -> Dispatch it (calculate ETA, set
        // it to busy).
        // TODO Step 3: If NO ambulances are available -> Send the call to the
        // 'triageSystem' queues.
    }

    /**
     * Triggered when an ambulance completes an emergency.
     */
    public void finishAmbulanceJob(String ambulanceId) {
        // TODO Step 1: Find the ambulance in the fleet using the provided ID.
        // TODO Step 2: Set that ambulance's availability back to 'true'.
        // TODO Step 3: Ask the 'triageSystem' if there are any calls waiting in the
        // queues.
        // TODO Step 4: If there is a waiting call, pop the highest priority one and
        // dispatch this ambulance to it immediately.
    }

    // --- HELPER METHODS (Optional but recommended for clean code) ---

    /**
     * Iterates through the fleet to find the first ambulance where isAvailable ==
     * true.
     * Returns null if everyone is busy.
     */
    private Ambulance findAvailableAmbulance() {
        // TODO: Loop through the 'fleet' list and return the first available one.
        return null;
    }

    /**
     * Finds a specific ambulance in the fleet by its ID.
     */
    private Ambulance getAmbulanceById(String id) {
        // TODO: Loop through the 'fleet' and return the ambulance whose ID matches the
        // string.
        return null;
    }
}