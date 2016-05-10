package pt.upa.broker.ws;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;


public class BackupMode extends BrokerMode {
    private static final int PRIMARY_SERVER_TOUCH_INTERVAL = 10 * 1000;

    private Timer timer = new Timer();

    private boolean touched = false;

    public BackupMode(BrokerPort port) {
        super(port);
        timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    recover();
                }
            }, 60 * 1000, PRIMARY_SERVER_TOUCH_INTERVAL);
    }

    @Override
    public String ping(String name) {
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

    @Override
    public void updateViewState(String id, TransportStateView newState) {
        super.updateViewState(id, newState);
        System.out.println("Received new view from primary server.");
    }

    @Override
    public void touch(String msg) {
        this.touched = true;
        System.out.println("Primary server says: " + msg);
    }

    @Override
    public void addView(TransportView tv) {
        super.addView(tv);
        System.out.println("Received new view from primary server with id " +
                           tv.getId() + ".");
    }

    private void recover() {
        if (this.touched) {
            this.touched = false;
            return;
        }

        timer.cancel();
        timer.purge();
        port.setServerMode(new PrimaryMode(this));

        // RODRIGO:TODO:Republish to UDDI

        System.out.println("Backup server is now the primary server.");
    }

}
