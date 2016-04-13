package pt.upa.broker.ws;

import java.util.List;
import javax.jws.WebService;

@WebService(
    name              = "BrokerWebService",
    targetNamespace   = "http://ws.broker.upa.pt/",
    wsdlLocation      = "broker.1_0.wsdl",
    serviceName       = "BrokerService",
    portName          = "BrokerPort",
    endpointInterface = "pt.upa.broker.ws.BrokerPortType"
)
public class BrokerPort implements BrokerPortType {

    private BrokerEndpointManager endpoint;

    public BrokerPort(BrokerEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    BrokerPort() { }

    /* BrokerPortType implementation */

    @Override
    public String ping(String name) {
        return "Ping: " + name;
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public List<TransportView> listTransports() {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public void clearTransports() {
        /* RODRIGO:FIXME:TODO */
    }

}


