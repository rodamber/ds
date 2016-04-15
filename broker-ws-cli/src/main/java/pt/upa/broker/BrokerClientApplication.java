package pt.upa.broker;

import java.util.concurrent.TimeUnit;

import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.cli.*;

public class BrokerClientApplication {

	public static void main(String[] args) throws Exception {
		        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BrokerClientApplication.class.getName() + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

        // Create client
        BrokerClient client = null;	

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new BrokerClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
            client = new BrokerClient(uddiURL, wsName);
        }
        
        System.out.println("Invoking ping(\"Hello\")");
        String result = client.ping("Hello");
        System.out.println(result);
		
		String tvID = client.requestTransport("Faro", "Beja", 51);
		
		TransportView tv = client.viewTransport(tvID);
		
		System.out.println(tvID);
		
		System.out.println(String.valueOf(tv.getPrice()));
		
		System.out.println(tv.getState().name());
		
		System.out.println(client.viewTransport(tvID).getState());
		
		TimeUnit.SECONDS.sleep(5);
		System.out.println(client.viewTransport(tvID).getState());
		
		TimeUnit.SECONDS.sleep(5);
		System.out.println(client.viewTransport(tvID).getState());
	}

}
