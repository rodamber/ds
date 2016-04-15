package pt.upa.transporter.ws;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import javax.jws.WebService;

@WebService(
    name              = "TransporterWebService",
    targetNamespace   = "http://ws.transporter.upa.pt/",
    wsdlLocation      = "transporter.1_0.wsdl",
    serviceName       = "TransporterService",
    portName          = "TransporterPort",
    endpointInterface = "pt.upa.transporter.ws.TransporterPortType"
)

public class TransporterPort implements TransporterPortType {

    private TransporterEndpointManager endpoint;
    private List<JobView> registry = new ArrayList<JobView>();
    
    public TransporterPort(TransporterEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    TransporterPort() { }

    /* TransporterPortType implementation */

    @Override
    public String ping(String name) {
        return "Ping: " + name;
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
        throws BadLocationFault_Exception, BadPriceFault_Exception {
    	
    	int transporterID = getTransporterID(endpoint.getWsName());
    	    	
    	if(!isCityNameValid(origin)){
    		throw new BadLocationFault_Exception(origin + " is not a valid city name", new BadLocationFault());
    	}
    	else if(!isCityNameValid(destination)){
    		throw new BadLocationFault_Exception(origin + " is not a valid city name", new BadLocationFault());
    	}
    	else if(price < 0){
    		throw new BadPriceFault_Exception("Price must be greater than 0", new BadPriceFault());
    	}
    	
    	if(!transporterServesCity(transporterID, origin) || !transporterServesCity(transporterID, destination)){
    		return null;
    	}
    	else if(price > 100){
    		return null;
    	}
    	else if(price > 10){
    		if(price % 2 == transporterID % 2){
    			//TODO FAZ OFERTA ABAIXO DO PREÇO
    		}
    		else{
    			//TODO FAZ OFERTA ACIMA DO PREÇO
    		}
    	}
    	
    	//TODO RESTO
    	
        return null;
    }

    @Override
    public JobView decideJob(String id, boolean accept)
        throws BadJobFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public JobView jobStatus(String id) {
        /* RODRIGO:FIXME:TODO */
        return null;
    }


    @Override
    public List<JobView> listJobs() {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public void clearJobs() {
    	/* RODRIGO:FIXME:TODO */ 
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    private List<String> northRegion = Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança");
    private List<String> centerRegion = Arrays.asList("Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda");
    private List<String> southRegion = Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja", "Faro");

    private boolean isCityNameValid(String cityname){
        return northRegion.contains(cityname) || centerRegion.contains(cityname) || southRegion.contains(cityname);
    }
    
    private boolean transporterServesCity(int transporterID, String cityname){
        return ((northRegion.contains(cityname) || centerRegion.contains(cityname)) && (transporterID%2) == 0) || ((centerRegion.contains(cityname) || southRegion.contains(cityname)) && (transporterID%2) == 1);
    }
    
    private int getTransporterID(String transporterWsName){
        return transporterWsName.charAt(transporterWsName.length()-1);
    }
    
    private int random(int min, int max){
    	return (new Random()).nextInt((max - min) + 1) + min;
    }
    
    private String generateID(int transporterID, int jobID){
    	return "UpaTransporter" + Integer.toString(transporterID) + "-" + Integer.toString(jobID);
    }

}
