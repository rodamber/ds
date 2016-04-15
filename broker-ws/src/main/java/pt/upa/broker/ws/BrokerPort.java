package pt.upa.broker.ws;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.jws.WebService;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.broker.ws.*;
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
    private List<TransportView> registry = new ArrayList<>();

    public BrokerPort(BrokerEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    BrokerPort() { }

    public void addRegistry(TransportView view) {
        registry.add(view);
    }

    public List<TransportView> getRegistry() {
        return registry;
    }

    /* BrokerPortType implementation */

    @Override
    public String ping(String name) {
        String msg = "";

        for (int no = 0; no < 10; ++no) {
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

    @Override
    public String requestTransport(String origin, String destination, int price)
        throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
               UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
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


