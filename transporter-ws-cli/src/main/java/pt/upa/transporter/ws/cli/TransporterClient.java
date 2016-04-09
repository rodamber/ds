package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.upa.transporter.ws.*;
import java.util.List;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class TransporterClient implements TransporterPortType {

    /** WS service */
    TransporterService service = null;

    /** WS port (port type is the interface, port is the implementation) */
    TransporterPortType port = null;

    /** UDDI server URL */
    private String uddiURL = null;

    /** WS name */
    private String wsName = null;

    /** WS endpoint address */
    private String wsURL = null; // default value is defined inside WSDL

    public String getWsURL() {
        return wsURL;
    }

    /** output option **/
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** constructor with provided web service URL */
    public TransporterClient(String wsURL) throws TransporterClientException {
        this.wsURL = wsURL;
        createStub();
    }

    /** constructor with provided UDDI location and name */
    public TransporterClient(String uddiURL, String wsName) throws TransporterClientException {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() throws TransporterClientException {
        try {
            if (verbose)
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose)
                System.out.printf("Looking for '%s'%n", wsName);
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
            throw new TransporterClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format("Service with name %s not found on UDDI at %s", wsName, uddiURL);
            throw new TransporterClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
            System.out.println("Creating stub ...");
        service = new TransporterService();
        port = service.getTransporterPort();

        if (wsURL != null) {
            if (verbose)
                System.out.println("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }

    /* *********************************************
    
    CALC EXAMPLE

    public int sum(int a, int b) {
        return port.sum(a, b);
    }

    public int sub(int a, int b) {
        return port.sub(a, b);
    }

    public int mult(int a, int b) {
        return port.mult(a, b);
    }

    public int intdiv(int a, int b) throws DivideByZero {
        return port.intdiv(a, b);
    }
    
    ********************************************* */
    
    /* TransporterPortType implementation */

    @Override
    public String ping(String name) {
        return "Ping: " + name;
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
        throws BadLocationFault_Exception, BadPriceFault_Exception {
        /* TODO */
        return null;
    }

    @Override
    public JobView decideJob(String id, boolean accept)
        throws BadJobFault_Exception {
        /* TODO */
        return null;
    }

    @Override
    public JobView jobStatus(String id) {
        /* TODO */
        return null;
    }


    @Override
    public List<JobView> listJobs() {
        /* TODO */
        return null;
    }

    @Override
    public void clearJobs() { /* TODO */ }
}
