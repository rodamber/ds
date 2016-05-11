package pt.upa.ca;

import javax.jws.WebService;

@WebService(endpointInterface = "pt.upa.ca.CA")
public class CAImpl implements CA {

	public String ping(String message) {
		return "Ping: " + message;
	}

}
