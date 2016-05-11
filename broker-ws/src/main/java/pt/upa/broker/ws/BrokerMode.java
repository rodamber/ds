package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;


/**
 * State design pattern. Represents a possible mode for the server.
 */
public abstract class BrokerMode {
    protected BrokerPort port;
    protected Hashtable<Integer, ViewRecord> records = new Hashtable<>();

    private int maxCurrentKey = 0;

    public BrokerMode(BrokerPort port) {
        if (port == null) {
            throw new IllegalArgumentException("port must not be null");
        }
        this.port = port;
        System.out.println(this);
    }

    public void addViewRecord(ViewRecord re) {
        records.put(re.getKey(), re);
        System.out.printf("Added new record with key %d%n", re.getKey());
    }

    public Optional<ViewRecord> getRecordByKey(int key) {
        return Optional.ofNullable(records.get(key));
    }

    public Optional<ViewRecord> getRecordByViewId(String id) {
        return records.values().stream()
            .filter(re -> re.view.getId().equals(id))
            .findFirst();
    }

    public List<TransportView> listTransports() {
        return records.values().stream()
            .map(re -> re.view)
            .collect(toList());
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

    public void updateViewState(int key, TransportStateView newState) {
        final ViewRecord re = getRecordByKey(key).get();
        re.view.setState(newState);
        System.out.printf("Updated record with key %d to state %s%n",
                          re.key, re.view.getState());
    }

    public void touch(String name) {
    }

    public abstract void shutdown();

}
