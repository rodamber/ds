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
    private static final String IM_ALIVE_TOUCH_MSG = "Up and running.";

    private Optional<String> backupServerWsURL = Optional.empty();
    private final Timer timer = new Timer();

    private List<String> transporters;

    public PrimaryMode(BrokerEndpointManager endpoint, String backupServerWsURL) {
        this(endpoint);
        this.backupServerWsURL = Optional.ofNullable(backupServerWsURL);

        touchBackupServer(IM_ALIVE_TOUCH_MSG, IM_ALIVE_TOUCH_INTERVAL);
    }

    private PrimaryMode(BrokerEndpointManager endpoint) {
        super(endpoint);

        final String upa = "UpaTransporter";
        for (int i = 1; i < 10; ++i) {
            this.transporters.add(upa + String.valueOf(i));
        }
    }

    @Override
    public String ping(String name)
    {
        String msg = "";
        for (final String transp : this.transporters) {
            try {
                TransporterClient client =
                    new TransporterClient(endpoint.getUddiURL(), transp);
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
        final TransportView view = addNewView(origin, destination);
        view.setState(view.getState().REQUESTED);

        List<JobView> allOffers = new ArrayList<>();
        JobView bestOffer = findBestOffer(origin, destination, price, allOffers);

        assertHasOffer(view, bestOffer);
        view.setState(view.getState().BUDGETED);

        assertGoodOffer(view, bestOffer, price);
        updateViewInfo(view, bestOffer);

        acceptBestAndRejectAllOtherOffers(view, bestOffer, allOffers);

        return view.getId();
    }

    @Override
    public TransportView viewTransport(String id)
        throws UnknownTransportFault_Exception
    {
        UnknownTransportFault fault = new UnknownTransportFault();
        fault.setId(id);

        if (id == null || id.equals("")) {
            throw new UnknownTransportFault_Exception("Null or empty id", fault);
        }

        TransportView view = null;
        for (TransportView v : this.views) {
            String vid = v.getId();
            if (vid != null && vid.equals(id)) {
                view = v;
                break;
            }
        }

        if (view == null) {
            throw new UnknownTransportFault_Exception("Unknown transport", fault);
        }

        if (view.getState().equals(TransportStateView.COMPLETED)) {
            return view;
        }

        try {
            TransporterClient client =
                new TransporterClient(endpoint.getUddiURL(),
                                      view.getTransporterCompany());
            JobStateView jobState = client.jobStatus(id).getJobState();

            if (jobState.equals(JobStateView.HEADING)) {
                view.setState(view.getState().HEADING);
            } else if (jobState.equals(JobStateView.ONGOING)) {
                view.setState(view.getState().ONGOING);
            } else if (jobState.equals(JobStateView.COMPLETED)) {
                view.setState(view.getState().COMPLETED);
            }
        } catch (TransporterClientException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void clearTransports() {
        super.clearTransports();
        for (final String transporterName: this.transporters) {
            try {
                TransporterClient client =
                    new TransporterClient(endpoint.getUddiURL(), transporterName);
                client.clearJobs();
            } catch (TransporterClientException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateViewState(String id, TransportStateView newState) {
        super.updateViewState(id, newState);
        if (this.backupServerWsURL.isPresent()) {
            try {
                new BrokerClient(this.backupServerWsURL.get()).updateViewState(id, newState);
            } catch (BrokerClientException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addView(TransportView tv) {
        super.addView(tv);
        if (this.backupServerWsURL.isPresent()) {
            try {
                new BrokerClient(this.backupServerWsURL.get()).addView(tv);
            } catch (BrokerClientException e) {
                e.printStackTrace();
            }
        }
    }

    private void touchBackupServer(String msg, int interval) {
        if (!backupServerWsURL.isPresent()) {
            System.out.println("No backup server URL was given");
            return;
        }

        System.out.println("TOUCH BackupServer at " + backupServerWsURL.get());
        try {
            BrokerClient client = new BrokerClient(backupServerWsURL.get());
            System.out.println("Created client for server at " + backupServerWsURL.get());

            client.touch(msg);
            System.out.println("Touched backup server.");
        } catch (BrokerClientException e) {
            System.out.println("Could not reach backup server.");
            e.printStackTrace();
        }

        timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    touchBackupServer(msg, interval);
                }
            }, interval);
        System.out.println("Rescheduled touch to backup server.");
    }

    /****************************** Helpers ***********************************/

    private JobView findBestOffer(String origin, String destination, int price,
                                  List<JobView> allOffers)
        throws UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        JobView bestOffer = null;
        for (final String transporterName: this.transporters) {
            try {
                final TransporterClient client =
                    new TransporterClient(endpoint.getUddiURL(), transporterName);
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

    private TransportView addNewView(String origin, String destination) {
        final TransportView view = new TransportView();
        view.setOrigin(origin);
        view.setDestination(destination);
        addView(view);
        return view;
    }

    private void assertHasOffer(TransportView view, JobView offer)
        throws UnavailableTransportFault_Exception {
        if (offer == null) { // Then there are no offers.
            view.setState(view.getState().FAILED);

            UnavailableTransportFault fault = new UnavailableTransportFault();
            fault.setOrigin(view.getOrigin());
            fault.setDestination(view.getDestination());
            throw new UnavailableTransportFault_Exception("Unavailable Transport", fault);
        }
    }

    private void assertGoodOffer(TransportView view, JobView offer, int price)
        throws UnavailableTransportPriceFault_Exception {
        if (offer.getJobPrice() > price) { // Then not a good enough price.
            view.setState(view.getState().FAILED);

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

    private void acceptBestAndRejectAllOtherOffers(TransportView view,
                                                   JobView bestOffer,
                                                   List<JobView> allOffers) {
        for (JobView job : allOffers) {
            try {
                final TransporterClient client =
                    new TransporterClient(endpoint.getUddiURL(),
                                          job.getCompanyName());
                boolean accept = bestOffer.getCompanyName().equals(job.getCompanyName());
                client.decideJob(job.getJobIdentifier(), accept);
                if (accept) {
                    view.setState(view.getState().BOOKED);
                }
            } catch (TransporterClientException | BadJobFault_Exception e) {
                e.printStackTrace();
            }
        }
    }

}
