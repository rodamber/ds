package pt.upa.broker.ws;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.jws.WebService;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;

@WebService(
    name              = "BrokerWebService",
    targetNamespace   = "http://ws.broker.upa.pt/",
    wsdlLocation      = "broker.2_0.wsdl",
    serviceName       = "BrokerService",
    portName          = "BrokerPort",
    endpointInterface = "pt.upa.broker.ws.BrokerPortType"
)
public class BrokerPort implements BrokerPortType {
    // Represents the possible modes of a server.
    public static final String PRIMARY_MODE = "PRIMARY_MODE";
    public static final String BACKUP_MODE  = "BACKUP_MODE";

    private BrokerMode mode;
    private BrokerEndpointManager endpoint;

    public BrokerPort(BrokerEndpointManager endpoint, String mode,
                      Optional<String> backupServerWsURL) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint must not be null");
        }
        this.endpoint = endpoint;

        if (mode.equals(PRIMARY_MODE)) {
            this.mode = new PrimaryMode(this, backupServerWsURL);
        } else if (mode.equals(BACKUP_MODE)) {
            this.mode = new BackupMode(this);
        } else {
            throw new IllegalArgumentException
                ("mode must be " + PRIMARY_MODE + " or " + BACKUP_MODE);
        }
    }

    public BrokerEndpointManager getEndpoint() {
        return this.endpoint;
    }

    public void setVerbose(boolean verbose) {
        mode.setVerbose(verbose);
    }

    public BrokerMode getServerMode() {
        return mode;
    }

    public void setServerMode(BrokerMode mode) {
        this.mode = mode;
    }

    /* BrokerPortType implementation */

    @Override
    public String ping(String name) {
        return mode.ping(name);
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception,
               UnknownLocationFault_Exception {
        return mode.requestTransport(origin, destination, price);
    }

    @Override
    public TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception {
        return mode.viewTransport(id);
    }

    @Override
    public List<TransportView> listTransports() {
        return mode.listTransports();
    }

    @Override
    public void clearTransports() {
        mode.clearTransports();
    }

    @Override
    public void touch(String name) {
        mode.touch(name);
    }

    @Override
    public void updateRecord(ViewRecord record) {
        mode.updateRecord(record);
    }

    public void shutdown() {
        mode.shutdown();
    }
}
