package pt.upa.broker.ws;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;


public class BackupMode extends BrokerMode {
    private static final int PRIMARY_SERVER_PING_INTERVAL = 10 * 1000;
    private static final String PRIMARY_SERVER_PING_MSG = "OK";

    private Timer timer = new Timer();

    public BackupMode(BrokerEndpointManager endpoint) {
        super(endpoint);
        ping("");
    }

    @Override
    public String ping(String name) {
        timer.cancel();
        timer.schedule(new TimerTask() {
               @Override
               public void run() {
                   recover();
               }
            }, PRIMARY_SERVER_PING_INTERVAL);
        return PRIMARY_SERVER_PING_MSG;
    }

    private void recover() {
        // TODO
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
