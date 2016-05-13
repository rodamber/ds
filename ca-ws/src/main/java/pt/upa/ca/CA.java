package pt.upa.ca;

import javax.jws.WebService;

import pt.upa.ca.exception.InvalidCertificateNameException;

@WebService
public interface CA {

	String ping(String message);
	boolean verifyCertificate(String certificate);
	String getPEMCertificate(String name) throws InvalidCertificateNameException;

}
