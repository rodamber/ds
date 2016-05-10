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
    wsdlLocation      = "broker.1_0.wsdl",
    serviceName       = "BrokerService",
    portName          = "BrokerPort",
    endpointInterface = "pt.upa.broker.ws.BrokerPortType"
)
public class BrokerPort implements BrokerPortType {
    // Represents the possible modes of a server.
    public static final int PRIMARY_MODE = 0;
    public static final int SECONDARY_MODE = 1;

    private BrokerMode mode;

    public BrokerPort(BrokerEndpointManager endpoint, int mode,
                      Optional<String> backupServerURL) {
        if (mode == PRIMARY_MODE) {
            try{
                this.mode = new PrimaryMode(endpoint, backupServerURL.get());
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Must provide backupServerURL when starting in primary mode", e);
            }
        } else if (mode == SECONDARY_MODE) {
            // this.mode = new SecondaryMode(endpoint);
        } else {
            throw new IllegalArgumentException("mode must be PRIMARY or SECONDARY");
        }
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
    public void updateViewState(String id, TransportStateView newState) {
        // mode.updateViewState(id, newState);
    }


    @Override
    public void addView(TransportView tv) {
        mode.addView(tv);
    }

    @Override
    public void touch(String name) {
        // mode.touch(name);
    }
}
