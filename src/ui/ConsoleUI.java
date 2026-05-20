package ui;

import controller.DispatchManager;
import datastructures.CityGraph;
import datastructures.TriageManager;
import models.Ambulance;
import models.EmergencyCall;
import models.Location;

import java.util.Scanner;

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

        // TODO: The UI Developer should call a method here to pre-populate
        // the graph with a few locations and register a couple of ambulances
        // so the system isn't completely empty when it boots up.

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

    // --- UI METHODS (TODOs for your UI Developer) ---

    private void printMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Report New Emergency Call");
        System.out.println("2. Mark Ambulance Job Complete");
        System.out.println("3. Run Project Demo Scenario");
        System.out.println("4. Exit");
        System.out.print("Enter choice: ");
    }

    private void reportEmergency() {
        // TODO Step 1: Use 'scanner' to ask the user for Call ID, Severity (1-3),
        // Location Name, and Description.
        // TODO Step 2: Create a new Location object from the input string.
        // TODO Step 3: Create a new EmergencyCall object using all the inputs.
        // TODO Step 4: Pass that new object into:
        // dispatchManager.handleIncomingCall(...)
    }

    private void resolveEmergency() {
        // TODO Step 1: Use 'scanner' to ask the user for the String ID of the Ambulance
        // that just finished its job.
        // TODO Step 2: Pass that String ID into:
        // dispatchManager.finishAmbulanceJob(...)
    }

    /**
     * This method automatically executes the exact scenario required by the grading
     * rubric.
     */
    private void runRubricScenario() {
        System.out.println("\n--- RUNNING RUBRIC SCENARIO ---");

        // TODO Step 1: Instantiate 3 Location objects (Location A, Location B, Location
        // C).
        // TODO Step 2: Register 2 Ambulances to the dispatchManager (e.g., AMB-01,
        // AMB-02).
        // TODO Step 3: Create 3 EmergencyCall objects:
        // - Call 1: Heart Attack (Severity 1) at Location A
        // - Call 2: Minor Car Accident (Severity 3) at Location B
        // - Call 3: House Fire (Severity 2) at Location C

        // TODO Step 4: Fire them off to the dispatchManager in rapid succession.
        // - (Because there are 3 calls and only 2 ambulances, one will be forced to
        // wait in the queue).

        // TODO Step 5: Simulate AMB-01 finishing its job by calling resolveEmergency
        // logic for AMB-01.
        // - This will prove to the grader that the system correctly pulls the Fire (Sev
        // 2)
        // - out of the queue before the Car Accident (Sev 3).
    }
}