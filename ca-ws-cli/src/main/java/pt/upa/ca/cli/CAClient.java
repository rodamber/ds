package pt.upa.ca.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.ws.BindingProvider;

// classes generated from WSDL
import pt.upa.ca.CA;
import pt.upa.ca.CAImplService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class CAClient {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", CAClient.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		CAImplService service = new CAImplService();
		CA port = service.getCAImplPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

		System.out.println("Remote call ...");
		String result = port.ping("CA is alive!");
		System.out.println(result);

		System.out.println("Verifying certificate ...");
		try{
			String PEM = getFileContents("../transporter-ws/src/main/resources/cer/UpaTransporter2.cer", StandardCharsets.UTF_8);
			boolean isValidCert = port.verifyCertificate(PEM);
			System.out.println("Certificate is " + (isValidCert?"":"in") + "valid");
		}
		catch (IOException e) {
			System.out.println("Failed to read local certificate");
		}
		
	}
	
	public static String getFileContents(String path, Charset encoding) throws IOException {
		  byte[] encoded = Files.readAllBytes(Paths.get(path));
		  return new String(encoded, encoding);
	}


}
