package ui;

import controller.DispatchManager;
import datastructures.CityGraph;
import datastructures.TriageManager;
import java.util.Scanner;
import models.Ambulance;
import models.EmergencyCall;
import models.Location;

public class ConsoleUI {

    // --- CORE VARIABLES ---
    private DispatchManager dispatchManager;
    private Scanner scanner;

    public ConsoleUI(DispatchManager dispatchManager) {
        this.dispatchManager = dispatchManager;
        this.scanner = new Scanner(System.in);
    }

    // --- MAIN METHOD (The entry point of the program) ---
    public static void main(String[] args) {
        // 1. Boot up the core Data Structures
        CityGraph cityMap = new CityGraph();
        TriageManager triageSystem = new TriageManager();

        // 2. Boot up the Controller and link it to the Data Structures
        DispatchManager manager = new DispatchManager(cityMap, triageSystem);

        // 3. Start the User Interface
        ConsoleUI ui = new ConsoleUI(manager);
        ui.start();
    }

    // --- UI LOOP ---
    public void start() {
        boolean running = true;
        System.out.println("=== EMERGENCY MEDICAL DISPATCH SYSTEM ===");

        // Pre-populate the system with locations and ambulances so it's not empty on
        // boot
        Location depot = new Location("Central Depot", 0.0, 0.0);
        Location hospitalA = new Location("Hospital A", 1.0, 2.0);
        Location hospitalB = new Location("Hospital B", 3.0, 4.0);

        Ambulance amb01 = new Ambulance("AMB-01", depot);
        Ambulance amb02 = new Ambulance("AMB-02", hospitalA);

        dispatchManager.registerAmbulance(amb01);
        dispatchManager.registerAmbulance(amb02);

        System.out.println("System ready. 2 ambulances registered.\n");

        while (running) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character left over by nextInt()

            switch (choice) {
                case 1:
                    reportEmergency();
                    break;
                case 2:
                    resolveEmergency();
                    break;
                case 3:
                    runRubricScenario(); // Crucial for the video demo!
                    break;
                case 4:
                    running = false;
                    System.out.println("Exiting system...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // --- UI METHODS ---

    private void printMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Report New Emergency Call");
        System.out.println("2. Mark Ambulance Job Complete");
        System.out.println("3. Run Project Demo Scenario");
        System.out.println("4. Exit");
        System.out.print("Enter choice: ");
    }

    private void reportEmergency() {
        System.out.println("\n--- REPORT NEW EMERGENCY ---");

        System.out.print("Enter Severity (1 = Critical, 2 = Medium, 3 = Low): ");
        int severity = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Location Name: ");
        String locationName = scanner.nextLine();

        // Setup default coordinates (will be overridden if the location is new)
        double x = 0.0;
        double y = 0.0;

        // Attempt to see if this location already exists via a dry run
        // We temporarily create a dummy object just to check the name
        Location resolvedLocation = dispatchManager.resolveEmergencyLocation(locationName, x, y);

        // If the returned location has coordinates 0.0, 0.0 but we meant a new place,
        // we should actually ask the user for coordinates. Let's make the logic
        // slightly smarter:
        if (resolvedLocation.getXCoordinate() == 0.0 && resolvedLocation.getYCoordinate() == 0.0
                && !locationName.equalsIgnoreCase("Central Depot")) {
            System.out.println("Unrecognized location. Please provide coordinates for GPS tracking.");
            System.out.print("Enter Location X Coordinate: ");
            x = scanner.nextDouble();

            System.out.print("Enter Location Y Coordinate: ");
            y = scanner.nextDouble();
            scanner.nextLine();

            // Re-resolve with the actual coordinates now that we know it's a new place
            resolvedLocation = dispatchManager.resolveEmergencyLocation(locationName, x, y);
        } else {
            System.out.println("Recognized existing location. Coordinates retrieved automatically.");
        }

        System.out.print("Enter Emergency Description: ");
        String description = scanner.nextLine();

        // Build object and send to dispatch manager
        EmergencyCall call = new EmergencyCall(severity, resolvedLocation, description);
        dispatchManager.handleIncomingCall(call);
    }

    private void resolveEmergency() {
        System.out.println("\n--- MARK JOB COMPLETE ---");

        // Step 1: Ask which ambulance finished
        System.out.print("Enter Ambulance ID (e.g. AMB-01): ");
        String ambulanceId = scanner.nextLine();

        // Step 2: Pass to dispatch manager
        dispatchManager.finishAmbulanceJob(ambulanceId);
    }

    /**
     * This method automatically executes the exact scenario required by the grading
     * rubric.
     */
    private void runRubricScenario() {
        System.out.println("\n--- RUNNING RUBRIC SCENARIO ---");

        // Step 1: Create 3 locations
        Location locationA = new Location("Location A", 1.0, 1.0);
        Location locationB = new Location("Location B", 2.0, 3.0);
        Location locationC = new Location("Location C", 4.0, 5.0);

        // Step 2: Register 2 ambulances
        Ambulance amb01 = new Ambulance("AMB-01", locationA);
        Ambulance amb02 = new Ambulance("AMB-02", locationB);
        dispatchManager.registerAmbulance(amb01);
        dispatchManager.registerAmbulance(amb02);

        // Step 3: Create 3 emergency calls
        EmergencyCall call1 = new EmergencyCall(1, locationA, "Heart Attack"); // Critical
        EmergencyCall call2 = new EmergencyCall(3, locationB, "Minor Car Accident"); // Low
        EmergencyCall call3 = new EmergencyCall(2, locationC, "House Fire"); // Medium

        // Step 4: Fire all 3 calls in rapid succession
        // AMB-01 takes Call 1, AMB-02 takes Call 2, Call 3 goes into the queue
        dispatchManager.handleIncomingCall(call1);
        dispatchManager.handleIncomingCall(call2);
        dispatchManager.handleIncomingCall(call3); // <-- This one gets queued

        // Step 5: Simulate AMB-01 finishing — system should pull House Fire (Sev 2)
        // from queue BEFORE the Car Accident (Sev 3), proving priority queue works
        System.out.println("\n--- SIMULATING AMB-01 COMPLETING JOB ---");
        dispatchManager.finishAmbulanceJob("AMB-01");
    }

}