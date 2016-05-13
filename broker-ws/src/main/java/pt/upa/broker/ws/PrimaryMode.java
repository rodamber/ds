package pt.upa.broker.ws;

import java.util.*;
import java.util.Collections;
import static java.util.stream.Collectors.toList;

import pt.upa.broker.ws.cli.*;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.*;

public class PrimaryMode extends BrokerMode {
    private static final int IM_ALIVE_TOUCH_INTERVAL = 2 * 1000;
    private static final String IM_ALIVE_TOUCH_MSG = "I'm Alive";

    private int maxCurrentKey = 0;

    private Optional<String> backupServerWsURL = Optional.empty();

    private List<String> transporters = new ArrayList<>();

    private Timer timer = new Timer();

    public PrimaryMode(BrokerPort port, Optional<String> backupServerWsURL) {
        this(port);
        this.backupServerWsURL = backupServerWsURL;
        if (backupServerWsURL.isPresent()) {
            touchBackupServer(IM_ALIVE_TOUCH_MSG, IM_ALIVE_TOUCH_INTERVAL);
            if (verbose) {
                System.out.println("Backup server is running at " +
                                port.getEndpoint().getWsURL());
            }
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
        if (!records.isEmpty()) {
            this.maxCurrentKey = Collections.max(records.keySet());
        }

        try {
            port.getEndpoint().publishToUDDI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String ping(String name)
    {

        Collection<UDDIRecord> records = getAllTransporters();
        String msg = "";
        for (final UDDIRecord record : records) {
            try {
                TransporterClient client = new TransporterClient(record.getUrl());
                msg += record.getOrgName() + " - " + client.ping(name);
            } catch (TransporterClientException e) {
                msg += record.getOrgName() + " - " + e.getMessage();
            }
            msg += "\n";
        }
        return msg;
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception,
               UnknownLocationFault_Exception {
        final ViewRecord record = newViewRecord(origin, destination);
        record.getView().setState(TransportStateView.REQUESTED);

        List<JobView> allOffers = new ArrayList<>();
        JobView bestOffer = findBestOffer(origin, destination, price, allOffers);

        assertHasOffer(record, bestOffer);
        record.getView().setState(TransportStateView.BUDGETED);

        assertGoodOffer(record, bestOffer, price);
        updateViewInfo(record, bestOffer);

        acceptBestAndRejectAllOtherOffers(record, bestOffer, allOffers);

        updateRecord(record);
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
        final ViewRecord record = optRecord.get();

        if (record.getView().getState().equals(TransportStateView.COMPLETED)) {
            return record.getView();
        }

        try {
            final TransporterClient client =
                new TransporterClient(port.getEndpoint().getUddiURL(),
                                      record.getView().getTransporterCompany());
            final JobStateView jobState = client.jobStatus(id).getJobState();
            final TransportView view = record.getView();

            // Get the most recent state for the view.
            if (!jobState.toString().equals(view.getState().toString())) {
                if (jobState.equals(JobStateView.HEADING)) {
                    view.setState(TransportStateView.HEADING);
                } else if (jobState.equals(JobStateView.ONGOING)) {
                    view.setState(TransportStateView.ONGOING);
                } else if (jobState.equals(JobStateView.COMPLETED)) {
                    view.setState(TransportStateView.COMPLETED);
                }
                updateRecord(record);
            }
        } catch (TransporterClientException e) {
            e.printStackTrace();
        }
        return record.getView();
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
        Collection<UDDIRecord> records = getAllTransporters();
        for (final UDDIRecord record : records) {
            try {
                new TransporterClient(record.getUrl()).clearJobs();
            } catch (TransporterClientException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds new view to the primary and backup servers (if applicable).
     */
    @Override
    public void updateRecord(ViewRecord re) {
        super.updateRecord(re);
        if (backupServerWsURL.isPresent()) {
            try {
                new BrokerClient(backupServerWsURL.get()).updateRecord(re);
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
            return;
        }

        try {
            new BrokerClient(backupServerWsURL.get()).touch(msg);
            if (verbose)
                System.out.println(". ");
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

        Collection<UDDIRecord> records = getAllTransporters();
        JobView bestOffer = null;

        for (final UDDIRecord record : records) {
            try {
                final TransporterClient client =
                    new TransporterClient(record.getUrl());
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

        return record;
    }

    private void assertHasOffer(ViewRecord record, JobView offer)
        throws UnavailableTransportFault_Exception {
        if (offer == null) { // Then there are no offers.
            record.getView().setState(TransportStateView.FAILED);

            UnavailableTransportFault fault = new UnavailableTransportFault();
            fault.setOrigin(record.getView().getOrigin());
            fault.setDestination(record.getView().getDestination());
            throw new UnavailableTransportFault_Exception("Unavailable Transport", fault);
        }
    }

    private void assertGoodOffer(ViewRecord record, JobView offer, int price)
        throws UnavailableTransportPriceFault_Exception {
        if (offer.getJobPrice() > price) { // Then not a good enough price.
            record.getView().setState(TransportStateView.FAILED);

            UnavailableTransportPriceFault fault = new UnavailableTransportPriceFault();
            fault.setBestPriceFound(offer.getJobPrice());
            throw new UnavailableTransportPriceFault_Exception("Price too high", fault);
        }
    }

    private void updateViewInfo(ViewRecord record, JobView offer) {
        TransportView view = record.getView();
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
                    record.getView().setState(TransportStateView.BOOKED);
                }
            } catch (TransporterClientException | BadJobFault_Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Collection<UDDIRecord> getAllTransporters() {
        UDDINaming naming = null;
        Collection<UDDIRecord> records = null;
        try {
            naming = new UDDINaming(port.getEndpoint().getUddiURL());
            records = naming.listRecords("UpaTransporter%");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }


}
