package pt.upa.transporter;

import java.util.List;
import java.util.Arrays;

public class Utils {
    private static List<String> cidadesNorte = Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança");
    private static List<String> cidadesCentro = Arrays.asList("Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda");
    private static List<String> cidadesSul = Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja", "Faro");

    public static boolean isCityNameValid(String cityname){
        return cidadesNorte.contains(cityname) || cidadesCentro.contains(cityname) || cidadesSul.contains(cityname);
    }
    
    public static boolean transporterServesCity(int transporterID, String cityname){
        return ((cidadesNorte.contains(cityname) || cidadesCentro.contains(cityname)) && (transporterID%2) == 0) || ((cidadesCentro.contains(cityname) || cidadesSul.contains(cityname)) && (transporterID%2) == 1);
    }
    
    public static int getTransporterID(String transporterWsName){
        return transporterWsName.charAt(transporterWsName.length()-1);
    }
}