package pt.upa.transporter.ws.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.cli.*;
import pt.upa.transporter.ws.*;
import mockit.Mocked;
import mockit.Verifications;
import mockit.Expectations;


/**
 *  Unit Test suite using a mocked (simulated) service and port
 */
public class TransporterClientServicePortMockTest {

	/** mocked web service endpoint address */
	private static String wsURL = "http://host:port/endpoint";


    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {
    }


    /** used for the BindingProvider request context */
	Map<String,Object> contextMap = null;

    @Before
    public void setUp() {
    	contextMap = new HashMap<String,Object>();
    }

    @After
    public void tearDown() {
    	contextMap = null;
    }
    
    /**
     *  In this test the server is mocked to
     *  simulate a communication exception.
     */
    @Test(expected=WebServiceException.class)
    public <P extends TransporterPortType & BindingProvider> void testMockServerException(
        @Mocked final TransporterService service,
        @Mocked final P port)
        throws Exception {

        new Expectations() {{
            new TransporterService();
            service.getTransporterPort(); result = port;
            port.getRequestContext(); result = contextMap;
            port.ping(anyString);
            result = new WebServiceException("fabricated");
        }};


        TransporterClient client = new TransporterClient(wsURL);
        client.ping("teste");
    }

    /**
     *  In this test the server is mocked to
     *  simulate a communication exception on a second call.
     */
    @Test
    public <P extends TransporterPortType & BindingProvider> void testMockServerExceptionOnSecondCall(
        @Mocked final TransporterService service,
        @Mocked final P port)
        throws Exception {

        new Expectations() {{
            new TransporterService();
            service.getTransporterPort(); result = port;
            port.getRequestContext(); result = contextMap;
            port.ping("teste");

            result = "Ping: teste";
            result = new WebServiceException("fabricated");
        }};


        TransporterClient client = new TransporterClient(wsURL);

        try {
            client.ping("teste");
        } catch(WebServiceException e) {
            fail();
        }

        try {
            client.ping("teste");
            fail();
        } catch(WebServiceException e) {
            assertEquals("fabricated", e.getMessage());
        }
    }

}