package pt.upa.broker.ws;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * State design pattern. Represents a possible mode for the server.
 */
public abstract class BrokerMode {
    protected BrokerPort port;
    protected List<TransportView> views;

    public BrokerMode(BrokerPort port) {
        if (port == null) {
            throw new IllegalArgumentException("port must not be null");
        }
        this.port = port;
        this.views = new ArrayList<>();
    }

    public Optional<TransportView> getViewById(String id) {
        return views.stream().filter(v -> v.getId().equals(id)).findFirst();
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

    public void updateViewState(String id, TransportStateView newState) {
        views.stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .get()
            .setState(newState);
    }

    public void touch(String name) {
    }

    public void addView(TransportView tv) {
        // RODRIGO:FIXME: Should throw an exception if view already exists or is null.
        if (tv != null) {
            this.views.add(tv);
        }
    }

    public abstract void shutdown();

}
