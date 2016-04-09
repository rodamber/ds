package pt.upa.transporter;

import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class TransporterApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n", TransporterApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];
        
		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
        
        try {
            //TODO
            System.out.println(TransporterApplication.class.getSimpleName() + " starting...");
            
			endpoint = Endpoint.create(new ...);

			// publish endpoint
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);

			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);

			// wait
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
			System.in.read();
        }
        catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
        }

	}

}
