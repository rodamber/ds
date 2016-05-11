package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * State design pattern. Represents a possible mode for the server.
 */
public abstract class BrokerMode {
    protected BrokerPort port;
    protected Hashtable<Long, TransportView> records = new Hashtable<>();

    // Largest key currently in use
    private Long maxCurrentKey = (long) 0;

    public BrokerMode(BrokerPort port) {
        if (port == null) {
            throw new IllegalArgumentException("port must not be null");
        }
        this.port = port;
    }

    public Optional<TransportView> getViewByKey(Long key) {
        return Optional.ofNullable(records.get(key));
    }

    public Optional<TransportView> getViewById(String id) {
        return records.values().stream()
            .filter(v -> v.getId().equals(id))
            .findFirst();
    }

    public List<TransportView> listTransports() {
        return new ArrayList<>(records.values());
    }

    public void clearTransports() {
        records.clear();
    }

    public abstract String ping(String name);

    public abstract String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception,
               UnknownLocationFault_Exception;

    public abstract TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception;

    public void updateViewState(String id, TransportStateView newState) {
    }

    public void touch(String name) {
    }

    public void addView(TransportView tv) {
        if (!records.contains(tv)) {
            records.put(maxCurrentKey++, tv);
        }
    }

    public abstract void shutdown();

}
