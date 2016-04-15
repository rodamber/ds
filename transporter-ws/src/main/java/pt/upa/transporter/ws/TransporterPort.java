package pt.upa.transporter.ws;

import java.util.Arrays;
import java.util.List;
import pt.upa.transporter.Utils;

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
    	
    	int transporterID = Utils.getTransporterID(endpoint.getWsName());
    	    	
    	if(!Utils.isCityNameValid(origin)){
    		throw new BadLocationFault_Exception(origin + " is not a valid city name", new BadLocationFault());
    	}
    	else if(!Utils.isCityNameValid(destination)){
    		throw new BadLocationFault_Exception(origin + " is not a valid city name", new BadLocationFault());
    	}
    	else if(price < 0){
    		throw new BadPriceFault_Exception("Price must be greater than 0", new BadPriceFault());
    	}
    	
    	if(!Utils.transporterServesCity(transporterID, origin) || !Utils.transporterServesCity(transporterID, destination)){
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

}
