package ui;

import controller.DispatchManager;
import datastructures.CityGraph;
import models.Ambulance;
import models.Location;
import java.util.Set;

public final class MalaysiaMapData {
    private static final Set<String> HOSPITAL_NAMES = Set.of(
            "Hospital Shah Alam",
            "Hospital Cyberjaya",
            "Hospital Kajang",
            "Hospital Kuala Lumpur",
            "Hospital Puchong",
            "Hospital Cheras",
            "Ampang Hospital");

    private MalaysiaMapData() {
    }

    public static boolean isHospital(Location location) {
        return location != null && HOSPITAL_NAMES.contains(location.getLocationName());
    }

    public static void seed(CityGraph cityMap, DispatchManager dispatchManager) {
        Location setiaAlam = add(cityMap, "Setia Alam", 1.6, 4.8);
        Location shahAlam = add(cityMap, "Shah Alam", 2.2, 5.7);
        Location puchong = add(cityMap, "Puchong", 4.0, 6.8);
        Location cyberjaya = add(cityMap, "Cyberjaya", 4.9, 8.5);
        Location kajang = add(cityMap, "Kajang", 6.7, 8.2);
        Location cheras = add(cityMap, "Cheras", 6.8, 6.7);
        Location sriPetaling = add(cityMap, "Sri Petaling", 5.4, 6.7);
        Location ampang = add(cityMap, "Ampang", 8.3, 5.8);
        Location klcc = add(cityMap, "KLCC", 7.5, 4.8);
        Location bukitBintang = add(cityMap, "Bukit Bintang", 7.0, 5.5);
        Location klSentral = add(cityMap, "KL Sentral", 5.9, 5.2);
        Location bangsarSouth = add(cityMap, "Bangsar South", 5.1, 5.9);
        Location bangsar = add(cityMap, "Bangsar", 5.2, 5.3);
        Location hospitalShahAlam = add(cityMap, "Hospital Shah Alam", 2.3, 5.2);
        Location hospitalCyberjaya = add(cityMap, "Hospital Cyberjaya", 5.2, 8.2);
        Location hospitalKajang = add(cityMap, "Hospital Kajang", 7.1, 8.4);
        Location hospitalKl = add(cityMap, "Hospital Kuala Lumpur", 7.3, 4.2);
        Location hospitalPuchong = add(cityMap, "Hospital Puchong", 3.7, 7.1);
        Location hospitalCheras = add(cityMap, "Hospital Cheras", 7.2, 6.5);
        Location ampangHospital = add(cityMap, "Ampang Hospital", 8.6, 6.1);

        // connecting the nodes with weighted edges
        connect(cityMap, setiaAlam, shahAlam, 6);
        connect(cityMap, shahAlam, puchong, 10);
        connect(cityMap, puchong, cyberjaya, 12);
        connect(cityMap, cyberjaya, kajang, 13);
        connect(cityMap, kajang, cheras, 11);
        connect(cityMap, cheras, sriPetaling, 7);
        connect(cityMap, sriPetaling, puchong, 8);
        connect(cityMap, sriPetaling, bangsarSouth, 6);
        connect(cityMap, bangsarSouth, bangsar, 3);
        connect(cityMap, bangsar, klSentral, 4);
        connect(cityMap, klSentral, bukitBintang, 6);
        connect(cityMap, bukitBintang, klcc, 4);
        connect(cityMap, klcc, ampang, 6);
        connect(cityMap, ampang, cheras, 8);
        connect(cityMap, bangsarSouth, klSentral, 5);
        connect(cityMap, bukitBintang, cheras, 9);
        connect(cityMap, puchong, bangsarSouth, 9);
        connect(cityMap, shahAlam, hospitalShahAlam, 3);
        connect(cityMap, cyberjaya, hospitalCyberjaya, 3);
        connect(cityMap, kajang, hospitalKajang, 2);
        connect(cityMap, klcc, hospitalKl, 4);
        connect(cityMap, puchong, hospitalPuchong, 2);
        connect(cityMap, cheras, hospitalCheras, 2);
        connect(cityMap, ampang, ampangHospital, 2);

        // assigning ambulances to hospitals
        dispatchManager.registerAmbulance(new Ambulance("AMB-01", hospitalShahAlam));
        dispatchManager.registerAmbulance(new Ambulance("AMB-02", hospitalCyberjaya));
        dispatchManager.registerAmbulance(new Ambulance("AMB-03", hospitalKajang));
        dispatchManager.registerAmbulance(new Ambulance("AMB-04", hospitalKl));
        dispatchManager.registerAmbulance(new Ambulance("AMB-05", ampangHospital));
        dispatchManager.registerAmbulance(new Ambulance("AMB-06", hospitalPuchong));
        dispatchManager.registerAmbulance(new Ambulance("AMB-07", hospitalCheras));
    }

    private static Location add(CityGraph cityMap, String name, double x, double y) {
        Location location = new Location(name, x, y);
        cityMap.addLocation(location);
        return location;
    }

    private static void connect(CityGraph cityMap, Location a, Location b, int distanceKm) {
        cityMap.addEdge(a.getLocationName(), b.getLocationName(), distanceKm);
        cityMap.addEdge(b.getLocationName(), a.getLocationName(), distanceKm);
    }
}
