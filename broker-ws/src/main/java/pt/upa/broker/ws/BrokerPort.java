package pt.upa.broker.ws;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.jws.WebService;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

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

    private BrokerEndpointManager endpoint;

    private static List<String> northRegion  = new ArrayList<>(Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança"));
    private static List<String> centerRegion = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda"));
    private static List<String> southRegion  = new ArrayList<>(Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja", "Faro"));

    // RODRIGO:FIXME
    private List<TransportView> views = new ArrayList<>();

    public BrokerPort(BrokerEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    BrokerPort() { }

    public void addView(TransportView view) {
        views.add(view);
    }

    public List<TransportView> getViews() {
        return views;
    }

    /* BrokerPortType implementation */

    @Override
    public String ping(String name) {
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

    public boolean knownLocation(String location) {
        return northRegion.contains(location) || centerRegion.contains(location) || southRegion.contains(location);
    }

    private JobView findBestOffer(String origin, String destination, int price, List<JobView> allOffers)
        throws UnknownLocationFault_Exception, InvalidPriceFault_Exception {

        JobView bestOffer = null;

        for (int no = 1; no < 10; ++no) {
            final String transporterName = "UpaTransporter" + String.valueOf(no);

            try {
                final TransporterClient client = new TransporterClient(endpoint.getUddiURL(), transporterName);

                JobView offer = client.requestJob(origin, destination, price);
                allOffers.add(offer);

                if (bestOffer == null || offer != null)
                    if (offer.getJobPrice() < bestOffer.getJobPrice())
                        bestOffer = offer;
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
               UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {

        // View for the requested transport.
        TransportView view = new TransportView();
        view.setOrigin(origin);
        view.setDestination(destination);
        view.setState(view.getState().REQUESTED);
        addView(view);

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

            } catch (TransporterClientException | BadJobFault_Exception e) {
                e.printStackTrace();
            }
        }

        return view.getId();
    }

    @Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public List<TransportView> listTransports() {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public void clearTransports() {
        /* RODRIGO:FIXME:TODO */
    }

}
