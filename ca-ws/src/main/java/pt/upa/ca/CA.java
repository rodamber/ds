package pt.upa.ca;

import javax.jws.WebService;

@WebService
public interface CA {

	String ping(String message);
	
	boolean verifyCertificate(String certificate);

}
