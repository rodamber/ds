package pt.upa.broker.ws;

import java.util.List;
import java.util.ArrayList;


/**
 * State design pattern. Represents a possible mode for the server.
 */
public abstract class BrokerMode {
    protected BrokerEndpointManager endpoint;
    protected List<TransportView> views;

    public BrokerMode(BrokerEndpointManager endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint must not be null");
        }
        this.endpoint = endpoint;
        this.views = new ArrayList<>();
    }

    public void addView(TransportView tv) {
        this.views.add(tv);
    }

    public List<TransportView> listTransports() {
        return this.views;
    }

    public void clearTransports() {
        this.views.clear();
    }

    public abstract String ping(String name);

    public abstract String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception,
               UnknownLocationFault_Exception;

    public abstract TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception;

}
