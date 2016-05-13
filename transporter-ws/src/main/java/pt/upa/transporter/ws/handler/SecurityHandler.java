package pt.upa.transporter.ws.handler;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

import pt.upa.ca.*;
import static pt.upa.ca.cli.CAClient.*;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *  This SOAPHandler shows how to set/get values from headers in
 *  inbound/outbound SOAP messages.
 *
 *  A header is created in an outbound message and is read on an
 *  inbound message.
 *
 *  The value that is read from the header
 *  is placed in a SOAP message context property
 *  that can be accessed by other handlers or by the application.
 */
public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

	private CA CAPort = new CAImplService().getCAImplPort();
	private ArrayList<String> usedNonce = new ArrayList<String>();

	private final static String CERTIFICATE_PATH = "src/main/resources/cer/";
	private final static String KEYSTORE_PATH = "src/main/resources/jks/";
	private final static String KEYSTORE_PASSWORD = "ac61d67c18c83e4a6ef4c90c32775e8860945830";
	private static String KEY_ALIAS;
	private final static String KEY_PASSWORD = "ac61d67c18c83e4a6ef4c90c32775e8860945830";

    public static final String CONTEXT_PROPERTY = "my.property";

    //
    // Handler interface methods
    //
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("AddHeaderHandler: Handling message.");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        String transporter = (String) smc.get("Transporter");	
        
        KEY_ALIAS = transporter;
        
        final PrivateKey privateKey = getPrivateKeyFromKeystore(KEYSTORE_PATH+transporter+".jks",KEYSTORE_PASSWORD.toCharArray(),KEY_ALIAS,KEY_PASSWORD.toCharArray());


        try {
            if (outboundElement.booleanValue()) {
                System.out.println("Writing body in outbound SOAP message...");

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                SOAPBody sb = se.getBody();
                if (sb == null)
                    sb = se.addBody();

                // add body element (name, namespace prefix, namespace)
                Name nonceName = se.createName("nonce", "ns2", "http://nonce.upa.pt/");
                SOAPBodyElement nonceElement = sb.addBodyElement(nonceName);

                // add header element value
                nonceElement.addTextNode(generateNonce());
                

                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add header element (name, namespace prefix, namespace)
                Name digest = se.createName("digest", "ns2", "http://digest.upa.pt/");
                SOAPHeaderElement digestElement = sh.addHeaderElement(digest);

                // add header element (name, namespace prefix, namespace)
                Name name = se.createName("name", "ns2", "http://name.upa.pt/");
                SOAPHeaderElement nameElement = sh.addHeaderElement(name);
                
                nameElement.addTextNode(transporter);
                                
                // sign body
                String bodySignature = printHexBinary(makeDigitalSignature(sb.toString().getBytes(), privateKey));
                digestElement.addTextNode(bodySignature);

            } else {
                System.out.println("Reading body in inbound SOAP message...");

                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPBody sb = se.getBody();

                SOAPHeader sh = se.getHeader();
                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                // check body
                if (sb == null) {
                    System.out.println("Body not found.");
                    return true;
                }

                // get digest
                Name elem = se.createName("digest", "ns2", "http://digest.upa.pt/");
                Iterator it = sh.getChildElements(elem);
                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header digest element not found.");
                    return false;
                }
                SOAPElement element = (SOAPElement) it.next();
                String digest = element.getValue();
                
                // get name
                elem = se.createName("name", "ns2", "http://name.upa.pt/");
                it = sh.getChildElements(elem);
                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header name element not found.");
                    return false;
                }
                element = (SOAPElement) it.next();
                String name = element.getValue();
                
                String pem = CAPort.getPEMCertificate(name);
                Certificate c = PEMtoCertificate(pem);
                boolean isValidSignature = verifyDigitalSignature(parseHexBinary(digest),sb.toString().getBytes(),c.getPublicKey());
                
                if(!isValidSignature){
                	return false;
                }

                // get first body element
                elem = se.createName("nonce", "ns2", "http://nonce.upa.pt/");
                it = sb.getChildElements(elem);
                // check body element
                if (!it.hasNext()) {
                    System.out.println("nonce element not found.");
                    return false;
                }
                element = (SOAPElement) it.next();
                String nonce = element.getValue();

                if(!validNonce(nonce)){
                	return false;
                }

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
            System.out.println("Continue normal processing...");
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("Ignoring fault message...");
        return true;
    }

    public void close(MessageContext messageContext) {
    }


	public String generateNonce() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final byte array[] = new byte[16];
		random.nextBytes(array);

		String nonce = printHexBinary(array);

		if(usedNonce.contains(nonce))
			return generateNonce();

		usedNonce.add(nonce);
		return nonce;
	}


	public boolean validNonce(String nonce) {
		if(usedNonce.contains(nonce)){
			return false;
		}
		usedNonce.add(nonce);
		return true;
	}

}
