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
    private CityGraph cityMap;
    private Scanner scanner;

    public ConsoleUI(DispatchManager dispatchManager, CityGraph cityMap) {
        this.dispatchManager = dispatchManager;
        this.cityMap = cityMap;
        this.scanner = new Scanner(System.in);
    }

    // --- MAIN METHOD ---
    public static void main(String[] args) {
        // 1. Boot up the core Data Structures
        CityGraph cityMap = new CityGraph();
        TriageManager triageSystem = new TriageManager();

        // 2. Boot up the Controller and link it to the Data Structures
        DispatchManager manager = new DispatchManager(cityMap, triageSystem);

        // 3. Start the User Interface
        ConsoleUI ui = new ConsoleUI(manager, cityMap);
        ui.start();
    }

    // --- UI LOOP ---
    public void start() {
        boolean running = true;
        System.out.println("=== EMERGENCY MEDICAL DISPATCH SYSTEM ===");

        MalaysiaMapData.seed(cityMap, dispatchManager);
        System.out.println("System ready. KL/Selangor road graph and ambulance fleet loaded.\n");

        DispatchMapUI gui = new DispatchMapUI(dispatchManager, cityMap);
        gui.setVisible(true);

        while (running) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    reportEmergency();
                    break;
                case 2:
                    resolveEmergency();
                    break;
                case 3:
                    runRubricScenario();
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

        Location resolvedLocation = cityMap.getLocation(locationName);
        if (resolvedLocation == null) {
            System.out.println("Unknown location. Please use one of the pre-defined KL/Selangor locations.");
            return;
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

        // Step 1: Use 3 pre-defined KL/Selangor locations
        Location locationA = cityMap.getLocation("KLCC");
        Location locationB = cityMap.getLocation("Bangsar");
        Location locationC = cityMap.getLocation("Kajang");

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
