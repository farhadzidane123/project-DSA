package ui;

import controller.DispatchManager;
import datastructures.CityGraph;
import models.Ambulance;
import models.Location;
import java.util.Set;

public final class MalaysiaMapData {
    private static final Set<String> HOSPITAL_NAMES = Set.of(
            "Universiti Malaya Medical Centre",
            "Hospital Kuala Lumpur",
            "Pantai Hospital Kuala Lumpur",
            "Subang Jaya Medical Centre",
            "KPJ Selangor Specialist Hospital",
            "Hospital Kajang",
            "Hospital Serdang",
            "Ampang Hospital");

    private MalaysiaMapData() {
    }

    public static boolean isHospital(Location location) {
        return location != null && HOSPITAL_NAMES.contains(location.getLocationName());
    }

    public static void seed(CityGraph cityMap, DispatchManager dispatchManager) {
        Location kk10 = add(cityMap, "Kolej Kediaman 10, Lingkaran Budi, Universiti Malaya, Kuala Lumpur", 4.7, 5.4);
        Location umMedical = add(cityMap, "Universiti Malaya Medical Centre", 4.4, 5.1);
        Location hospitalKl = add(cityMap, "Hospital Kuala Lumpur", 7.3, 4.5);
        Location pantaiHospital = add(cityMap, "Pantai Hospital Kuala Lumpur", 5.3, 5.2);
        Location sjmc = add(cityMap, "Subang Jaya Medical Centre", 2.9, 6.2);
        Location kpjSelangor = add(cityMap, "KPJ Selangor Specialist Hospital", 1.8, 6.0);
        Location hospitalKajang = add(cityMap, "Hospital Kajang", 7.4, 8.6);
        Location hospitalSerdang = add(cityMap, "Hospital Serdang", 5.8, 8.2);
        Location ampangHospital = add(cityMap, "Ampang Hospital", 8.5, 6.4);
        Location lingkaranBudi = add(cityMap, "Lingkaran Budi", 4.9, 5.7);
        Location bangsar = add(cityMap, "Bangsar", 5.5, 5.4);
        Location bangsarSouth = add(cityMap, "Bangsar South", 5.1, 6.1);
        Location midValley = add(cityMap, "Mid Valley Megamall", 5.9, 5.9);
        Location klSentral = add(cityMap, "KL Sentral", 6.3, 5.3);
        Location brickfields = add(cityMap, "Brickfields", 6.1, 5.6);
        Location bukitBintang = add(cityMap, "Bukit Bintang", 7.2, 5.7);
        Location klcc = add(cityMap, "KLCC", 7.8, 5.1);
        Location damansara = add(cityMap, "Damansara Utama", 3.9, 4.5);
        Location ss2 = add(cityMap, "SS2 Petaling Jaya", 4.0, 5.2);
        Location petalingJaya = add(cityMap, "Petaling Jaya", 3.5, 5.8);
        Location sunway = add(cityMap, "Sunway Pyramid", 3.4, 6.8);
        Location subangJaya = add(cityMap, "Subang Jaya", 2.8, 6.6);
        Location shahAlam = add(cityMap, "Shah Alam", 1.7, 6.4);
        Location setiaAlam = add(cityMap, "Setia Alam", 1.3, 5.2);
        Location kajang = add(cityMap, "Kajang", 7.2, 8.8);
        Location semenyih = add(cityMap, "Semenyih", 8.0, 9.7);
        Location cheras = add(cityMap, "Cheras", 7.1, 6.9);
        Location ampang = add(cityMap, "Ampang", 8.6, 6.1);
        Location sriPetaling = add(cityMap, "Sri Petaling", 6.2, 7.0);
        Location puchong = add(cityMap, "Puchong", 4.2, 7.4);
        Location cyberjaya = add(cityMap, "Cyberjaya", 5.3, 9.0);

        connect(cityMap, kk10, lingkaranBudi, 2);
        connect(cityMap, lingkaranBudi, umMedical, 3);
        connect(cityMap, umMedical, pantaiHospital, 3);
        connect(cityMap, lingkaranBudi, bangsarSouth, 5);
        connect(cityMap, umMedical, ss2, 6);
        connect(cityMap, ss2, damansara, 5);
        connect(cityMap, ss2, petalingJaya, 6);
        connect(cityMap, petalingJaya, sunway, 8);
        connect(cityMap, sunway, subangJaya, 5);
        connect(cityMap, subangJaya, shahAlam, 12);
        connect(cityMap, shahAlam, setiaAlam, 10);
        connect(cityMap, bangsarSouth, bangsar, 5);
        connect(cityMap, bangsar, pantaiHospital, 2);
        connect(cityMap, bangsar, midValley, 4);
        connect(cityMap, midValley, brickfields, 5);
        connect(cityMap, brickfields, klSentral, 3);
        connect(cityMap, klSentral, hospitalKl, 6);
        connect(cityMap, klSentral, bukitBintang, 7);
        connect(cityMap, bukitBintang, klcc, 6);
        connect(cityMap, klcc, hospitalKl, 4);
        connect(cityMap, midValley, sriPetaling, 8);
        connect(cityMap, sriPetaling, cheras, 9);
        connect(cityMap, cheras, ampang, 9);
        connect(cityMap, ampang, ampangHospital, 3);
        connect(cityMap, cheras, kajang, 12);
        connect(cityMap, kajang, hospitalKajang, 2);
        connect(cityMap, kajang, semenyih, 10);
        connect(cityMap, puchong, sriPetaling, 9);
        connect(cityMap, puchong, sunway, 7);
        connect(cityMap, sunway, sjmc, 3);
        connect(cityMap, subangJaya, sjmc, 2);
        connect(cityMap, shahAlam, kpjSelangor, 3);
        connect(cityMap, puchong, cyberjaya, 13);
        connect(cityMap, cyberjaya, hospitalSerdang, 5);
        connect(cityMap, hospitalSerdang, kajang, 8);
        connect(cityMap, cyberjaya, kajang, 14);
        connect(cityMap, bangsarSouth, puchong, 10);
        connect(cityMap, damansara, klSentral, 11);
        connect(cityMap, ampang, klcc, 8);

        dispatchManager.registerAmbulance(new Ambulance("AMB-01", umMedical));
        dispatchManager.registerAmbulance(new Ambulance("AMB-02", pantaiHospital));
        dispatchManager.registerAmbulance(new Ambulance("AMB-03", sjmc));
        dispatchManager.registerAmbulance(new Ambulance("AMB-04", kpjSelangor));
        dispatchManager.registerAmbulance(new Ambulance("AMB-05", hospitalKajang));
        dispatchManager.registerAmbulance(new Ambulance("AMB-06", ampangHospital));
    }

    private static Location add(CityGraph cityMap, String name, double x, double y) {
        Location location = new Location(name, x, y);
        cityMap.addLocation(location);
        return location;
    }

    private static void connect(CityGraph cityMap, Location a, Location b, int minutes) {
        cityMap.addEdge(a.getLocationName(), b.getLocationName(), minutes);
        cityMap.addEdge(b.getLocationName(), a.getLocationName(), minutes);
    }
}
