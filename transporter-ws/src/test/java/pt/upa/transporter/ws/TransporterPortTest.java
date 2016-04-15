/*package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.Properties;
import java.io.InputStream;

public class TransporterPortTest {
    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }

    private TransporterPort localPort;
    private TransporterEndpointManager endpoint;
    private String wsURL;
    private String uddiURL;
    private String wsName;
    private int transporterID;

    @Before
    public void setUp() {
        try{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = classLoader.getResourceAsStream("my.properties");
            Properties p = new Properties();
            p.load(is);

            wsURL = p.getProperty("ws.url");
            uddiURL = p.getProperty("uddi.url");
            wsName = p.getProperty("ws.name");
            transporterID = wsName.charAt(wsName.length()-1);

            endpoint = new TransporterEndpointManager(uddiURL, wsName, wsURL);

            localPort = new TransporterPort(endpoint);
        } catch(Exception e){}
    }

    @After
    public void tearDown() {
        try{
            endpoint.stop();
            localPort = null;
        } catch (Exception e){}
    }


    // tests

    //TODO XXX FIXME
    public void testPing() {
        final String result = localPort.ping("SD");
        assertEquals("testPing should be \"Ping: SD\" got \"" + result + "\"", "Ping: SD", result);
    }

    @Test
    public void testValidJob() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Faro", "Portalegre", 25);

        if(transporterID % 2 == 0){
            assertEquals("testValidJob should be null but got \"" + j + "\"", null, j);
        }
        else{
            assertEquals("testValidJob should be \"Faro\" but got \"" + j + "\"", "Faro", j.getJobOrigin());
            assertEquals("testValidJob should be \"Portalegre\" but got \"" + j + "\"", "Portalegre", j.getJobDestination());
        }
    }

    @Test(expected = BadLocationFault_Exception.class)
    public void testBadOriginLocation() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Benfica", "Castelo Branco", 25);
    }

    @Test(expected = BadLocationFault_Exception.class)
    public void testBadDestinationLocation() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Lisboa", "Mafra", 25);
    }

    @Test
    public void testOppositeLocations() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Bragança", "Faro", 25);
        
        assertEquals("testOppositeLocations should be null but got \"" + j + "\"", null, j);
    }

    @Test(expected = BadPriceFault_Exception.class)
    public void testPriceBelowZero() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Lisboa", "Leiria", -25);
    }

    @Test
    public void testPriceAbove100() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Lisboa", "Leiria", 25000);

        assertEquals("testPriveAbove100 should be null but got \"" + j + "\"", null, j);
    }
    
    @Test
    public void testInvalidJobStatus() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Lisboa", "Leiria", 25);

        assertEquals(null, localPort.jobStatus("UpaTransporter" + Integer.toString(transporterID) + "-" + "1"));
	}

	@Test
    public void testJobStatus() throws BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Lisboa", "Leiria", 25);

        assertEquals(j, localPort.jobStatus("UpaTransporter" + Integer.toString(transporterID) + "-" + "0"));		
	}
	
	@Test
    public void testRejectedDecideJob() throws BadJobFault_Exception, BadLocationFault_Exception, BadPriceFault_Exception{
        JobView j = localPort.requestJob("Lisboa", "Leiria", 25);
        j = localPort.decideJob("UpaTransporter" + Integer.toString(transporterID) + "-0", false);

        assertEquals("testRejectedDecideJob should get REJECTED but got " + j.getJobState().name(), "REJECTED", j.getJobState().name());		
	}

	@Test
	public void testHigherPrice() throws BadLocationFault_Exception, BadPriceFault_Exception{
        int price;

        if(transporterID % 2 == 0)
        	price = 26;
        else
        	price = 25;

        JobView j = localPort.requestJob("Lisboa", "Leiria", price);

        assertTrue(j.getJobPrice() < price);
	}
}
*/