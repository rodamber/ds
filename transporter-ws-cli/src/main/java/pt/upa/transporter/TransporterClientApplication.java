package pt.upa.transporter;

import pt.upa.transporter.ws.cli.*;
import java.util.concurrent.TimeUnit;
import pt.upa.transporter.ws.*;


public class TransporterClientApplication {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + TransporterClientApplication.class.getName() + " wsURL OR uddiURL wsName");
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
        TransporterClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new TransporterClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
            client = new TransporterClient(uddiURL, wsName);
        }

		System.out.println("Invoking ping(\"Sistemas Distribuídos são fantásticos\")");
		String result = client.ping("Sistemas Distribuídos são fantásticos");
		System.out.println(result);
		
		//System.out.println(randInt(1,5));
		JobView jb = client.requestJob("Setúbal", "Évora", 50);
		JobView jb1 = client.requestJob("Évora", "Setúbal", 29);
		
		String jbID = jb.getJobIdentifier();
		String jbID1 = jb1.getJobIdentifier();
		
		System.out.println(jbID);
		System.out.println(jbID1);
		
		System.out.println(String.valueOf(jb.getJobPrice()));
		System.out.println(String.valueOf(jb1.getJobPrice()));
		
		System.out.println(jb.getJobState().name());
		System.out.println(jb1.getJobState().name());
		
		jb = client.decideJob(jbID, true);
		jb1 = client.decideJob(jbID1, true);

		System.out.println(client.jobStatus(jbID).getJobState());
		System.out.println(client.jobStatus(jbID1).getJobState());
		
		TimeUnit.SECONDS.sleep(5);
		System.out.println(client.jobStatus(jbID).getJobState());
		System.out.println(client.jobStatus(jbID1).getJobState());
		
		TimeUnit.SECONDS.sleep(5);
		System.out.println(client.jobStatus(jbID).getJobState());
		System.out.println(client.jobStatus(jbID1).getJobState());
    }
}
