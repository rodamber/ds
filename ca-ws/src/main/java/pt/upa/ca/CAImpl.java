package pt.upa.ca;

import javax.jws.WebService;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

@WebService(endpointInterface = "pt.upa.ca.CA")
public class CAImpl implements CA {
	
	private final String CertificatePath = "src/main/resources/cer/";
	private final String CACertificatePath = CertificatePath + "CA.cer";
	private final Certificate CACertificate;
	private final PublicKey CAPublicKey;
	
	public CAImpl() {
		CACertificate = getCACertificate();
		CAPublicKey = CACertificate.getPublicKey();
	}

	public String ping(String message){
		return "Ping: " + message;
	}
	
	public boolean verifyCertificate(String cert){
		Certificate certificate;
		try{
			 certificate = PEMtoCertificate(cert);
		} catch (Exception e) {
			return false;
		}
		
		//VERIFICAR SE A CA ASSINOU O CERTIFICADO
		if(!verifySignedCertificate(certificate, CAPublicKey)){
			return false;
		}
		
		//VERIFICAR SE EXISTE ALGUM CERTIFICADO NA PASTA IGUAL A ESTE QUE RECEBI		
		for(File file: (new File(CertificatePath)).listFiles()){
			if(file.isFile()){
				try{
					if(readCertificateFile(CertificatePath + file.getName()).equals(certificate)){
						return true;
					}
				}
				catch(Exception e){
					continue;
				}
			}
		}
		
		return false;
	}
	
	
	
	/* ****************** */
	
	private Certificate PEMtoCertificate(String pem) throws CertificateException {
		InputStream is = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return cf.generateCertificate(is);
	}

	private Certificate getCACertificate(){
		try{
			return readCertificateFile(CACertificatePath);
		}
		catch (IOException e){
			System.out.println("File not found at " + CACertificatePath);
		}
		catch (CertificateException e){
			System.out.println("File at " + CACertificatePath + " is not a certificate");
		}
		return null;
	}
	
	public Certificate readCertificateFile(String certificateFilePath) throws CertificateException, IOException {
		
		FileInputStream fileContent = new FileInputStream(certificateFilePath);
		BufferedInputStream bufferedFileContent = new BufferedInputStream(fileContent);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bufferedFileContent.available() > 0) {
			return cf.generateCertificate(bufferedFileContent);
		}
		
		bufferedFileContent.close();
		fileContent.close();
		return null;
	}
	
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
			return false;
		}
		return true;
	}
}
