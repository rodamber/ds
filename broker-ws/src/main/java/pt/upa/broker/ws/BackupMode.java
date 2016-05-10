package pt.upa.broker.ws;

import java.util.Optional;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;


public class BackupMode extends BrokerMode {

    public BackupMode(BrokerEndpointManager endpoint) {
        super(endpoint);
    }

    @Override
    public String ping(String name) {
        // TODO
        return "OK";
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception,
               UnknownLocationFault_Exception {
        UnavailableTransportFault fault = new UnavailableTransportFault();
        fault.setOrigin(origin);
        fault.setDestination(destination);
        throw new UnavailableTransportFault_Exception("Unavailable Transport", fault);
    }

    @Override
    public TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception {
        UnknownTransportFault fault = new UnknownTransportFault();
        fault.setId(id);
        throw new UnknownTransportFault_Exception("Unknown id", fault);
    }

}
