package pt.upa.transporter.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.*;
import static org.junit.Assert.*;

import pt.upa.transporter.ws.cli.*;
import pt.upa.transporter.ws.*;

/**
 * Super class for integration test suites
 * 
 * Loads test properties from configuration file
 */
public abstract class BaseTransporterIT {

	private static final String TEST_PROP_FILE = "/test.properties";

	private static Properties props = null;
	protected static TransporterClient client = null;

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		props = new Properties();
		try {
			props.load(BaseTransporterIT.class.getResourceAsStream(TEST_PROP_FILE));
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		String uddiURL = props.getProperty("uddi.url");
		String wsName = props.getProperty("ws.name");
		String wsURL = props.getProperty("ws.url");

		if (uddiURL != null) {
			client = new TransporterClient(uddiURL, wsName);
		} else {
			client = new TransporterClient(wsURL);
		}
		client.setVerbose(true);

	}

	@AfterClass
	public static void cleanup() {
		client = null;
	}

}
