package com.cardealer.catalog;

import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.FuelType;
import com.cardealer.model.enums.TransmissionType;
import com.cardealer.model.enums.VehicleOrigin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class AdvancedSearchCatalog {

    private AdvancedSearchCatalog() {
    }

    public static List<String> brands() {
        return List.of(
            "Abarth", "ACA", "Acura", "Aiways", "Aixam", "Alfa Romeo", "Alpina", "Alpine", "AMC",
            "Aston Martin", "Audi", "Austin", "Autobianchi", "Baic", "Bentley", "BMW", "Borgward",
            "Brilliance", "Bugatti", "Buick", "BYD", "Cadillac", "Casalini", "Caterham", "Chevrolet",
            "Chrysler", "Citroen", "Cupra", "Dacia", "Daewoo", "Daihatsu", "Datsun", "Dodge", "DS",
            "Ferrari", "Fiat", "Fisker", "Ford", "Genesis", "Geo", "GMC", "Great Wall", "Honda",
            "Hummer", "Hyundai", "INEOS", "Infiniti", "Isuzu", "Iveco", "Jaguar", "Jeep", "Kia",
            "Koenigsegg", "Lada", "Lamborghini", "Lancia", "Land Rover", "Lexus", "Ligier", "Lincoln",
            "Lotus", "Lucid", "Mahindra", "Maserati", "Maybach", "Mazda", "McLaren", "Mercedes-Benz",
            "MG", "Microcar", "Mini", "Mitsubishi", "Morgan", "NIO", "Nissan", "Oldsmobile", "Opel",
            "Pagani", "Peugeot", "Polestar", "Pontiac", "Porsche", "Proton", "RAM", "Renault",
            "Rolls-Royce", "Rover", "Saab", "Seat", "Skoda", "Smart", "SsangYong", "Subaru", "Suzuki",
            "Talbot", "Tata", "Tesla", "Toyota", "Triumph", "TVR", "Vauxhall", "Volkswagen", "Volvo",
            "Voyah", "Wartburg", "Westfield", "Wiesmann", "XPeng", "Yugo", "Zastava", "Zeekr"
        );
    }

    public static Map<String, List<String>> modelsByBrand() {
        Map<String, List<String>> models = new LinkedHashMap<>();
        models.put("Abarth", List.of("500", "595", "695", "Punto Evo"));
        models.put("Alfa Romeo", List.of("147", "156", "159", "Giulia", "Giulietta", "Mito", "Stelvio", "Tonale"));
        models.put("Audi", List.of("A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "TT"));
        models.put("BMW", List.of("1 Series", "2 Series", "3 Series", "4 Series", "5 Series", "7 Series", "X1", "X3", "X5", "X6", "i3", "i4"));
        models.put("Citroen", List.of("C1", "C3", "C4", "C5 Aircross", "Berlingo", "Jumpy", "SpaceTourer"));
        models.put("Cupra", List.of("Ateca", "Born", "Formentor", "Leon", "Tavascan"));
        models.put("Dacia", List.of("Duster", "Jogger", "Logan", "Sandero", "Spring"));
        models.put("DS", List.of("DS 3", "DS 4", "DS 7", "DS 9"));
        models.put("Fiat", List.of("500", "500X", "Panda", "Punto", "Tipo", "Doblo", "Ducato"));
        models.put("Ford", List.of("Fiesta", "Focus", "Kuga", "Mondeo", "Mustang", "Puma", "Ranger", "Transit"));
        models.put("Honda", List.of("Accord", "Civic", "CR-V", "HR-V", "Jazz", "e"));
        models.put("Hyundai", List.of("i10", "i20", "i30", "Bayon", "IONIQ", "IONIQ 5", "Kona", "Santa Fe", "Tucson"));
        models.put("Jaguar", List.of("E-Pace", "F-Pace", "F-Type", "I-Pace", "XE", "XF"));
        models.put("Jeep", List.of("Avenger", "Cherokee", "Compass", "Grand Cherokee", "Renegade", "Wrangler"));
        models.put("Kia", List.of("Ceed", "EV6", "Niro", "Picanto", "Sorento", "Sportage", "Stonic"));
        models.put("Land Rover", List.of("Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Evoque", "Range Rover Sport"));
        models.put("Lexus", List.of("CT", "ES", "IS", "NX", "RX", "UX"));
        models.put("Mazda", List.of("2", "3", "6", "CX-3", "CX-30", "CX-5", "MX-5"));
        models.put("Mercedes-Benz", List.of("A-Class", "B-Class", "C-Class", "CLA", "E-Class", "GLA", "GLC", "GLE", "Sprinter", "Vito"));
        models.put("MG", List.of("HS", "MG3", "MG4", "Marvel R", "ZS"));
        models.put("Mini", List.of("Clubman", "Convertible", "Countryman", "Cooper", "Paceman"));
        models.put("Mitsubishi", List.of("ASX", "Eclipse Cross", "L200", "Outlander", "Space Star"));
        models.put("Nissan", List.of("Ariya", "Juke", "Leaf", "Micra", "Navara", "Qashqai", "X-Trail"));
        models.put("Opel", List.of("Astra", "Corsa", "Crossland", "Grandland", "Insignia", "Mokka", "Vivaro"));
        models.put("Peugeot", List.of("108", "208", "2008", "3008", "308", "408", "5008", "Partner", "Traveller"));
        models.put("Polestar", List.of("1", "2", "3", "4"));
        models.put("Porsche", List.of("718", "911", "Cayenne", "Macan", "Panamera", "Taycan"));
        models.put("Renault", List.of("Austral", "Captur", "Clio", "Espace", "Kadjar", "Kangoo", "Megane", "Scenic", "Trafic"));
        models.put("Seat", List.of("Arona", "Ateca", "Ibiza", "Leon", "Tarraco"));
        models.put("Skoda", List.of("Citigo", "Fabia", "Kamiq", "Karoq", "Kodiaq", "Octavia", "Scala", "Superb"));
        models.put("Smart", List.of("forfour", "fortwo", "#1", "#3"));
        models.put("SsangYong", List.of("Korando", "Musso", "Rexton", "Tivoli"));
        models.put("Subaru", List.of("BRZ", "Forester", "Impreza", "Levorg", "Outback", "XV"));
        models.put("Suzuki", List.of("Across", "Ignis", "Jimny", "S-Cross", "Swift", "Vitara"));
        models.put("Tesla", List.of("Model 3", "Model S", "Model X", "Model Y"));
        models.put("Toyota", List.of("Aygo", "C-HR", "Camry", "Corolla", "Hilux", "Prius", "RAV4", "Yaris", "Yaris Cross"));
        models.put("Volkswagen", List.of("Arteon", "Caddy", "Golf", "ID.3", "ID.4", "Passat", "Polo", "T-Cross", "T-Roc", "Tiguan", "Touareg", "Transporter"));
        models.put("Volvo", List.of("C40", "S60", "S90", "V60", "V90", "XC40", "XC60", "XC90"));
        return models;
    }

    public static List<String> mergedBrands(List<String> dynamicBrands) {
        TreeSet<String> merged = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        merged.addAll(brands());
        if (dynamicBrands != null) {
            merged.addAll(dynamicBrands);
        }
        return new ArrayList<>(merged);
    }

    public static List<Map<String, String>> fuelOptions() {
        return List.of(
            Map.of("value", "", "labelKey", "search.fuel.all"),
            Map.of("value", FuelType.GASOLINA.name(), "labelKey", "search.fuel.gasoline"),
            Map.of("value", FuelType.DIESEL.name(), "labelKey", "search.fuel.diesel"),
            Map.of("value", FuelType.GLP.name(), "labelKey", "search.fuel.lpg"),
            Map.of("value", FuelType.ELECTRICO.name(), "labelKey", "search.fuel.electric"),
            Map.of("value", FuelType.HIBRIDO.name(), "labelKey", "search.fuel.hybrid"),
            Map.of("value", FuelType.GAS_NATURAL.name(), "labelKey", "search.fuel.naturalGas"),
            Map.of("value", FuelType.HIDROGENO.name(), "labelKey", "search.fuel.hydrogen"),
            Map.of("value", FuelType.BENZINE_CNG.name(), "labelKey", "search.fuel.benzineCng"),
            Map.of("value", FuelType.HCNG.name(), "labelKey", "search.fuel.hcng")
        );
    }

    public static List<Map<String, String>> transmissionOptions() {
        return List.of(
            Map.of("value", "", "labelKey", "search.transmission.all"),
            Map.of("value", TransmissionType.AUTOMATICO.name(), "labelKey", "search.transmission.automatic"),
            Map.of("value", TransmissionType.MANUAL.name(), "labelKey", "search.transmission.manual")
        );
    }

    public static List<Map<String, String>> colorOptions() {
        return List.of(
            Map.of("value", "AMARILLO", "labelKey", "search.color.yellow"),
            Map.of("value", "AZUL", "labelKey", "search.color.blue"),
            Map.of("value", "BEIGE", "labelKey", "search.color.beige"),
            Map.of("value", "BLANCO", "labelKey", "search.color.white"),
            Map.of("value", "CREMA", "labelKey", "search.color.cream"),
            Map.of("value", "DORADO", "labelKey", "search.color.gold"),
            Map.of("value", "GRIS", "labelKey", "search.color.gray"),
            Map.of("value", "MARRON", "labelKey", "search.color.brown"),
            Map.of("value", "MORADO", "labelKey", "search.color.purple"),
            Map.of("value", "NARANJA", "labelKey", "search.color.orange"),
            Map.of("value", "NEGRO", "labelKey", "search.color.black"),
            Map.of("value", "PLATEADO", "labelKey", "search.color.silver"),
            Map.of("value", "ROJO", "labelKey", "search.color.red"),
            Map.of("value", "ROSA", "labelKey", "search.color.pink"),
            Map.of("value", "VARIOS", "labelKey", "search.color.multiple"),
            Map.of("value", "VERDE", "labelKey", "search.color.green")
        );
    }

    public static List<Map<String, String>> colorCodeOptions() {
        return List.of(
            colorCode("Y01"), colorCode("Y02"), colorCode("B01"), colorCode("B02"), colorCode("BG1"),
            colorCode("W01"), colorCode("W02"), colorCode("CR1"), colorCode("GD1"), colorCode("GY1"),
            colorCode("GY2"), colorCode("BR1"), colorCode("PR1"), colorCode("OR1"), colorCode("BK1"),
            colorCode("BK2"), colorCode("SV1"), colorCode("RD1"), colorCode("RD2"), colorCode("PK1"),
            colorCode("MX1"), colorCode("GN1"), colorCode("GN2"), colorCode("BE1"), colorCode("ML1")
        );
    }

    public static List<Map<String, String>> bodyTypeOptions() {
        return List.of(
            Map.of("value", "HATCHBACK", "labelKey", "search.bodyType.hatchback"),
            Map.of("value", "COUPE", "labelKey", "search.bodyType.coupe"),
            Map.of("value", "CONVERTIBLE", "labelKey", "search.bodyType.convertible"),
            Map.of("value", "WAGON", "labelKey", "search.bodyType.wagon"),
            Map.of("value", "DELIVERY_VAN", "labelKey", "search.bodyType.deliveryVan"),
            Map.of("value", "MINIVAN", "labelKey", "search.bodyType.minivan"),
            Map.of("value", "SEDAN", "labelKey", "search.bodyType.sedan"),
            Map.of("value", "SUV", "labelKey", "search.bodyType.suv"),
            Map.of("value", "MONOVA", "labelKey", "search.bodyType.monova"),
            Map.of("value", "COMMERCIAL_VEHICLE", "labelKey", "search.bodyType.commercialVehicle")
        );
    }

    public static Map<String, List<BodyType>> bodyTypeMappings() {
        Map<String, List<BodyType>> mappings = new LinkedHashMap<>();
        mappings.put("HATCHBACK", List.of(BodyType.HATCHBACK));
        mappings.put("COUPE", List.of(BodyType.COUPE));
        mappings.put("CONVERTIBLE", List.of(BodyType.CONVERTIBLE));
        mappings.put("WAGON", List.of(BodyType.WAGON));
        mappings.put("DELIVERY_VAN", List.of(BodyType.VAN));
        mappings.put("MINIVAN", List.of(BodyType.VAN));
        mappings.put("SEDAN", List.of(BodyType.SEDAN));
        mappings.put("SUV", List.of(BodyType.SUV));
        mappings.put("MONOVA", List.of(BodyType.VAN));
        mappings.put("COMMERCIAL_VEHICLE", List.of(BodyType.PICKUP, BodyType.VAN));
        return mappings;
    }

    public static Set<String> supportedColorValues() {
        return extractValues(colorOptions());
    }

    public static Set<String> supportedColorCodes() {
        return extractValues(colorCodeOptions());
    }

    public static Set<String> supportedBodyTypeValues() {
        return extractValues(bodyTypeOptions());
    }

    public static List<Map<String, String>> originOptions() {
        return List.of(
            Map.of("value", VehicleOrigin.NETHERLANDS.name(), "labelKey", "search.origin.netherlands"),
            Map.of("value", VehicleOrigin.BELGIUM.name(), "labelKey", "search.origin.belgium"),
            Map.of("value", VehicleOrigin.GERMANY.name(), "labelKey", "search.origin.germany"),
            Map.of("value", VehicleOrigin.ENGLAND.name(), "labelKey", "search.origin.england"),
            Map.of("value", VehicleOrigin.POLAND.name(), "labelKey", "search.origin.poland"),
            Map.of("value", VehicleOrigin.FRANCE.name(), "labelKey", "search.origin.france"),
            Map.of("value", VehicleOrigin.OTHER_EUROPEAN_COUNTRIES.name(), "labelKey", "search.origin.otherEuropeanCountries"),
            Map.of("value", VehicleOrigin.AMERICA.name(), "labelKey", "search.origin.america"),
            Map.of("value", VehicleOrigin.JAPAN.name(), "labelKey", "search.origin.japan")
        );
    }

    public static Set<String> supportedOriginValues() {
        return extractValues(originOptions());
    }

    public static List<Integer> nearbyRadiusOptions() {
        return List.of(5, 10, 25, 50, 75, 100, 150, 200);
    }

    public static Set<Integer> supportedNearbyRadiusOptions() {
        return new LinkedHashSet<>(nearbyRadiusOptions());
    }

    public static List<Integer> pricePresets() {
        return List.of(
            1000, 2000, 3000, 5000, 7500, 10000, 15000, 20000, 30000, 40000, 50000,
            75000, 100000, 150000, 200000, 250000, 300000
        );
    }

    public static List<Integer> mileagePresets() {
        return List.of(
            10000, 20000, 30000, 50000, 75000, 100000, 150000, 200000, 300000,
            400000, 500000, 750000, 1000000
        );
    }

    private static Map<String, String> colorCode(String value) {
        return Map.of("value", value, "labelKey", "search.colorCode." + value.toLowerCase());
    }

    private static Set<String> extractValues(List<Map<String, String>> options) {
        Set<String> values = new LinkedHashSet<>();
        options.forEach(option -> values.add(option.get("value")));
        return values;
    }
}
