package com.cardealer.catalog;

import com.cardealer.dto.DealerDirectoryEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DealerDirectoryCatalog {

    private static final List<String> SPANISH_REGIONS = List.of(
        "Madrid", "Catalunya", "Andalucia", "Pais Vasco", "Galicia", "Castilla y Leon",
        "Valencia", "Aragon", "Murcia", "Asturias", "Navarra", "Canarias",
        "Baleares", "Extremadura", "La Rioja", "Cantabria"
    );

    private static final Map<String, List<String>> REGION_CITIES = buildRegionCities();
    private static final List<String> COMPANY_PREFIXES = List.of(
        "Atlas", "Boreal", "Costa", "Delta", "Elite", "Futura", "Gran", "Hispania",
        "Iberia", "Jet", "Krono", "Levante", "Motor", "Norte", "Omega", "Prime",
        "Quality", "Ruta", "Sigma", "Top", "Urban", "Vanguard", "West", "Zenit"
    );
    private static final List<String> COMPANY_SUFFIXES = List.of(
        "Motex", "Desguace", "Schadeautos", "Vehiculos", "Comerciales", "Ocasion",
        "Export", "Motors", "Auto Group", "Salvage", "Recambios", "Fleet"
    );
    private static final List<String> SPECIALIZATIONS = List.of(
        "Motex", "Desguace", "Vehiculo comercial", "Ocasion", "Siniestrados", "Exportacion"
    );
    private static final List<DealerDirectoryEntry> DIRECTORY = buildDirectory();

    private DealerDirectoryCatalog() {
    }

    public static List<String> supportedRegions() {
        return SPANISH_REGIONS;
    }

    public static List<DealerDirectoryEntry> entries() {
        return DIRECTORY;
    }

    public static String regionForCity(String city) {
        if (city == null || city.isBlank()) {
            return "Madrid";
        }
        String normalized = city.trim().toLowerCase(Locale.ROOT);
        return REGION_CITIES.entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(normalized::equals))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse("Madrid");
    }

    private static List<DealerDirectoryEntry> buildDirectory() {
        List<DealerDirectoryEntry> entries = new ArrayList<>();
        int sequence = 0;

        for (int regionIndex = 0; regionIndex < SPANISH_REGIONS.size(); regionIndex++) {
            String region = SPANISH_REGIONS.get(regionIndex);
            List<String> cities = REGION_CITIES.get(region);

            for (int i = 0; i < 18; i++) {
                String city = cities.get(i % cities.size());
                String prefix = COMPANY_PREFIXES.get((regionIndex * 7 + i) % COMPANY_PREFIXES.size());
                String suffix = COMPANY_SUFFIXES.get((regionIndex * 5 + i) % COMPANY_SUFFIXES.size());
                String specialization = SPECIALIZATIONS.get((regionIndex + i) % SPECIALIZATIONS.size());

                entries.add(DealerDirectoryEntry.builder()
                    .companyName(prefix + " " + city + " " + suffix)
                    .specialization(specialization)
                    .region(region)
                    .city(city)
                    .email("directorio+" + sequence + "@schadeautos.example")
                    .phone("+34 9" + String.format(Locale.ROOT, "%08d", 10000000 + sequence))
                    .listingCount(12 + ((regionIndex + i) % 37))
                    .realDealer(false)
                    .build());
                sequence++;
            }
        }

        return entries.stream()
            .sorted(Comparator.comparing(DealerDirectoryEntry::getCompanyName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private static Map<String, List<String>> buildRegionCities() {
        Map<String, List<String>> regionCities = new LinkedHashMap<>();
        regionCities.put("Madrid", List.of("Madrid", "Alcala de Henares", "Getafe", "Leganes", "Mostoles", "Alcorcon"));
        regionCities.put("Catalunya", List.of("Barcelona", "Girona", "Lleida", "Tarragona", "Sabadell", "Terrassa"));
        regionCities.put("Andalucia", List.of("Sevilla", "Malaga", "Granada", "Cadiz", "Cordoba", "Almeria"));
        regionCities.put("Pais Vasco", List.of("Bilbao", "Vitoria", "Donostia", "Barakaldo", "Irun", "Eibar"));
        regionCities.put("Galicia", List.of("A Coruna", "Vigo", "Santiago", "Ourense", "Lugo", "Pontevedra"));
        regionCities.put("Castilla y Leon", List.of("Valladolid", "Burgos", "Leon", "Salamanca", "Segovia", "Palencia"));
        regionCities.put("Valencia", List.of("Valencia", "Alicante", "Castellon", "Elche", "Gandia", "Benidorm"));
        regionCities.put("Aragon", List.of("Zaragoza", "Huesca", "Teruel", "Calatayud", "Utebo", "Alcaniz"));
        regionCities.put("Murcia", List.of("Murcia", "Cartagena", "Lorca", "Molina de Segura", "Yecla", "Aguilas"));
        regionCities.put("Asturias", List.of("Oviedo", "Gijon", "Aviles", "Mieres", "Langreo", "Siero"));
        regionCities.put("Navarra", List.of("Pamplona", "Tudela", "Estella", "Burlada", "Tafalla", "Zizur Mayor"));
        regionCities.put("Canarias", List.of("Las Palmas", "Santa Cruz", "La Laguna", "Arrecife", "Telde", "Adeje"));
        regionCities.put("Baleares", List.of("Palma", "Ibiza", "Mahon", "Inca", "Manacor", "Calvia"));
        regionCities.put("Extremadura", List.of("Badajoz", "Caceres", "Merida", "Plasencia", "Don Benito", "Almendralejo"));
        regionCities.put("La Rioja", List.of("Logrono", "Calahorra", "Arnedo", "Haro", "Lardero", "Najera"));
        regionCities.put("Cantabria", List.of("Santander", "Torrelavega", "Castro Urdiales", "Camargo", "Laredo", "Santona"));
        return regionCities;
    }
}
