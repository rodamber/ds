package pt.upa.transporter.ws;

import java.util.List;
import javax.jws.WebService;

@WebService(
    name              = "TransporterWebService",
    targetNamespace   = "http://ws.transporter.upa.pt/",
    wsdlLocation      = "transporter.1_0.wsdl",
    serviceName       = "TransporterService",
    portName          = "TransporterPort",
    endpointInterface = "transporter.TransporterPortType"
)

public class TransporterPort implements TransporterPortType {

    private TransporterEndpointManager endpoint;

    public TransporterPort(TransporterEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    TransporterPort() { }

    /* TransporterPortType implementation */

    @Override
    public String ping(String name) {
        return "Ping: " + name;
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
        throws BadLocationFault_Exception, BadPriceFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public JobView decideJob(String id, boolean accept)
        throws BadJobFault_Exception {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public JobView jobStatus(String id) {
        /* RODRIGO:FIXME:TODO */
        return null;
    }


    @Override
    public List<JobView> listJobs() {
        /* RODRIGO:FIXME:TODO */
        return null;
    }

    @Override
    public void clearJobs() { /* RODRIGO:FIXME:TODO */ }

}
