package pt.upa.transporter.ws;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.TimerTask;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import pt.upa.transporter.ws.handler.SecurityHandler;

@WebService(
    name              = "TransporterWebService",
    targetNamespace   = "http://ws.transporter.upa.pt/",
    wsdlLocation      = "transporter.1_0.wsdl",
    serviceName       = "TransporterService",
    portName          = "TransporterPort",
    endpointInterface = "pt.upa.transporter.ws.TransporterPortType"
)
@HandlerChain(file = "/transporter_handler-chain.xml")
public class TransporterPort implements TransporterPortType {
	@Resource
	private WebServiceContext webServiceContext;
	
	
    private TransporterEndpointManager endpoint;
    private List<JobView> registry = new ArrayList<JobView>();
    private int lastJobID = 0;
    private Timer timer = new Timer();

    public TransporterPort(TransporterEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    TransporterPort() { }

    /* TransporterPortType implementation */
    
    private void updateSmc(){
        MessageContext messageContext = webServiceContext.getMessageContext();
		messageContext.put(SecurityHandler.RESPONSE_PROPERTY, endpoint.getWsName());
    }
    
    @Override
    public String ping(String name) {
    	updateSmc();
        return "Ping: " + name;
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
        throws BadLocationFault_Exception, BadPriceFault_Exception {
    	updateSmc();

        int transporterID = getTransporterID(endpoint.getWsName());

        if(!isCityNameValid(origin)){
            throw new BadLocationFault_Exception(origin + " is not a valid city name", new BadLocationFault());
        }
        else if(!isCityNameValid(destination)){
            throw new BadLocationFault_Exception(destination + " is not a valid city name", new BadLocationFault());
        }
        else if(price < 0){
            throw new BadPriceFault_Exception("Price must be greater than 0", new BadPriceFault());
        }

        if(!transporterServesCity(transporterID, origin) || !transporterServesCity(transporterID, destination)){
            return null;
        }
        else if(price > 100){
            return null;
        }

        int newPrice;

        if(price <= 10 || price % 2 == transporterID % 2){
            //TODO FAZ OFERTA ABAIXO DO PREÇO
            newPrice = price - random(1,price);
        }
        else{
            //TODO FAZ OFERTA ACIMA DO PREÇO
            newPrice = price + random(1, 101 - price);
        }

        String jobID = generateJobID(transporterID, lastJobID++);

        JobView job = JobView(endpoint.getWsName(), jobID, origin, destination, newPrice);

        job.setJobState(JobStateView.PROPOSED);
        addToRegistry(job);

        return job;
    }

    @Override
    public JobView decideJob(String id, boolean accept)
        throws BadJobFault_Exception
    {
    	updateSmc();
        JobView job = searchRegistry(id);

        BadJobFault fault = new BadJobFault();
        fault.setId(id);

        if (job == null) {
            throw new BadJobFault_Exception("Null job", fault);
        }

        if (job.getJobState().equals(JobStateView.ACCEPTED) ||
            job.getJobState().equals(JobStateView.REJECTED)) {
            throw new BadJobFault_Exception("Duplicate job", fault);
        }

        if (job != null && accept) {
            job.setJobState(JobStateView.ACCEPTED);
            timer.schedule(new ChangeOfState(job, timer), ThreadLocalRandom.current().nextInt(1000,5000));
        } else {
            job.setJobState(JobStateView.REJECTED);
        }
        return job;
    }

    @Override
    public JobView jobStatus(String id) {
    	updateSmc();
        return searchRegistry(id);
    }

    @Override
    public List<JobView> listJobs() {
    	updateSmc();
        return registry;
    }

    @Override
    public void clearJobs() {
    	updateSmc();
        registry.clear();
    }

    /* ** ------------------------------------------------------------- ** */

    private List<String> northRegion = Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança");
    private List<String> centerRegion = Arrays.asList("Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda");
    private List<String> southRegion = Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja", "Faro");

    private boolean isCityNameValid(String cityname){
        return northRegion.contains(cityname) || centerRegion.contains(cityname) || southRegion.contains(cityname);
    }

    private boolean transporterServesCity(int transporterID, String cityname){
        return ((northRegion.contains(cityname) || centerRegion.contains(cityname)) && (transporterID%2) == 0) || ((centerRegion.contains(cityname) || southRegion.contains(cityname)) && (transporterID%2) == 1);
    }

    private int getTransporterID(String transporterWsName){
        return transporterWsName.charAt(transporterWsName.length()-1);
    }

    private static int random(int min, int max){
        return (new Random()).nextInt((max - min) + 1) + min;
    }

    private String generateJobID(int transporterID, int jobID){
        return "UpaTransporter" + Integer.toString(transporterID) + "-" + Integer.toString(jobID);
    }

    private JobView JobView(String companyName, String jobID, String origin, String destination, int price){
        JobView job = new JobView();
        job.setCompanyName(companyName);
        job.setJobIdentifier(jobID);
        job.setJobOrigin(origin);
        job.setJobDestination(destination);
        job.setJobPrice(price);
        return job;
    }

    private void addToRegistry(JobView j){
        registry.add(j);
    }

    private JobView searchRegistry(String jobID){
        for(JobView j : registry){
            if(j.getJobIdentifier().equals(jobID)){
                return j;
            }
        }
        return null;
    }

    class ChangeOfState extends TimerTask{
        private JobView j;
        private Timer t;

        public ChangeOfState(JobView job, Timer timer){
            j = job;
            t = timer;
        }

        @Override
        public void run(){
            switch(j.getJobState()){
            case ACCEPTED:
                j.setJobState(JobStateView.HEADING);
                t.schedule(new ChangeOfState(j, t), ThreadLocalRandom.current().nextInt(1000, 5000));
                System.out.println(this);
                this.cancel();
                break;
            case HEADING:
                j.setJobState(JobStateView.ONGOING);
                t.schedule(new ChangeOfState(j, t), ThreadLocalRandom.current().nextInt(1000, 5000));
                System.out.println(this);
                this.cancel();
                break;
            case ONGOING:
                j.setJobState(JobStateView.COMPLETED);
                t.schedule(new ChangeOfState(j, t), ThreadLocalRandom.current().nextInt(1000, 5000));
                this.cancel();
                break;
            default:
                t.purge();
                break;
            }
        }
    }
}
