package ui;

import controller.DispatchManager;
import datastructures.CityGraph;
import models.Ambulance;
import models.EmergencyCall;
import models.Location;
import models.Road;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DispatchMapUI extends JFrame {
    private static final int AMBULANCE_AVERAGE_SPEED_KMH = 80;
    private static final String SIMULATED_BUSY_ISSUE = "Simulation: ambulance unavailable";

    private final CityGraph cityMap;
    private final DispatchManager dispatchManager;
    private final MapPanel mapPanel;
    private final JLabel statusLabel;
    private final JLabel etaLabel;
    private final JLabel priorityLabel;
    private final JTextArea ambulanceListArea;
    private final JTextArea historyArea;
    private final List<DispatchRecord> dispatchHistory = new ArrayList<>();
    private final Random random = new Random();
    private boolean allBusySimulationEnabled;

    public DispatchMapUI(DispatchManager dispatchManager, CityGraph cityMap) {
        this.dispatchManager = dispatchManager;
        this.cityMap = cityMap;
        this.mapPanel = new MapPanel();
        this.statusLabel = createReadout("READY");
        this.etaLabel = createReadout("ETA --");
        this.priorityLabel = createReadout("NO PRIORITY");
        this.ambulanceListArea = createListArea();
        this.historyArea = createHistoryArea();

        setTitle("KL/Selangor Emergency Dispatch Map");
        setSize(1240, 760);
        setMinimumSize(new Dimension(1060, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(11, 16, 20));

        add(mapPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBackground(new Color(15, 20, 24));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));

        JLabel titleLabel = new JLabel("DISPATCH");
        titleLabel.setForeground(new Color(236, 241, 237));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(titleLabel);

        JLabel subtitle = new JLabel("Dijkstra route visualizer");
        subtitle.setForeground(new Color(145, 157, 155));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        JTextField placeField = createField("KLCC");
        placeField.setBorder(BorderFactory.createTitledBorder("Known place"));
        panel.add(placeField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField incidentField = createField("Heart Attack");
        incidentField.setBorder(BorderFactory.createTitledBorder("Incident name / medical issue"));
        panel.add(incidentField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton dispatchBtn = createButton("FIND NEAREST AMBULANCE");
        dispatchBtn.addActionListener(e -> dispatch(placeField.getText(), incidentField.getText()));
        panel.add(dispatchBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel zoomControls = new JPanel(new BorderLayout(8, 0));
        zoomControls.setOpaque(false);
        zoomControls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JButton zoomOut = createButton("-");
        JButton zoomIn = createButton("+");
        zoomOut.addActionListener(e -> mapPanel.zoomBy(0.86));
        zoomIn.addActionListener(e -> mapPanel.zoomBy(1.16));
        zoomControls.add(zoomOut, BorderLayout.WEST);
        zoomControls.add(zoomIn, BorderLayout.EAST);
        panel.add(zoomControls);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JCheckBox showWeightsBox = new JCheckBox("Show road distances");
        showWeightsBox.setOpaque(false);
        showWeightsBox.setForeground(new Color(214, 232, 218));
        showWeightsBox.setFont(new Font("SansSerif", Font.BOLD, 12));
        showWeightsBox.setFocusPainted(false);
        showWeightsBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showWeightsBox.addActionListener(e -> mapPanel.setShowRoadWeights(showWeightsBox.isSelected()));
        panel.add(showWeightsBox);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JCheckBox allBusySimulationBox = new JCheckBox("Simulate all ambulances busy");
        allBusySimulationBox.setOpaque(false);
        allBusySimulationBox.setForeground(new Color(214, 232, 218));
        allBusySimulationBox.setFont(new Font("SansSerif", Font.BOLD, 12));
        allBusySimulationBox.setFocusPainted(false);
        allBusySimulationBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        allBusySimulationBox.addActionListener(e -> setAllBusySimulation(allBusySimulationBox.isSelected()));
        panel.add(allBusySimulationBox);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel nearbyTitle = new JLabel("NEAREST AMBULANCES");
        nearbyTitle.setForeground(new Color(236, 241, 237));
        nearbyTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(nearbyTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JScrollPane scrollPane = new JScrollPane(ambulanceListArea);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        scrollPane.setPreferredSize(new Dimension(300, 140));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(35, 47, 52)));
        panel.add(scrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 14)));

        panel.add(priorityLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(statusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(etaLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel historyTitle = new JLabel("DISPATCH HISTORY");
        historyTitle.setForeground(new Color(236, 241, 237));
        historyTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(historyTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JScrollPane historyScrollPane = new JScrollPane(historyArea);
        historyScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        historyScrollPane.setPreferredSize(new Dimension(300, 210));
        historyScrollPane.setBorder(BorderFactory.createLineBorder(new Color(35, 47, 52)));
        panel.add(historyScrollPane);
        panel.add(Box.createVerticalGlue());

        JLabel hint = new JLabel("<html>Mouse wheel zooms.<br>Drag the map to pan.</html>");
        hint.setForeground(new Color(117, 128, 126));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(hint);

        return panel;
    }

    private JTextField createField(String value) {
        JTextField field = new JTextField(value);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBackground(new Color(226, 232, 228));
        field.setForeground(new Color(18, 24, 28));
        return field;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(new Color(0, 184, 94));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return button;
    }

    private JTextArea createListArea() {
        JTextArea area = new JTextArea("Press Find Nearest Ambulance");
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(20, 27, 31));
        area.setForeground(new Color(219, 232, 224));
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JTextArea createHistoryArea() {
        JTextArea area = new JTextArea("No completed dispatch yet.");
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(20, 27, 31));
        area.setForeground(new Color(219, 232, 224));
        area.setFont(new Font("Consolas", Font.PLAIN, 11));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JLabel createReadout(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setOpaque(true);
        label.setBackground(new Color(24, 31, 36));
        label.setForeground(new Color(214, 232, 218));
        label.setFont(new Font("Consolas", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return label;
    }

    private void dispatch(String rawPlace, String rawIncident) {
        String place = rawPlace.trim();
        String incident = rawIncident.trim();

        if (place.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Known Place is required before searching for an ambulance.");
            statusLabel.setText("PLACE REQUIRED");
            etaLabel.setText("ETA --");
            priorityLabel.setText("NO PRIORITY");
            ambulanceListArea.setText("Enter a pre-defined known place first.");
            return;
        }

        Location target = cityMap.getLocation(place);
        if (target == null) {
            JOptionPane.showMessageDialog(this,
                    "Unknown place. Please use one of the pre-defined KL/Selangor locations.");
            statusLabel.setText("UNKNOWN PLACE");
            etaLabel.setText("ETA --");
            priorityLabel.setText("NO PRIORITY");
            ambulanceListArea.setText("No route calculated. The map only accepts pre-defined locations.");
            return;
        }

        mapPanel.focusOnLocation(target, 1.75);
        mapPanel.setTarget(target);

        EmergencyCall call = null;
        if (!incident.isEmpty()) {
            int severity = inferSeverity(incident);
            call = new EmergencyCall(severity, target, incident);
            priorityLabel.setText(priorityText(severity));
        } else {
            priorityLabel.setText("NO INCIDENT: NO QUEUE PRIORITY");
        }

        DispatchChoice choice = findNearestAmbulance(target);
        if (choice == null) {
            if (call != null) {
                dispatchManager.handleIncomingCall(call);
                String queueName = call.getSeverity() == 3 ? "regular queue" : "priority queue";
                JOptionPane.showMessageDialog(this,
                        "No ambulance is available now. Incident added to the " + queueName + ".");
                statusLabel.setText(call.getSeverity() == 3 ? "REGULAR QUEUE" : "PRIORITY QUEUE");
                ambulanceListArea.setText("All ambulances are busy.\n"
                        + "Call queued in " + queueName + ".\n\n"
                        + "Medical issue: " + call.getDescription() + "\n"
                        + "Destination: " + call.getLocation().getLocationName());
            } else {
                JOptionPane.showMessageDialog(this, "No available ambulance can reach this location.");
                statusLabel.setText("NO ROUTE");
            }
            etaLabel.setText("ETA --");
            return;
        }

        HospitalRoute hospitalRoute = findNearestHospitalRoute(target);
        if (hospitalRoute == null) {
            JOptionPane.showMessageDialog(this, "No hospital route is available from this emergency location.");
            statusLabel.setText("NO HOSPITAL ROUTE");
            etaLabel.setText("ETA --");
            return;
        }

        EmergencyCall activeCall = call != null ? call : new EmergencyCall(3, target, "Unspecified emergency");
        choice = choice.withHospitalRoute(hospitalRoute, activeCall.getDescription());
        choice.ambulance.dispatchTo(activeCall);

        int emergencyLaneEta = estimatedMinutes(choice.distanceKm + choice.hospitalDistanceKm);
        statusLabel.setText("SCANNING KNOWN AREA");
        etaLabel.setText("BEST " + choice.ambulance.getAmbulanceId() + " | TOTAL " + emergencyLaneEta + " min");
        mapPanel.scanAndDispatch(choice);
    }

    private int inferSeverity(String incident) {
        String text = incident.toLowerCase();
        if (text.contains("heart") || text.contains("cardiac") || text.contains("stroke")
                || text.contains("unconscious") || text.contains("not breathing")) {
            return 1;
        }
        if (text.contains("fire") || text.contains("burn") || text.contains("bleeding")
                || text.contains("fracture") || text.contains("accident")) {
            return 2;
        }
        return 3;
    }

    private String priorityText(int severity) {
        return switch (severity) {
            case 1 -> "SEVERITY 1: PRIORITY QUEUE";
            case 2 -> "SEVERITY 2: PRIORITY QUEUE";
            default -> "SEVERITY 3: STANDARD QUEUE";
        };
    }

    private void setAllBusySimulation(boolean enabled) {
        allBusySimulationEnabled = enabled;
        if (enabled) {
            int simulatedCount = markAvailableAmbulancesBusy();
            mapPanel.startBusySimulation();
            statusLabel.setText("SIMULATION: ALL BUSY");
            etaLabel.setText("NON-URGENT CALLS -> REGULAR QUEUE");
            ambulanceListArea.setText("Simulation enabled.\n"
                    + simulatedCount + " available ambulances are moving between hospitals and calls.\n\n"
                    + "Enter a non-urgent issue, then press Find Nearest Ambulance.");
        } else {
            mapPanel.stopBusySimulation();
            int releasedCount = releaseSimulatedBusyAmbulances();
            statusLabel.setText("READY");
            etaLabel.setText("ETA --");
            ambulanceListArea.setText("Simulation disabled.\n"
                    + releasedCount + " simulated ambulances returned to available status.");
        }
        mapPanel.repaint();
    }

    private int markAvailableAmbulancesBusy() {
        int count = 0;
        for (Ambulance ambulance : dispatchManager.getFleet()) {
            if (ambulance.isAvailable()) {
                EmergencyCall simulatedCall = new EmergencyCall(3, ambulance.getCurrentLocation(), SIMULATED_BUSY_ISSUE);
                if (ambulance.dispatchTo(simulatedCall)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int releaseSimulatedBusyAmbulances() {
        int count = 0;
        for (Ambulance ambulance : dispatchManager.getFleet()) {
            EmergencyCall currentCall = ambulance.getCurrentCall();
            if (currentCall != null && SIMULATED_BUSY_ISSUE.equals(currentCall.getDescription())) {
                ambulance.completeCall();
                count++;
            }
        }
        return count;
    }

    private DispatchChoice findNearestAmbulance(Location target) {
        DispatchChoice best = null;
        List<DispatchChoice> candidates = new ArrayList<>();

        for (Ambulance amb : dispatchManager.getFleet()) {
            if (!amb.isAvailable()) {
                continue;
            }

            int eta = cityMap.calculateEta(amb.getCurrentLocation(), target);
            List<Location> path = cityMap.findShortestPath(
                    amb.getCurrentLocation().getLocationName(),
                    target.getLocationName());

            if (eta != Integer.MAX_VALUE && !path.isEmpty()) {
                DispatchChoice choice = new DispatchChoice(amb, target, path, routeDistanceKm(path));
                candidates.add(choice);
            }
        }

        candidates.sort(Comparator.comparingDouble(candidate -> candidate.distanceKm));
        best = candidates.isEmpty() ? null : candidates.get(0);
        mapPanel.setCandidates(candidates);
        updateAmbulanceList(candidates);
        return best;
    }

    private HospitalRoute findNearestHospitalRoute(Location emergencyLocation) {
        HospitalRoute best = null;

        for (Location hospital : cityMap.getNodes().values()) {
            if (!MalaysiaMapData.isHospital(hospital)) {
                continue;
            }

            List<Location> path = cityMap.findShortestPath(
                    emergencyLocation.getLocationName(),
                    hospital.getLocationName());
            if (path.isEmpty()) {
                continue;
            }

            HospitalRoute route = new HospitalRoute(hospital, path, routeDistanceKm(path));
            if (best == null || route.distanceKm < best.distanceKm) {
                best = route;
            }
        }

        return best;
    }

    private void updateAmbulanceList(List<DispatchChoice> candidates) {
        if (candidates.isEmpty()) {
            ambulanceListArea.setText("No available ambulance can reach this place.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            DispatchChoice choice = candidates.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(choice.ambulance.getAmbulanceId())
                    .append(" - ")
                    .append(formatKm(choice.distanceKm))
                    .append(" km, ETA ")
                    .append(estimatedMinutes(choice.distanceKm))
                    .append(" min\n   from ")
                    .append(choice.ambulance.getCurrentLocation().getLocationName())
                    .append('\n');
        }
        ambulanceListArea.setText(builder.toString());
        ambulanceListArea.setCaretPosition(0);
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

    private int estimatedMinutes(double distanceKm) {
        return Math.max(1, (int) Math.round((distanceKm / AMBULANCE_AVERAGE_SPEED_KMH) * 60 * 0.85));
    }

    private String formatKm(double distanceKm) {
        return String.format("%.1f", distanceKm);
    }

    private void addDispatchHistory(DispatchChoice choice) {
        double totalDistanceKm = choice.distanceKm + choice.hospitalDistanceKm;
        DispatchRecord record = new DispatchRecord(
                choice.ambulance.getAmbulanceId(),
                choice.medicalIssue,
                choice.target.getLocationName(),
                choice.hospital.getLocationName(),
                estimatedMinutes(totalDistanceKm),
                totalDistanceKm);
        dispatchHistory.add(0, record);
        updateHistoryArea();
    }

    private void updateHistoryArea() {
        if (dispatchHistory.isEmpty()) {
            historyArea.setText("No completed dispatch yet.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dispatchHistory.size(); i++) {
            DispatchRecord record = dispatchHistory.get(i);
            builder.append(i + 1)
                    .append(". Ambulance: ")
                    .append(record.ambulanceId)
                    .append("\n   Medical Issue: ")
                    .append(record.medicalIssue)
                    .append("\n   Last Destination: ")
                    .append(record.lastDestination)
                    .append("\n   Arrived Hospital: ")
                    .append(record.arrivedHospital)
                    .append("\n   Time Taken: ")
                    .append(record.minutesTaken)
                    .append(" min")
                    .append("\n   Distance Taken: ")
                    .append(formatKm(record.distanceTakenKm))
                    .append(" km\n\n");
        }
        historyArea.setText(builder.toString());
        historyArea.setCaretPosition(0);
    }

    private static final class DispatchChoice {
        private final Ambulance ambulance;
        private final Location target;
        private final List<Location> path;
        private final double distanceKm;
        private final Location hospital;
        private final List<Location> hospitalPath;
        private final double hospitalDistanceKm;
        private final String medicalIssue;

        private DispatchChoice(Ambulance ambulance, Location target, List<Location> path, double distanceKm) {
            this(ambulance, target, path, distanceKm, null, Collections.emptyList(), 0, "Unspecified emergency");
        }

        private DispatchChoice(Ambulance ambulance, Location target, List<Location> path, double distanceKm,
                Location hospital, List<Location> hospitalPath, double hospitalDistanceKm, String medicalIssue) {
            this.ambulance = ambulance;
            this.target = target;
            this.path = path;
            this.distanceKm = distanceKm;
            this.hospital = hospital;
            this.hospitalPath = hospitalPath;
            this.hospitalDistanceKm = hospitalDistanceKm;
            this.medicalIssue = medicalIssue;
        }

        private DispatchChoice withHospitalRoute(HospitalRoute hospitalRoute, String medicalIssue) {
            return new DispatchChoice(ambulance, target, path, distanceKm,
                    hospitalRoute.hospital, hospitalRoute.path, hospitalRoute.distanceKm, medicalIssue);
        }
    }

    private static final class DispatchRecord {
        private final String ambulanceId;
        private final String medicalIssue;
        private final String lastDestination;
        private final String arrivedHospital;
        private final int minutesTaken;
        private final double distanceTakenKm;

        private DispatchRecord(String ambulanceId, String medicalIssue, String lastDestination,
                String arrivedHospital, int minutesTaken, double distanceTakenKm) {
            this.ambulanceId = ambulanceId;
            this.medicalIssue = medicalIssue;
            this.lastDestination = lastDestination;
            this.arrivedHospital = arrivedHospital;
            this.minutesTaken = minutesTaken;
            this.distanceTakenKm = distanceTakenKm;
        }
    }

    private static final class HospitalRoute {
        private final Location hospital;
        private final List<Location> path;
        private final double distanceKm;

        private HospitalRoute(Location hospital, List<Location> path, double distanceKm) {
            this.hospital = hospital;
            this.path = path;
            this.distanceKm = distanceKm;
        }
    }

    private class MapPanel extends JPanel {
        private static final double SCALE = 92.0;
        private static final int SCAN_TICKS = 78;

        private double zoomFactor = 1.0;
        private double xOffset = 70;
        private double yOffset = -320;
        private Point dragStartPoint;
        private Timer timer;
        private Timer busySimulationTimer;
        private int scanTick;
        private Location target;
        private DispatchChoice activeChoice;
        private List<DispatchChoice> candidates = Collections.emptyList();
        private boolean transportingToHospital;
        private boolean showRoadWeights;
        private double animX;
        private double animY;
        private int pathIndex;
        private double segmentProgress;
        private final Map<String, Point> ambulanceOffsets = new HashMap<>();
        private final Map<Ambulance, BusySimulationRoute> busySimulationRoutes = new HashMap<>();

        private class BusySimulationRoute {
            private final Location hospital;
            private final Location destination;
            private final List<Location> path;
            private boolean outbound = true;
            private int routeIndex;
            private double routeProgress;
            private double currentX;
            private double currentY;
            private Location endpoint;

            private BusySimulationRoute(Location hospital, Location destination, List<Location> path) {
                this.hospital = hospital;
                this.destination = destination;
                this.path = path;
                this.currentX = hospital.getXCoordinate();
                this.currentY = hospital.getYCoordinate();
                this.endpoint = hospital;
            }

            private void step() {
                endpoint = null;
                if (path.size() < 2) {
                    currentX = hospital.getXCoordinate();
                    currentY = hospital.getYCoordinate();
                    endpoint = hospital;
                    return;
                }

                Location start = segmentStart();
                Location end = segmentEnd();
                int segmentMinutes = Math.max(1, (int) Math.round(roadDistanceKm(start, end)));
                routeProgress += Math.max(0.008, 0.11 / segmentMinutes);

                if (routeProgress >= 1.0) {
                    currentX = end.getXCoordinate();
                    currentY = end.getYCoordinate();
                    routeIndex++;
                    routeProgress = 0;

                    if (routeIndex >= path.size() - 1) {
                        endpoint = outbound ? destination : hospital;
                        outbound = !outbound;
                        routeIndex = 0;
                    }
                    return;
                }

                currentX = start.getXCoordinate() + (end.getXCoordinate() - start.getXCoordinate()) * routeProgress;
                currentY = start.getYCoordinate() + (end.getYCoordinate() - start.getYCoordinate()) * routeProgress;
            }

            private Location segmentStart() {
                return outbound ? path.get(routeIndex) : path.get(path.size() - 1 - routeIndex);
            }

            private Location segmentEnd() {
                return outbound ? path.get(routeIndex + 1) : path.get(path.size() - 2 - routeIndex);
            }

            private Location currentEndpoint() {
                return endpoint;
            }
        }

        private MapPanel() {
            setBackground(new Color(11, 16, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStartPoint = e.getPoint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStartPoint != null) {
                        xOffset += (e.getX() - dragStartPoint.x) / zoomFactor;
                        yOffset += (e.getY() - dragStartPoint.y) / zoomFactor;
                        dragStartPoint = e.getPoint();
                        repaint();
                    }
                }
            });

            addMouseWheelListener(e -> zoomBy(e.getWheelRotation() < 0 ? 1.12 : 0.89));
        }

        private void zoomBy(double factor) {
            zoomFactor = Math.max(0.45, Math.min(3.2, zoomFactor * factor));
            repaint();
        }

        private void focusOnLocation(Location loc, double zoom) {
            zoomFactor = zoom;
            xOffset = (getWidth() / 2.0) / zoomFactor - worldX(loc);
            yOffset = (getHeight() / 2.0) / zoomFactor - worldY(loc);
            repaint();
        }

        private void setTarget(Location target) {
            this.target = target;
            repaint();
        }

        private void setCandidates(List<DispatchChoice> candidates) {
            this.candidates = candidates;
            repaint();
        }

        private void setShowRoadWeights(boolean showRoadWeights) {
            this.showRoadWeights = showRoadWeights;
            repaint();
        }

        private void startBusySimulation() {
            busySimulationRoutes.clear();
            for (Ambulance ambulance : dispatchManager.getFleet()) {
                EmergencyCall currentCall = ambulance.getCurrentCall();
                if (currentCall != null && SIMULATED_BUSY_ISSUE.equals(currentCall.getDescription())) {
                    BusySimulationRoute route = createBusySimulationRoute(ambulance);
                    if (route != null) {
                        busySimulationRoutes.put(ambulance, route);
                    }
                }
            }

            if (busySimulationTimer != null && busySimulationTimer.isRunning()) {
                busySimulationTimer.stop();
            }
            busySimulationTimer = new Timer(16, e -> {
                stepBusySimulation();
                repaint();
            });
            busySimulationTimer.start();
            repaint();
        }

        private void stopBusySimulation() {
            if (busySimulationTimer != null) {
                busySimulationTimer.stop();
            }
            for (Map.Entry<Ambulance, BusySimulationRoute> entry : busySimulationRoutes.entrySet()) {
                entry.getKey().setCurrentLocation(entry.getValue().hospital);
            }
            busySimulationRoutes.clear();
            repaint();
        }

        private BusySimulationRoute createBusySimulationRoute(Ambulance ambulance) {
            Location hospital = nearestHospital(ambulance.getCurrentLocation());
            Location destination = randomDestinationForHospital(ambulance, hospital);
            if (hospital == null || destination == null) {
                return null;
            }

            List<Location> path = cityMap.findShortestPath(hospital.getLocationName(), destination.getLocationName());
            if (path.size() < 2) {
                return null;
            }
            ambulance.setCurrentLocation(hospital);
            return new BusySimulationRoute(hospital, destination, path);
        }

        private Location nearestHospital(Location from) {
            Location bestHospital = null;
            double bestDistance = Double.MAX_VALUE;
            for (Location hospital : cityMap.getNodes().values()) {
                if (!MalaysiaMapData.isHospital(hospital)) {
                    continue;
                }
                if (from.equals(hospital)) {
                    return hospital;
                }
                List<Location> path = cityMap.findShortestPath(from.getLocationName(), hospital.getLocationName());
                if (path.isEmpty()) {
                    continue;
                }
                double distance = routeDistanceKm(path);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestHospital = hospital;
                }
            }
            return bestHospital;
        }

        private Location randomDestinationForHospital(Ambulance ambulance, Location hospital) {
            List<Location> candidates = new ArrayList<>();
            for (Location location : cityMap.getNodes().values()) {
                if (MalaysiaMapData.isHospital(location) || location.equals(hospital)
                        || isExcludedSimulationDestination(ambulance, location)) {
                    continue;
                }
                if (hospital.equals(nearestHospital(location))) {
                    candidates.add(location);
                }
            }
            if (candidates.isEmpty()) {
                return randomReachableDestination(ambulance, hospital);
            }
            return candidates.get(random.nextInt(candidates.size()));
        }

        private Location randomReachableDestination(Ambulance ambulance, Location hospital) {
            List<Location> candidates = new ArrayList<>();
            for (Location location : cityMap.getNodes().values()) {
                if (MalaysiaMapData.isHospital(location) || location.equals(hospital)
                        || isExcludedSimulationDestination(ambulance, location)) {
                    continue;
                }
                if (!cityMap.findShortestPath(hospital.getLocationName(), location.getLocationName()).isEmpty()) {
                    candidates.add(location);
                }
            }
            if (candidates.isEmpty()) {
                return null;
            }
            return candidates.get(random.nextInt(candidates.size()));
        }

        private boolean isExcludedSimulationDestination(Ambulance ambulance, Location location) {
            if (!"AMB-07".equals(ambulance.getAmbulanceId())) {
                return false;
            }
            String name = location.getLocationName();
            return "Bukit Bintang".equals(name) || "Cheras".equals(name);
        }

        private void stepBusySimulation() {
            for (Map.Entry<Ambulance, BusySimulationRoute> entry : busySimulationRoutes.entrySet()) {
                BusySimulationRoute route = entry.getValue();
                route.step();
                Location endpoint = route.currentEndpoint();
                if (endpoint != null) {
                    entry.getKey().setCurrentLocation(endpoint);
                }
            }
        }

        private void scanAndDispatch(DispatchChoice choice) {
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
            activeChoice = choice;
            transportingToHospital = false;
            scanTick = 0;
            pathIndex = 0;
            segmentProgress = 0;
            animX = choice.path.get(0).getXCoordinate();
            animY = choice.path.get(0).getYCoordinate();

            timer = new Timer(16, e -> {
                if (scanTick < SCAN_TICKS) {
                    scanTick++;
                    if (scanTick == SCAN_TICKS) {
                        statusLabel.setText("ROUTE LOCKED: " + activeChoice.ambulance.getAmbulanceId());
                    }
                    repaint();
                    return;
                }
                stepAmbulance();
                repaint();
            });
            timer.start();
        }

        private void stepAmbulance() {
            List<Location> activePath = getActivePath();
            if (activeChoice == null || pathIndex >= activePath.size() - 1) {
                finishAnimation();
                return;
            }

            Location start = activePath.get(pathIndex);
            Location end = activePath.get(pathIndex + 1);
            int segmentMinutes = Math.max(1, (int) Math.round(roadDistanceKm(start, end)));
            double step = Math.max(0.012, 0.16 / segmentMinutes);
            segmentProgress += step;

            if (segmentProgress >= 1.0) {
                animX = end.getXCoordinate();
                animY = end.getYCoordinate();
                pathIndex++;
                segmentProgress = 0;
            } else {
                animX = start.getXCoordinate() + (end.getXCoordinate() - start.getXCoordinate()) * segmentProgress;
                animY = start.getYCoordinate() + (end.getYCoordinate() - start.getYCoordinate()) * segmentProgress;
            }
        }

        private void finishAnimation() {
            if (activeChoice != null && !transportingToHospital) {
                activeChoice.ambulance.setCurrentLocation(activeChoice.target);
                transportingToHospital = true;
                pathIndex = 0;
                segmentProgress = 0;
                animX = activeChoice.hospitalPath.get(0).getXCoordinate();
                animY = activeChoice.hospitalPath.get(0).getYCoordinate();
                statusLabel.setText("TRANSPORTING TO " + activeChoice.hospital.getLocationName());
                etaLabel.setText("HOSPITAL LEG " + estimatedMinutes(activeChoice.hospitalDistanceKm) + " min");
                return;
            }

            if (timer != null) {
                timer.stop();
            }
            activeChoice.ambulance.setCurrentLocation(activeChoice.hospital);
            activeChoice.ambulance.completeCall();
            addDispatchHistory(activeChoice);
            statusLabel.setText("AVAILABLE: " + activeChoice.ambulance.getAmbulanceId());
            etaLabel.setText("COMPLETED VIA " + activeChoice.hospital.getLocationName());
            activeChoice = null;
            transportingToHospital = false;
            ambulanceListArea.setText("Mission complete. Press Find Nearest Ambulance for the next call.");
            repaint();
        }

        private List<Location> getActivePath() {
            if (activeChoice == null) {
                return Collections.emptyList();
            }
            return transportingToHospital ? activeChoice.hospitalPath : activeChoice.path;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawBackdrop(g2);

            AffineTransform previous = g2.getTransform();
            g2.scale(zoomFactor, zoomFactor);
            g2.translate(xOffset, yOffset);

            drawMetroArea(g2);
            drawRoads(g2);
            if (showRoadWeights) {
                drawAllRoadWeights(g2);
            }
            drawDijkstraRoutes(g2);
            drawLocations(g2);
            drawAmbulances(g2);
            drawTarget(g2);
            drawMovingAmbulance(g2);

            g2.setTransform(previous);
            drawHud(g2);
            g2.dispose();
        }

        private void drawBackdrop(Graphics2D g2) {
            GradientPaint paint = new GradientPaint(0, 0, new Color(8, 13, 17), 0, getHeight(), new Color(20, 28, 30));
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 9));
            for (int x = 0; x < getWidth(); x += 44) {
                g2.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += 44) {
                g2.drawLine(0, y, getWidth(), y);
            }
        }

        private void drawMetroArea(Graphics2D g2) {
            Path2D klSelangor = new Path2D.Double();
            klSelangor.moveTo(worldX(1.0), worldY(4.2));
            klSelangor.curveTo(worldX(2.2), worldY(3.7), worldX(4.5), worldY(3.6), worldX(6.0), worldY(4.4));
            klSelangor.curveTo(worldX(8.0), worldY(4.8), worldX(9.0), worldY(6.6), worldX(8.4), worldY(8.0));
            klSelangor.curveTo(worldX(7.6), worldY(9.9), worldX(5.7), worldY(10.5), worldX(4.0), worldY(9.1));
            klSelangor.curveTo(worldX(2.3), worldY(8.8), worldX(1.1), worldY(6.8), worldX(1.0), worldY(4.2));
            klSelangor.closePath();

            g2.setColor(new Color(32, 70, 61, 130));
            g2.fill(klSelangor);
            g2.setColor(new Color(95, 137, 119, 95));
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(klSelangor);

            g2.setColor(new Color(52, 83, 91, 115));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine((int) worldX(3.0), (int) worldY(6.7), (int) worldX(8.6), (int) worldY(6.0));
            g2.drawLine((int) worldX(4.4), (int) worldY(4.1), (int) worldX(5.2), (int) worldY(9.3));
        }

        private void drawRoads(Graphics2D g2) {
            Set<String> drawnRoads = new HashSet<>();

            for (Map.Entry<Location, List<Road>> entry : cityMap.getAdjacencyList().entrySet()) {
                Location from = entry.getKey();
                for (Road road : entry.getValue()) {
                    Location to = road.getTo();
                    String roadKey = roadKey(from, to);
                    if (!drawnRoads.add(roadKey)) {
                        continue;
                    }

                    int traffic = road.getTravelTime();
                    if (traffic >= 12) {
                        g2.setColor(new Color(214, 82, 72, 145));
                    } else if (traffic >= 8) {
                        g2.setColor(new Color(235, 187, 72, 135));
                    } else {
                        g2.setColor(new Color(46, 214, 130, 145));
                    }
                    g2.setStroke(new BasicStroke(traffic >= 10 ? 2.5f : 1.6f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    g2.drawLine((int) worldX(from), (int) worldY(from), (int) worldX(to), (int) worldY(to));
                }
            }
        }

        private String roadKey(Location from, Location to) {
            String fromName = from.getLocationName();
            String toName = to.getLocationName();
            if (fromName.compareTo(toName) <= 0) {
                return fromName + "|" + toName;
            }
            return toName + "|" + fromName;
        }

        private void drawDijkstraRoutes(Graphics2D g2) {
            if (scanTick > 0 && scanTick < SCAN_TICKS) {
                for (DispatchChoice candidate : candidates) {
                    drawPath(g2, candidate.path, new Color(31, 177, 255, 80), 3.0f);
                }
            }

            if (activeChoice != null && scanTick >= SCAN_TICKS) {
                if (transportingToHospital) {
                    drawPath(g2, activeChoice.path, new Color(0, 245, 141, 90), 3.2f);
                    drawPath(g2, activeChoice.hospitalPath, new Color(31, 177, 255, 230), 5.2f);
                    if (!showRoadWeights) {
                        drawPathWeights(g2, activeChoice.hospitalPath);
                    }
                } else {
                    drawPath(g2, activeChoice.path, new Color(0, 245, 141, 230), 5.2f);
                    if (!showRoadWeights) {
                        drawPathWeights(g2, activeChoice.path);
                    }
                }
            }
        }

        private void drawPath(Graphics2D g2, List<Location> path, Color color, float width) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < path.size() - 1; i++) {
                g2.drawLine((int) worldX(path.get(i)), (int) worldY(path.get(i)),
                        (int) worldX(path.get(i + 1)), (int) worldY(path.get(i + 1)));
            }
        }

        private void drawPathWeights(Graphics2D g2, List<Location> path) {
            g2.setFont(new Font("Consolas", Font.BOLD, 10));
            FontMetrics metrics = g2.getFontMetrics();

            for (int i = 0; i < path.size() - 1; i++) {
                Location from = path.get(i);
                Location to = path.get(i + 1);
                String label = formatKm(roadDistanceKm(from, to)) + " km";
                drawRoadWeightLabel(g2, metrics, from, to, label, 13);
            }
        }

        private void drawAllRoadWeights(Graphics2D g2) {
            Set<String> drawnRoads = new HashSet<>();
            g2.setFont(new Font("Consolas", Font.BOLD, 10));
            FontMetrics metrics = g2.getFontMetrics();

            for (Map.Entry<Location, List<Road>> entry : cityMap.getAdjacencyList().entrySet()) {
                Location from = entry.getKey();
                for (Road road : entry.getValue()) {
                    Location to = road.getTo();
                    if (!drawnRoads.add(roadKey(from, to))) {
                        continue;
                    }
                    drawRoadWeightLabel(g2, metrics, from, to, formatKm(road.getTravelTime()) + " km", 15);
                }
            }
        }

        private void drawRoadWeightLabel(Graphics2D g2, FontMetrics metrics, Location from, Location to, String label,
                int perpendicularOffset) {
            int x = (int) ((worldX(from) + worldX(to)) / 2);
            int y = (int) ((worldY(from) + worldY(to)) / 2);
            int width = metrics.stringWidth(label) + 8;
            double dx = worldX(to) - worldX(from);
            double dy = worldY(to) - worldY(from);
            double length = Math.max(1, Math.hypot(dx, dy));
            int xShift = (int) Math.round((-dy / length) * perpendicularOffset);
            int yShift = (int) Math.round((dx / length) * perpendicularOffset);
            x += xShift;
            y += yShift;

            g2.setColor(new Color(5, 10, 12, 210));
            g2.fillRoundRect(x - width / 2, y - 10, width, 16, 6, 6);
            g2.setColor(new Color(235, 255, 244));
            g2.drawString(label, x - width / 2 + 4, y + 2);
        }

        private void drawLocations(Graphics2D g2) {
            for (Location loc : cityMap.getNodes().values()) {
                double x = worldX(loc);
                double y = worldY(loc);
                if (MalaysiaMapData.isHospital(loc)) {
                    drawHospitalIcon(g2, x, y, loc.getLocationName());
                    continue;
                }

                g2.setColor(new Color(12, 16, 18));
                g2.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
                g2.setColor(new Color(214, 226, 219));
                g2.fill(new Ellipse2D.Double(x - 2.5, y - 2.5, 5, 5));
                if (loc.equals(target)) {
                    continue;
                }
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(new Color(229, 235, 231, 205));
                Point labelOffset = labelOffset(loc.getLocationName());
                g2.drawString(loc.getLocationName(), (float) x + labelOffset.x, (float) y + labelOffset.y);
            }
        }

        private void drawHospitalIcon(Graphics2D g2, double x, double y, String name) {
            g2.setColor(new Color(5, 10, 12, 185));
            g2.fillRoundRect((int) x - 11, (int) y - 11, 22, 22, 5, 5);
            g2.setColor(new Color(38, 137, 255));
            g2.fillRoundRect((int) x - 8, (int) y - 8, 16, 16, 4, 4);
            g2.setColor(Color.WHITE);
            g2.fillRect((int) x - 2, (int) y - 6, 4, 12);
            g2.fillRect((int) x - 6, (int) y - 2, 12, 4);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(new Color(210, 229, 255));
            Point labelOffset = labelOffset(name);
            g2.drawString(name, (float) x + labelOffset.x, (float) y + labelOffset.y);
        }

        private void drawAmbulances(Graphics2D g2) {
            for (Ambulance ambulance : dispatchManager.getFleet()) {
                if (activeChoice != null && ambulance == activeChoice.ambulance && scanTick >= SCAN_TICKS) {
                    continue;
                }
                Location loc = ambulance.getCurrentLocation();
                Point offset = ambulanceOffsets.computeIfAbsent(ambulance.getAmbulanceId(), this::stableOffset);
                BusySimulationRoute simulationRoute = busySimulationRoutes.get(ambulance);
                double x = simulationRoute == null ? worldX(loc) : worldX(simulationRoute.currentX);
                double y = simulationRoute == null ? worldY(loc) : worldY(simulationRoute.currentY);
                x += offset.x;
                y += offset.y;
                boolean candidate = isCandidate(ambulance);

                if (candidate && scanTick > 0 && scanTick < SCAN_TICKS) {
                    float pulse = (scanTick % 34) / 34.0f;
                    g2.setColor(new Color(0, 210, 134, 70));
                    g2.setStroke(new BasicStroke(1.4f));
                    double radius = 13 + pulse * 25;
                    g2.draw(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
                }

                drawAmbulanceIcon(g2, x, y, candidate ? new Color(0, 230, 130) : new Color(211, 71, 74),
                        ambulance.getAmbulanceId());
            }
        }

        private void drawMovingAmbulance(Graphics2D g2) {
            if (activeChoice == null || scanTick < SCAN_TICKS) {
                return;
            }
            drawAmbulanceIcon(g2, worldX(animX), worldY(animY), new Color(0, 255, 146),
                    activeChoice.ambulance.getAmbulanceId());
        }

        private void drawAmbulanceIcon(Graphics2D g2, double x, double y, Color color, String id) {
            g2.setColor(new Color(0, 0, 0, 135));
            g2.fillRoundRect((int) x - 13, (int) y - 9, 26, 18, 6, 6);
            g2.setColor(color);
            g2.fillRoundRect((int) x - 10, (int) y - 6, 20, 12, 5, 5);
            g2.setColor(Color.WHITE);
            g2.fillRect((int) x - 2, (int) y - 5, 4, 10);
            g2.fillRect((int) x - 5, (int) y - 2, 10, 4);
            g2.setFont(new Font("Consolas", Font.BOLD, 10));
            g2.drawString(id, (float) x - 16, (float) y - 13);
        }

        private void drawTarget(Graphics2D g2) {
            if (target == null) {
                return;
            }
            double x = worldX(target);
            double y = worldY(target);

            Polygon pin = new Polygon();
            pin.addPoint((int) x, (int) y + 18);
            pin.addPoint((int) x - 11, (int) y - 4);
            pin.addPoint((int) x + 11, (int) y - 4);
            g2.setColor(new Color(255, 58, 74));
            g2.fill(pin);
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(x - 4, y - 9, 8, 8));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            Point labelOffset = targetLabelOffset(target.getLocationName());
            g2.drawString(target.getLocationName(), (float) x + labelOffset.x, (float) y + labelOffset.y);
        }

        private Point labelOffset(String name) {
            return switch (name) {
                case "Setia Alam" -> new Point(18, -18);
                case "Shah Alam" -> new Point(18, 24);
                case "Puchong" -> new Point(-72, 6);
                case "Cyberjaya" -> new Point(18, 24);
                case "Kajang" -> new Point(18, -18);
                case "Cheras" -> new Point(18, 22);
                case "Sri Petaling" -> new Point(18, -18);
                case "Ampang" -> new Point(22, 4);
                case "Bangsar" -> new Point(-58, -16);
                case "Bangsar South" -> new Point(18, 24);
                case "KL Sentral" -> new Point(30, -10);
                case "Bukit Bintang" -> new Point(16, 18);
                case "KLCC" -> new Point(28, -18);
                case "Hospital Shah Alam" -> new Point(26, -18);
                case "Hospital Cyberjaya" -> new Point(28, 24);
                case "Hospital Kajang" -> new Point(28, -18);
                case "Hospital Kuala Lumpur" -> new Point(36, -28);
                case "Hospital Puchong" -> new Point(-108, 22);
                case "Hospital Cheras" -> new Point(26, -18);
                case "Ampang Hospital" -> new Point(28, 26);
                default -> new Point(18, -16);
            };
        }

        private Point targetLabelOffset(String name) {
            return switch (name) {
                case "Bangsar South" -> new Point(22, 24);
                case "Puchong", "Cyberjaya", "Cheras" -> new Point(28, 28);
                default -> new Point(28, 10);
            };
        }

        private void drawHud(Graphics2D g2) {
            String text = "KL/Selangor EMS Road Network  |  green: light traffic  amber: moderate  red: heavy";
            g2.setFont(new Font("Consolas", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int width = fm.stringWidth(text) + 22;
            g2.setColor(new Color(8, 13, 17, 185));
            g2.fillRoundRect(14, 14, width, 34, 8, 8);
            g2.setColor(new Color(219, 232, 224));
            g2.drawString(text, 25, 36);
        }

        private boolean isCandidate(Ambulance ambulance) {
            for (DispatchChoice candidate : candidates) {
                if (candidate.ambulance == ambulance) {
                    return true;
                }
            }
            return false;
        }

        private Point stableOffset(String id) {
            int hash = Math.abs(id.hashCode());
            return new Point((hash % 17) - 8, ((hash / 17) % 17) - 8);
        }

        private double worldX(Location loc) {
            return worldX(loc.getXCoordinate());
        }

        private double worldY(Location loc) {
            return worldY(loc.getYCoordinate());
        }

        private double worldX(double x) {
            return x * SCALE;
        }

        private double worldY(double y) {
            return y * SCALE;
        }
    }
}
