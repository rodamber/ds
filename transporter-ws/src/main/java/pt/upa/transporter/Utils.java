package pt.upa.transporter;

import java.util.List;
import java.util.Arrays;
import java.util.Random;

public class Utils {
    private static List<String> northRegion = Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança");
    private static List<String> centerRegion = Arrays.asList("Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda");
    private static List<String> southRegion = Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja", "Faro");

    public static boolean isCityNameValid(String cityname){
        return northRegion.contains(cityname) || centerRegion.contains(cityname) || southRegion.contains(cityname);
    }
    
    public static boolean transporterServesCity(int transporterID, String cityname){
        return ((northRegion.contains(cityname) || centerRegion.contains(cityname)) && (transporterID%2) == 0) || ((centerRegion.contains(cityname) || southRegion.contains(cityname)) && (transporterID%2) == 1);
    }
    
    public static int getTransporterID(String transporterWsName){
        return transporterWsName.charAt(transporterWsName.length()-1);
    }
    
    public static int random(int min, int max){
    	return (new Random()).nextInt((max - min) + 1) + min;
    }
}