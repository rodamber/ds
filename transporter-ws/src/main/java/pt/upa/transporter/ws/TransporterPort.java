package pt.upa.transporter.ws;

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

    TransporterPort() {
    }

    /* TransporterPortType implementation */

}
