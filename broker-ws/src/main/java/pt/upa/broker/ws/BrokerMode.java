package pt.upa.broker.ws;

import pt.upa.broker.BrokerApplication;
import java.util.*;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;


/**
 * State design pattern. Represents a possible mode for the server.
 */
public abstract class BrokerMode {
    protected BrokerPort port;
    protected Hashtable<Integer, ViewRecord> records = new Hashtable<>();

    protected boolean verbose = false;

    public BrokerMode(BrokerPort port) {
        if (port == null) {
            throw new IllegalArgumentException("port must not be null");
        }
        this.port = port;
        this.verbose = port.getEndpoint().isVerbose();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void updateRecord(ViewRecord re) {
        if (verbose) {
            if (this.records.containsKey(re.getKey())) {
                System.out.printf("Updated record with key %d: ", re.getKey());
                BrokerApplication.printView(re.getView());
            } else {
                System.out.printf("Added record with key %d%n", re.getKey());
            }
        }
        records.put(re.getKey(), re);
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
        final List<TransportView> views =
            records.values().stream().map(re -> re.view).collect(toList());
        for (TransportView tv : views) {
            try {
                viewTransport(tv.getId());
            } catch (UnknownTransportFault_Exception e) {
                if (tv.getId() != null) {
                    e.printStackTrace();
                } // else ignore
            }
        }
        return views;
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

    public void touch(String name) {
    }

    public abstract void shutdown();

}
