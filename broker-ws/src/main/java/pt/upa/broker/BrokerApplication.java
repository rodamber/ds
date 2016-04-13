package pt.upa.broker;

import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.*;
import pt.upa.broker.ws.*;

public class BrokerApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
        if (args.length == 0 || args.length == 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BrokerApplication.class.getName() + " wsURL OR uddiURL wsName wsURL");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        
        //TransporterClient transporter1 = new TransporterClient();

        // Create server implementation object, according to options
        BrokerEndpointManager endpoint = null;
        if (args.length == 1) {
            wsURL = args[0];
            endpoint = new BrokerEndpointManager(wsURL);
        } else if (args.length >= 3) {
            uddiURL = args[0];
            wsName = args[1];
            wsURL = args[2];
            endpoint = new BrokerEndpointManager(uddiURL, wsName, wsURL);
            endpoint.setVerbose(true);
        }

        try {
            endpoint.start();
            endpoint.awaitConnections();
        } finally {
            endpoint.stop();
        }
	}

}
