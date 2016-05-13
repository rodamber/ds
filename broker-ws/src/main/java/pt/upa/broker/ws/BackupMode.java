package pt.upa.broker.ws;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;


public class BackupMode extends BrokerMode {
    private static final int PRIMARY_SERVER_TOUCH_INTERVAL = 4 * 1000;

    private Timer timer = new Timer();

    private boolean touched = false;

    public BackupMode(BrokerPort port) {
        super(port);
        port.getEndpoint().unpublishFromUDDI();
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
        Optional<ViewRecord> optRecord = getRecordByViewId(id);

        if (optRecord.isPresent()) {
            return optRecord.get().getView();
        }

        UnknownTransportFault fault = new UnknownTransportFault();
        fault.setId(id);
        throw new UnknownTransportFault_Exception("Unknown id", fault);
    }

    @Override
    public void updateRecord(ViewRecord re) {
        super.updateRecord(re);
        maxCurrentKey = re.getKey();
    }

    @Override
    public void touch(String msg) {
        this.touched = true;
        if (verbose)
            System.out.println(". ");
    }

    private void recover() {
        if (this.touched) {
            this.touched = false;
            return;
        }

        if (verbose)
            System.out.println("X");

        timer.cancel();
        timer.purge();
        port.setServerMode(new PrimaryMode(this));

        if (verbose)
            System.out.println("Backup server is now the primary server");
    }

    @Override
    public void shutdown() {
        timer.cancel();
        timer.purge();
    }

    @Override
    public String toString() {
        return BrokerPort.BACKUP_MODE;
    }

}
