/*
 * 
 *    Copyright MBCRAFT di Marco Bagnaresi - © 2013-2016
 *    All rights reserved - Tutti i diritti riservati
 * 
 *    Mail : info [ at ] mbcraft [ dot ] it 
 *    Web : http://www.mbcraft.it
 * 
 */
package it.mbcraft.command_server;

import it.mbcraft.command_server.engine.CommandServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public class Main {
    
    public static final String SOFTWARE_NAME = "JavaCommandServer";
    public static final String SOFTWARE_VERSION = "1.0.0";
    public static final String SOFTWARE_VENDOR = "MBCRAFT";

    private static final String START_COMMAND = "--start";
    private static final String STOP_COMMAND = "--stop";

    public static void main(String[] args) {

        if (args.length > 0) {
            if (args[0].equals(START_COMMAND)) {
                start(args);
                return;
            }

            if (args[0].equals(STOP_COMMAND)) {
                stop(args);
                return;
            }

            unknownArgument(args);
        }
        printUsage();
    }

    private static void start(String[] args) {
        CommandServer cs = CommandServer.getInstance();
        if (args.length > 1) {
            String pp = args[1];
            cs.setPackagePrefix(pp);
        }
        if (args.length > 2) {
            int listenPort = Integer.parseInt(args[1]);
            cs.setListenPort(listenPort);
        }
        cs.start();
    }

    private static void stop(String[] args) {
        System.out.println("Trying to stop server ...");
        int listenPort = CommandServer.DEFAULT_LISTEN_PORT;
        if (args.length > 1) {
            listenPort = Integer.parseInt(args[1]);
        }

        try (Socket sk = new Socket(InetAddress.getLoopbackAddress(), listenPort)) {
            sk.setKeepAlive(true);
            sk.setReuseAddress(true);
            sk.setTcpNoDelay(true);
            sk.setReceiveBufferSize(1024);
            sk.getInputStream().available();
            PrintWriter pw = new PrintWriter(sk.getOutputStream());
            pw.write("POST /stop HTTP/1.1\n"
                    + "Host: localhost:"+listenPort+"\n"
                    + "Connection: close\n"
                    + "Content-Length: 15\n"
                    + "Cache-Control: max-age=0\n"
                    + "Accept: *\n"
                    + "Content-Type: application/x-www-form-urlencoded"
                    + "User-Agent: "+SOFTWARE_NAME+"/"+SOFTWARE_VERSION+"\n"
                    + "Accept-Language: *\n"
                    + "Accept-Encoding: *\n\n"
                    );
            pw.write("command=%2Fstop");
            pw.flush();            
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static final void unknownArgument(String[] args) {
        System.err.println("Unknown argument : " + args[0]);
        System.exit(1);
    }

    public static final void printUsage() {
        System.out.println("-------------------------------");
        System.out.println(SOFTWARE_NAME+" "+SOFTWARE_VERSION+" - A local http command server written in Java - powered by "+SOFTWARE_VENDOR);
        System.out.println("");
        System.out.println("Command line options :");
        System.out.println("");
        System.out.println("--start [packagePrefix] [listenPort] : starts the server. packagePrefix and listenPort are optional parameters.");
        System.out.println("");
        System.out.println("  Parameters:");
        System.out.println("");
        System.out.println("        [packagePrefix] : the java package prefix used when looking for command classes. Default is '" + CommandServer.DEFAULT_PACKAGE_PREFIX + "'.");
        System.out.println("");
        System.out.println("        [listenPort] : the port the command server listens to. The server is always binded to 'localhost' address.");
        System.out.println("");
        System.out.println("--stop [listenPort] : stops the server. listenPort is an optional parameter.");
        System.out.println("");
        System.out.println("  Parameters:");
        System.out.println("");
        System.out.println("        [listenPort] : the port the command server listens to. The server is always binded to 'localhost' address.");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("-------------------------------");
        System.exit(0);
    }
}
