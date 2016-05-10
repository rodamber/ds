package pt.upa.broker.ws;

import java.util.Optional;

import java.io.IOException;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.broker.ws.BrokerPort;
import pt.upa.transporter.ws.cli.*;

/** Endpoint manager */
public class BrokerEndpointManager {

    /** UDDI naming server location */
    private String uddiURL = null;
    /** Web Service name */
    private String wsName = null;

    /** Get Web Service UDDI publication name */
    public String getWsName() {
        return wsName;
    }

    public String getUddiURL() {
        return uddiURL;
    }

    /** Web Service location to publish */
    private String wsURL = null;

    /** Port implementation */
    private BrokerPort portImpl;

    public String getWsURL() {
        return wsURL;
    }

    /** Obtain Port implementation */
    public BrokerPortType getPort() {
        return portImpl;
    }

    /** Web Service endpoint */
    private Endpoint endpoint = null;
    /** UDDI Naming instance for contacting UDDI server */
    private UDDINaming uddiNaming = null;

    /** Get UDDI Naming instance for contacting UDDI server */
    UDDINaming getUddiNaming() {
        return uddiNaming;
    }

    /** output option **/
    private boolean verbose = true;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public BrokerEndpointManager(String uddiURL, String wsName, String wsURL,
                                 String mode, Optional<String> backupWsURL) {
        this(wsURL, mode, backupWsURL);
        this.uddiURL = uddiURL;
        this.wsName = wsName;
    }

    public BrokerEndpointManager(String wsURL, String mode,
                                 Optional<String> backupWsURL) {
        if (wsURL == null)
            throw new NullPointerException("Web Service URL cannot be null!");
        this.wsURL = wsURL;
        this.portImpl = new BrokerPort(this, mode, backupWsURL);
    }

    /* endpoint management */

    public void start() throws Exception {
        try {
            // publish endpoint
            endpoint = Endpoint.create(this.portImpl);
            if (verbose) {
                System.out.printf("Starting %s at %s%n", wsName, wsURL);
            }
            endpoint.publish(wsURL);
        } catch (Exception e) {
            endpoint = null;
            if (verbose) {
                System.out.printf("Caught exception when starting: %s%n", e);
                e.printStackTrace();
            }
            throw e;
        }
        publishToUDDI();
    }

    public void awaitConnections() {
        if (verbose) {
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");
        }
        try {
            System.in.read();
        } catch (IOException e) {
            if (verbose) {
                System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
            }
        }
    }

    public void stop() throws Exception {
        try {
            if (endpoint != null) {
                // stop endpoint
                endpoint.stop();
                if (verbose) {
                    System.out.printf("Stopped %s at %s%n", wsName, wsURL);
                }
            }
        } catch (Exception e) {
            if (verbose) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
        }
        this.portImpl = null;
        unpublishFromUDDI();
    }

    /* UDDI */

    void publishToUDDI() throws Exception {
        try {
            // publish to UDDI
            if (uddiURL != null) {
                if (verbose) {
                    System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
                }
                uddiNaming = new UDDINaming(uddiURL);
                uddiNaming.rebind(wsName, wsURL);
            }
        } catch (Exception e) {
            uddiNaming = null;
            if (verbose) {
                System.out.printf("Caught exception when binding to UDDI: %s%n", e);
            }
            throw e;
        }
    }

    void unpublishFromUDDI() {
        try {
            if (uddiNaming != null) {
                // delete from UDDI
                uddiNaming.unbind(wsName);
                if (verbose) {
                    System.out.printf("Unpublished '%s' from UDDI%n", wsName);
                }
                uddiNaming = null;
            }
        } catch (Exception e) {
            if (verbose) {
                System.out.printf("Caught exception when unbinding: %s%n", e);
            }
        }
    }

}
