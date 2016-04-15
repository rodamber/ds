package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.upa.broker.ws.*;
import java.util.List;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class BrokerClient implements BrokerPortType {

    /** WS service */
    BrokerService service = null;

    /** WS port (port type is the interface, port is the implementation) */
    BrokerPortType port = null;

    /** UDDI server URL */
    private String uddiURL = null;

    /** WS name */
    private String wsName = null;

    /** WS endpoint address */
    private String wsURL = null; // default value is defined inside WSDL

    public String getWsURL() {
        return wsURL;
    }

    /** output option **/
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** constructor with provided web service URL */
    public BrokerClient(String wsURL) throws BrokerClientException {
        this.wsURL = wsURL;
        createStub();
    }

    /** constructor with provided UDDI location and name */
    public BrokerClient(String uddiURL, String wsName) throws BrokerClientException {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() throws BrokerClientException {
        try {
            if (verbose)
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose)
                System.out.printf("Looking for '%s'%n", wsName);
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
            throw new BrokerClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format("Service with name %s not found on UDDI at %s", wsName, uddiURL);
            throw new BrokerClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
            System.out.println("Creating stub ...");
        service = new BrokerService();
        port = service.getBrokerPort();

        if (wsURL != null) {
            if (verbose)
                System.out.println("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }
    
    /* BrokerPortType implementation */

    @Override
    public String ping(String name) {
        return port.ping(name);
    }
    
    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
        return port.requestTransport(origin, destination, price);
    }
    
    @Override
    public TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception{
        return port.viewTransport(id);
    }
    
    @Override
    public List<TransportView> listTransports(){
        return port.listTransports();
    }
    
    @Override
    public void clearTransports(){
        port.clearTransports();
    }

}
