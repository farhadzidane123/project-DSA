# 1. Group Members
- Mohammad Farhad Zidane Bin Mohammad Firdaus (24001142) 
- Nik Ahmad Afham Bin Nik Mohd Asri (24002053) 
- Abu Hanifah Bin Faisal (24001268) 
- Ariff Ikhwan bin Amrul Ezwan (24001425) 
- Muhammad Afifi Furqan Bin Azliniza (24001181) 

# 2. Problem Statement
During emergencies, dispatchers waste critical time manually locating available ambulances and estimating their routes. [cite: 17] This project implements an integrated system that utilizes graph-based pathfinding, severity-based prioritization, and fair handling of non-urgent calls that can significantly reduce response times and save lives. 

# 3. Approach
1. **Ingest and Categorize**: Incoming calls are instantly tagged with a medical severity level. 
2. **Sort and Triage**: Critical emergencies enter a dynamic priority tier, sorting themselves so the most life-threatening cases are processed first. 
3. **Route Optimization**: The system queries the city's network topology via a graph representation, running pathfinding computations to locate the nearest physical resource and calculate an exact estimated time of arrival (ETA). 
4. **Fallback Handling**: When field assets are depleted, non-urgent calls wait in a basic line, ensuring high-priority emergencies can continue to jump the queue dynamically until all life-threatening alerts are resolved. 

# 4. File Structure
- `src/`
  - `controller/`
    - `DispatchManager.java`:
  - `datastructures/`
    - `CityGraph.java` Graph class with Dijkstra method
    - `EmergencyPriorityQueue.java` Priority Queue class / heap
    - `StandardEmergencyQueue.java` Queue class / deque
    - `TriageManager.java`
  - `models/`
    - `Ambulance.java`
    - `AmbulanceStatus.java`
    - `EmergencyCall.java`
    - `Location.java`
    - `Road.java`
  - `ui/`
    - `ConsoleUI.java` Main simulation loop
    - `DispatchMapUI.java`
    - `MalaysiaMapData.java`

# 5. How to Execute the Program
To run the simulation, execute the `ConsoleUI.java` file. You can compile the project and run it via the command line or using an IDE. 

**Using the Command Line:**
1. Navigate to the project directory where the `src/` folder is located.
2. Compile the Java files using `javac`:
   ```bash
   javac -d bin src/**/*.java
