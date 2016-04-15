package pt.upa.broker.ws.cli;

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

import pt.upa.broker.ws.cli.*;
import pt.upa.broker.ws.*;
import mockit.Mocked;
import mockit.Verifications;
import mockit.Expectations;


/**
 *  Unit Test suite using a mocked (simulated) service and port
 */
public class BrokerClientServicePortMockTest {

    // static members

	/** mocked web service endpoint address */
	private static String wsURL = "http://host:port/endpoint";

	
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {
    }


    // members
    
    /** used for the BindingProvider request context */
	Map<String,Object> contextMap = null;


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	contextMap = new HashMap<String,Object>();
    }

    @After
    public void tearDown() {
    	contextMap = null;
    }


    // tests
    // assertEquals(expected, actual);

    /**
     *  In this test the server is mocked to
     *  simulate a communication exception.
     */
    @Test(expected=WebServiceException.class)
    public <P extends BrokerPortType & BindingProvider> void testMockServerException(
        @Mocked final BrokerService service,
        @Mocked final P port)
        throws Exception {

    	
        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new Expectations() {{
            new BrokerService();
            service.getBrokerPort(); result = port;
            port.getRequestContext(); result = contextMap;
            //port.sum(anyInt, anyInt);
            port.ping(anyString);
            result = new WebServiceException("fabricated");
        }};


        // Unit under test is exercised.
        BrokerClient client = new BrokerClient(wsURL);
        // call to mocked server
        //client.sum(1,2);
        client.ping("teste");
    }

    /**
     *  In this test the server is mocked to
     *  simulate a communication exception on a second call.
     */
    @Test
    public <P extends BrokerPortType & BindingProvider> void testMockServerExceptionOnSecondCall(
        @Mocked final BrokerService service,
        @Mocked final P port)
        throws Exception {

        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new Expectations() {{
            new BrokerService();
            service.getBrokerPort(); result = port;
            port.getRequestContext(); result = contextMap;
            port.ping("teste");
            // first call to sum returns the result
            //result = 3;
            result = "Ping: teste";
            // second call throws an exception
            result = new WebServiceException("fabricated");
        }};


        // Unit under test is exercised.
        BrokerClient client = new BrokerClient(wsURL);

        // first call to mocked server
        try {
            client.ping("teste");
        } catch(WebServiceException e) {
            // exception is not expected
            fail();
        }

        // second call to mocked server
        try {
            client.ping("teste");
            fail();
        } catch(WebServiceException e) {
            assertEquals("fabricated", e.getMessage());
        }
    }

}
