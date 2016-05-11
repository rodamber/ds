package pt.upa.broker.ws;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import pt.upa.broker.ws.cli.*;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;

public class PrimaryMode extends BrokerMode {
    private static final int IM_ALIVE_TOUCH_INTERVAL = 5 * 1000;
    private static final String IM_ALIVE_TOUCH_MSG = "I'm Alive";

    private Optional<String> backupServerWsURL = Optional.empty();

    private List<String> transporters = new ArrayList<>();

    private Timer timer = new Timer();

    public PrimaryMode(BrokerPort port, Optional<String> backupServerWsURL) {
        this(port);
        this.backupServerWsURL = backupServerWsURL;
        if (backupServerWsURL.isPresent()) {
            touchBackupServer(IM_ALIVE_TOUCH_MSG, IM_ALIVE_TOUCH_INTERVAL);
            System.out.println("Backup server is running at " +
                               port.getEndpoint().getWsURL());
        }
    }

    public PrimaryMode(BrokerPort port) {
        super(port);
        final String upa = "UpaTransporter";
        for (int i = 1; i < 10; ++i) {
            this.transporters.add(upa + String.valueOf(i));
        }
    }

    public PrimaryMode(BackupMode backupMode) {
        this(backupMode.port);
        this.records = backupMode.records;

        try {
            port.getEndpoint().publishToUDDI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String ping(String name)
    {
        String msg = "";
        for (final String transp : this.transporters) {
            try {
                TransporterClient client =
                    new TransporterClient(port.getEndpoint().getUddiURL(), transp);
                msg += transp + " - " + client.ping(name);
            } catch (TransporterClientException e) {
                msg += transp + " - " + e.getMessage();
            }
        }
        return "Ping: " + msg;
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception,
               UnknownLocationFault_Exception {
        final ViewRecord record = newViewRecord(origin, destination);
        updateViewState(record.getKey(), record.getView().getState().REQUESTED);

        List<JobView> allOffers = new ArrayList<>();
        JobView bestOffer = findBestOffer(origin, destination, price, allOffers);

        assertHasOffer(record, bestOffer);
        updateViewState(record.getKey(), record.getView().getState().BUDGETED);

        assertGoodOffer(record, bestOffer, price);
        updateViewInfo(record.getView(), bestOffer);

        acceptBestAndRejectAllOtherOffers(record, bestOffer, allOffers);

        return record.getView().getId();
    }

    @Override
    public TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception {
        UnknownTransportFault fault = new UnknownTransportFault();
        fault.setId(id);

        if (id == null || id.equals("")) {
            throw new UnknownTransportFault_Exception("Null or empty id", fault);
        }

        final Optional<ViewRecord> optRecord = getRecordByViewId(id);

        if (!optRecord.isPresent()) {
            throw new UnknownTransportFault_Exception("Unknown transport", fault);
        }
        final TransportView view = optRecord.get().getView();

        if (view.getState().equals(TransportStateView.COMPLETED)) {
            return view;
        }

        try {
            final TransporterClient client =
                new TransporterClient(port.getEndpoint().getUddiURL(),
                                      view.getTransporterCompany());
            final JobStateView jobState = client.jobStatus(id).getJobState();
            final ViewRecord record = getRecordByViewId(view.getId()).get();
            // Get the most recent state for the view.
            if (jobState.equals(JobStateView.HEADING)) {
                updateViewState(record.getKey(), record.getView().getState().HEADING);
            } else if (jobState.equals(JobStateView.ONGOING)) {
                updateViewState(record.getKey(), record.getView().getState().ONGOING);
            } else if (jobState.equals(JobStateView.COMPLETED)) {
                updateViewState(record.getKey(), record.getView().getState().COMPLETED);
            }
        } catch (TransporterClientException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void clearTransports() {
        // Clear primary server state
        super.clearTransports();
        // Clear backup server state
        if (backupServerWsURL.isPresent()) {
            try {
                new BrokerClient(backupServerWsURL.get()).clearTransports();
            } catch (BrokerClientException e) {
                e.printStackTrace();
            }
        }
        // Clear transporters state
        for (final String transporterName: this.transporters) {
            try {
                TransporterClient client =
                    new TransporterClient(port.getEndpoint().getUddiURL(), transporterName);
                client.clearJobs();
            } catch (TransporterClientException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update view state in the primary and backup servers (if applicable).
     */
    @Override
    public void updateViewState(int key, TransportStateView newState) {
        super.updateViewState(key, newState);
        if (backupServerWsURL.isPresent()) {
            try {
                new BrokerClient(backupServerWsURL.get()).updateViewState(key, newState);
            } catch (BrokerClientException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds new view to the primary and backup servers (if applicable).
     */
    @Override
    public void addViewRecord(ViewRecord re) {
        super.addViewRecord(re);
        System.out.printf("Added new view to primary server with key %d%n", re.getKey());

        if (backupServerWsURL.isPresent()) {
            try {
                new BrokerClient(backupServerWsURL.get()).addViewRecord(re);
                System.out.println("Added view to backup server");
            } catch (BrokerClientException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  Notifies (touches) the backup server that the primary is up and
     *  schedules another touch.
     */
    private void touchBackupServer(String msg, int interval) {
        if (!backupServerWsURL.isPresent()) {
            System.out.println("No backup server URL was given");
            return;
        }

        try {
            BrokerClient client = new BrokerClient(backupServerWsURL.get());

            client.touch(msg);
            System.out.println("Touched backup server");
        } catch (BrokerClientException e) {
            e.printStackTrace();
        }

        timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    touchBackupServer(msg, interval);
                }
            }, interval);
    }

    @Override
    public void shutdown() {
        timer.cancel();
        timer.purge();
    }

    @Override
    public String toString() {
        return BrokerPort.PRIMARY_MODE;
    }

    /****************************** Helpers ***********************************/

    private JobView findBestOffer(String origin, String destination, int price,
                                  List<JobView> allOffers)
        throws UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        JobView bestOffer = null;
        for (final String transporterName: this.transporters) {
            try {
                final TransporterClient client =
                    new TransporterClient(port.getEndpoint().getUddiURL(), transporterName);
                final JobView offer =
                    client.requestJob(origin, destination, price);
                if (offer == null) {
                    continue;
                }
                allOffers.add(offer);
                if (bestOffer == null ||
                    offer.getJobPrice() < bestOffer.getJobPrice()) {
                    bestOffer = offer;
                }
            } catch (BadLocationFault_Exception e) {
                UnknownLocationFault fault = new UnknownLocationFault();
                fault.setLocation(e.getFaultInfo().getLocation());
                throw new UnknownLocationFault_Exception("Unknown location", fault);
            } catch (BadPriceFault_Exception e) {
                InvalidPriceFault fault = new InvalidPriceFault();
                fault.setPrice(e.getFaultInfo().getPrice());
                throw new InvalidPriceFault_Exception("Invalid price", fault);
            } catch (TransporterClientException e) {
                // No problem, just keep searching for better offers.
                e.printStackTrace();
            }
        }
        return bestOffer;
    }

    private ViewRecord newViewRecord(String origin, String destination) {
        final TransportView view = new TransportView();
        view.setOrigin(origin);
        view.setDestination(destination);

        ViewRecord record = new ViewRecord();
        record.setKey(maxCurrentKey++);
        record.setView(view);

        addViewRecord(record);
        return record;
    }

    private void assertHasOffer(ViewRecord record, JobView offer)
        throws UnavailableTransportFault_Exception {
        if (offer == null) { // Then there are no offers.
            updateViewState(record.getKey(), record.getView().getState().FAILED);

            UnavailableTransportFault fault = new UnavailableTransportFault();
            fault.setOrigin(record.getView().getOrigin());
            fault.setDestination(record.getView().getDestination());
            throw new UnavailableTransportFault_Exception("Unavailable Transport", fault);
        }
    }

    private void assertGoodOffer(ViewRecord record, JobView offer, int price)
        throws UnavailableTransportPriceFault_Exception {
        if (offer.getJobPrice() > price) { // Then not a good enough price.
            updateViewState(record.getKey(), record.getView().getState().FAILED);

            UnavailableTransportPriceFault fault = new UnavailableTransportPriceFault();
            fault.setBestPriceFound(offer.getJobPrice());
            throw new UnavailableTransportPriceFault_Exception("Price too high", fault);
        }
    }

    private void updateViewInfo(TransportView view, JobView offer) {
        view.setId(offer.getJobIdentifier());
        view.setPrice(offer.getJobPrice());
        view.setTransporterCompany(offer.getCompanyName());
    }

    private void acceptBestAndRejectAllOtherOffers(ViewRecord record,
                                                   JobView bestOffer,
                                                   List<JobView> allOffers) {
        for (JobView job : allOffers) {
            try {
                final TransporterClient client =
                    new TransporterClient(port.getEndpoint().getUddiURL(),
                                          job.getCompanyName());
                boolean accept = bestOffer.getCompanyName().equals(job.getCompanyName());
                client.decideJob(job.getJobIdentifier(), accept);
                if (accept) {
                    updateViewState(record.getKey(), record.getView().getState().BOOKED);
                }
            } catch (TransporterClientException | BadJobFault_Exception e) {
                e.printStackTrace();
            }
        }
    }

}
