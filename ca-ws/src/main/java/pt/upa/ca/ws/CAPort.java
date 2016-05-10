package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService(
    name              = "CAWebService",
    targetNamespace   = "http://ws.ca.upa.pt/",
    wsdlLocation      = "ca.1_0.wsdl",
    serviceName       = "CAService",
    portName          = "CAPort",
    endpointInterface = "pt.upa.ca.ws.CAPortType"
)
public class CAPort implements CAPortType {

    private CAEndpointManager endpoint;

    public CAPort(CAEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    CAPort() { }

    /* CAPortType implementation */

    @Override
    public String ping(String name) {
        return "Ping: " + name;
    }
    
}
