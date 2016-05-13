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

        final String verbose = ask(in, "Verbose [true or false]", args, 0);
        final String mode = ask(in, "Mode [PRIMARY_MODE or BACKUP_MODE]", args, 1);
        final String uddiIP = ask(in, "UDDI IP", args, 2);
        final String uddiPort = ask(in, "UDDI Port", args, 3);
        final String wsName = ask(in, "Web Service Name", args, 4);
        final String wsIP = ask(in, "This Server IP", args, 5);
        final String wsPort = ask(in, "This Server Port", args, 6);
        final String backupWsIP = askNullable(in, "Backup Server IP", args, 7);
        final String backupWsPort = askNullable(in, "Backup Server Port", args, 8);

        final String uddiURL = buildURL(uddiIP, uddiPort);
        final String wsURL = buildWsURL(wsIP, wsPort);
        final String backupWsURL = buildWsURL(backupWsIP, backupWsPort);

        final BrokerEndpointManager endpoint =
            new BrokerEndpointManager(uddiURL, wsName, wsURL, mode,
                                      Optional.ofNullable(backupWsURL));
        endpoint.setVerbose(Boolean.parseBoolean(verbose));

        try {
            endpoint.start();
            endpoint.awaitConnections();
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

}
