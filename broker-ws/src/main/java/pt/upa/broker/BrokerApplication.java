package pt.upa.broker;

import java.util.Optional;

import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.*;
import pt.upa.broker.ws.*;

public class BrokerApplication {

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length == 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BrokerApplication.class.getName() +
                               " wsURL OR uddiURL wsName wsURL");
            return;
        }

        BrokerEndpointManager endpoint = null;
        if (args.length == 1) {
            final String wsURL = args[0];
            final String mode    = BrokerPort.PRIMARY_MODE;
            endpoint = new BrokerEndpointManager(wsURL, mode, Optional.empty());
        } else if (args.length >= 4) {
            final String uddiURL     = args[0];
            final String wsName      = args[1];
            final String wsURL       = args[2];
            final String mode        = args[3];
            final String backupWsURL = args[4];
            endpoint = new BrokerEndpointManager(uddiURL, wsName, wsURL, mode,
                                                 Optional.ofNullable(backupWsURL));
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
