package pt.upa.broker.ws;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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
public class BrokerPort implements BrokerPortType
{
    private BrokerEndpointManager endpoint;

    private List<TransportView> views = new ArrayList<>();

    public BrokerPort(BrokerEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    BrokerPort() { }

    /* BrokerPortType implementation */

    @Override
    public String ping(String name)
    {
        String msg = "";

        for (int no = 1; no < 10; ++no) {
            String transporterName = "UpaTransporter" + String.valueOf(no);

            TransporterClient client;
            try {
                client = new TransporterClient(endpoint.getUddiURL(), transporterName);
                msg += String.valueOf(no) + ". " + client.ping(name) + "\n";
            } catch (TransporterClientException e) {
                msg += String.valueOf(no) + ". " + e.getMessage() + "\n";
            }
        }

        return "Ping:\n" + msg;
    }

    private JobView findBestOffer(String origin, String destination, int price,
                                  List<JobView> allOffers)
        throws UnknownLocationFault_Exception, InvalidPriceFault_Exception
    {
        JobView bestOffer = null;

        for (int no = 1; no < 10; ++no) {
            final String transporterName = "UpaTransporter" + String.valueOf(no);
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

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception
    {
        // View for the requested transport.
        TransportView view = new TransportView();
        view.setOrigin(origin);
        view.setDestination(destination);
        view.setState(view.getState().REQUESTED);
        this.views.add(view);

        // Find the best offer.
        List<JobView> allOffers = new ArrayList<>();
        JobView bestOffer = findBestOffer(origin, destination, price, allOffers);

        if (bestOffer == null) { // Then there are no offers.
            view.setState(view.getState().FAILED);

            UnavailableTransportFault fault = new UnavailableTransportFault();
            fault.setOrigin(origin);
            fault.setDestination(destination);
            throw new UnavailableTransportFault_Exception("Unavailable Transport", fault);
        }

        view.setState(view.getState().BUDGETED);

        if (bestOffer.getJobPrice() > price) { // Then not a good enough price.
            view.setState(view.getState().FAILED);
            UnavailableTransportPriceFault fault = new UnavailableTransportPriceFault();
            fault.setBestPriceFound(bestOffer.getJobPrice());
            throw new UnavailableTransportPriceFault_Exception("Price too high", fault);
        }

        // Set view parameters for requested transport accordingly.
        view.setId(bestOffer.getJobIdentifier());
        view.setPrice(bestOffer.getJobPrice());
        view.setTransporterCompany(bestOffer.getCompanyName());

        // Accept best offer and reject all others.
        for (JobView job : allOffers) {
            try {
                final TransporterClient client =
                    new TransporterClient(endpoint.getUddiURL(), job.getCompanyName());

                boolean accept = bestOffer.getCompanyName().equals(job.getCompanyName());
                client.decideJob(job.getJobIdentifier(), accept);
                if (accept) {
                    view.setState(view.getState().BOOKED);
                }
            } catch (TransporterClientException | BadJobFault_Exception e) {
                e.printStackTrace();
            }
        }

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
    public List<TransportView> listTransports() {
        return this.views;
    }

    @Override
    public void clearTransports() {
        this.views.clear();

        for (int no = 1; no < 10; ++no) {
            String transporterName = "UpaTransporter" + String.valueOf(no);
            try {
                TransporterClient client =
                    new TransporterClient(endpoint.getUddiURL(), transporterName);
                client.clearJobs();
            } catch (TransporterClientException e) {
                e.printStackTrace();
            }
        }
    }

}
