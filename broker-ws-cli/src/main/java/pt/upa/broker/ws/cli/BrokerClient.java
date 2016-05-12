package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.ws.*;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;

/**
 * Client.
 *
 * Adds easier endpoint address configuration and
 * UDDI lookup capability to the PortType generated by wsimport.
 */
public class BrokerClient implements BrokerPortType {
    private final int MAX_TRIES = 5;

    BrokerService service = null;
    BrokerPortType port = null;

    private String uddiURL = null;
    private String wsName = null;
    private String wsURL = null;

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

    private void init() {
        int receiveTimeout = 6000;

        // The receive timeout property has alternative names
        // Again, set them all to avoid compability issues
        final List<String> RECV_TIME_PROPS = new ArrayList<String>();
        RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
        RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
        RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");

        // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
        for (String propName : RECV_TIME_PROPS) {
            BindingProvider bindingProvider = (BindingProvider) port;
            bindingProvider.getRequestContext().put(propName, receiveTimeout);
        }
    }

    public BrokerClient(String wsURL) throws BrokerClientException {
        this.wsURL = wsURL;
        createStub();
        init();
    }

    public BrokerClient(String uddiURL, String wsName) throws BrokerClientException {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
        init();
    }

    private void uddiLookup() throws BrokerClientException {
        try {
            if (verbose) {
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            }
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose) {
                System.out.printf("Looking for '%s'%n", wsName);
            }
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!",
                                       uddiURL);
            throw new BrokerClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format("Service with name %s not found on UDDI at %s", wsName,
                                       uddiURL);
            throw new BrokerClientException(msg);
        }
    }

    private void createStub() {
        if (verbose) {
            System.out.println("Creating stub ...");
        }
        service = new BrokerService();
        port = service.getBrokerPort();

        if (wsURL != null) {
            if (verbose) {
                System.out.println("Setting endpoint address ...");
            }
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }

    // remote invocation methods ----------------------------------------------

    @Override
    public String ping(String name) {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                return port.ping(name);
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
        return null;
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                return port.requestTransport(origin, destination, price);
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
        return null;
    }

    @Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                return port.viewTransport(id);
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
        return null;
    }

    @Override
    public List<TransportView> listTransports() {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                return port.listTransports();
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
        return null;
    }

    @Override
    public void clearTransports() {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                port.clearTransports();
                break;
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
    }

    @Override
    public void updateViewState(Integer key, TransportStateView newState) {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                port.updateViewState(key, newState);
                break;
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
    }

    @Override
    public void touch(String name) {
        port.touch(name);
    }

    @Override
    public void addViewRecord(ViewRecord re) {
        for (int i = 0; i < MAX_TRIES; ++i)
            try {
                port.addViewRecord(re);
                break;
            } catch(WebServiceException wse) {
                wse.printStackTrace();
            }
    }

}
