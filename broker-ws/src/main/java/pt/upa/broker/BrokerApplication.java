package pt.upa.broker;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import javax.xml.ws.*;

import pt.ulisboa.tecnico.sdis.ws.uddi.*;
import pt.upa.transporter.ws.cli.*;
import pt.upa.broker.ws.*;

public class BrokerApplication {

    public static void main(String[] args) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        final String interact = ask(in, "Interaction [true or false]", args, 0);
        final String verbose = ask(in, "Verbose [true or false]", args, 1);
        final String mode = ask(in, "Mode [PRIMARY_MODE or BACKUP_MODE]", args, 2);
        final String uddiIP = ask(in, "UDDI IP", args, 3);
        final String uddiPort = ask(in, "UDDI Port", args, 4);
        final String wsName = ask(in, "Web Service Name", args, 5);
        final String wsIP = ask(in, "This Server IP", args, 6);
        final String wsPort = ask(in, "This Server Port", args, 7);
        final String backupWsIP = askNullable(in, "Backup Server IP", args, 8);
        final String backupWsPort = askNullable(in, "Backup Server Port", args, 9);

        final String uddiURL = buildURL(uddiIP, uddiPort);
        final String wsURL = buildWsURL(wsIP, wsPort);
        final String backupWsURL = buildWsURL(backupWsIP, backupWsPort);

        final BrokerEndpointManager endpoint =
            new BrokerEndpointManager(uddiURL, wsName, wsURL, mode,
                                      Optional.ofNullable(backupWsURL));
        endpoint.setVerbose(Boolean.parseBoolean(verbose));

        try {
            endpoint.start();
            if (Boolean.parseBoolean(interact)) {
                interact(endpoint);
            } else {
                if (Boolean.parseBoolean(verbose)) {
                    System.out.println("Press enter to quit");
                }
                in.read();
            }
        } finally {
            endpoint.stop();
        }
    }

    public static String ask(BufferedReader in, String prompt, String[] args,
                             int index) throws IOException {
        return ask(in, prompt, args, index, false);
    }

    public static String askNullable(BufferedReader in, String prompt,
                                     String[] args, int index) throws IOException {
        return ask(in, prompt, args, index, true);
    }

    public static String ask(BufferedReader in, String prompt, String[] args,
                             int index, boolean nullable) throws IOException {
        Optional<String> defVal = elemAt(args, index);

        System.out.printf("%s %s:\n> ", prompt,
                          "(default: " + (defVal.isPresent() ? defVal.get() : "none") + ")");

        final String input = in.readLine();
        if (input.isEmpty() && !defVal.isPresent() && !nullable) {
            throw new NullPointerException("Arguments missing!");
        } else if (input.isEmpty() && defVal.isPresent()){
            return defVal.get();
        } else if (input.isEmpty() && nullable) {
            return null;
        } else {
            return input;
        }
    }

    public static <T> Optional<T> elemAt(T[] array, int index) {
        if (array.length <= index) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(array[index]);
        }
    }

    public static String buildURL(String ip, String port) {
        if (ip == null || port == null) {
            return null;
        } else {
            return "http://" + ip + ":" + port;
        }
    }

    public static String buildWsURL(String ip, String port) {
        if (buildURL(ip, port) != null) {
            return buildURL(ip, port) + "/broker-ws/endpoint";
        } else {
            return null;
        }
    }

    public static void interact(BrokerEndpointManager endpoint) throws IOException {
        final BrokerPort portImpl = endpoint.getPort();
        System.out.println("Awaiting connections");

        final BufferedReader in =
            new BufferedReader(new InputStreamReader(System.in));
        final String prompt = ">>> ";
        final String helperMsg =
            "Enter an option [clear, list, view, ping, request or quit]";

        System.out.println(helperMsg);
        System.out.print(prompt);

        String input;
        while (!(input = in.readLine()).equals("quit")) {
            try {
                if (input.equals("clear")) {
                    portImpl.clearTransports();
                    System.out.println("Cleared transports.");
                } else if (input.equals("list")) {
                    System.out.println("Transport list: ");
                    portImpl.listTransports().stream().forEach(BrokerApplication::printView);
                } else if (input.equals("view")) {
                    System.out.println("Enter the id: ");
                    System.out.print(prompt);
                    input = in.readLine();
                    printView(portImpl.viewTransport(input));
                } else if (input.equals("ping")) {
                    System.out.println(portImpl.ping("Hello!"));
                } else if (input.equals("request")) {
                    System.out.println("Enter the origin: ");
                    System.out.print(prompt);
                    final String origin = in.readLine();
                    System.out.println("Enter the destination: ");
                    System.out.print(prompt);
                    final String destination = in.readLine();
                    System.out.println("Enter the price: ");
                    System.out.print(prompt);
                    final String price = in.readLine();
                    portImpl.requestTransport(origin, destination,
                                              Integer.parseInt(price));
                } else {
                    if (input != "") {
                        System.out.println("Unknown option.");
                    }
                }
                System.out.println(helperMsg);
                System.out.print(prompt);
            } catch (Exception e) {
                if (endpoint.isVerbose()) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void printView(TransportView view) {
        System.out.printf("Id: %s; Origin: %s; Destination: %s; Price: %s; State: %s%n",
                          view.getId(),
                          view.getOrigin(),
                          view.getDestination(),
                          view.getPrice(),
                          view.getState());
    }

}
