/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package it.mbcraft.command_server.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.ExceptionLogger;

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;

/**
 * Embedded HTTP/1.1 file server based on a non-blocking I/O model and capable
 * of direct channel (zero copy) data transfer.
 *
 * The document root will be a fixed dir.
 */
public class CommandServer implements IService {

    public static final int DEFAULT_LISTEN_PORT = 8081;

    private static CommandServer _instance = null;
    public static String DEFAULT_PACKAGE_PREFIX = "it.mbcraft.command_server.commands";
    public static String INTERNAL_COMMAND_PACKAGE_PREFIX = "it.mbcraft.command_server.engine.core_commands";

    private HttpServer server = null;
    private int listenPort = DEFAULT_LISTEN_PORT;
    private String packagePrefix = DEFAULT_PACKAGE_PREFIX;
    private boolean wasStopped = false;

    public static CommandServer getInstance() {
        if (_instance == null) {
            _instance = new CommandServer();
        }
        return _instance;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int lp) {
        if (server != null) {
            throw new IllegalStateException("Server is already running.");
        }
        listenPort = lp;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public void setPackagePrefix(String prefix) {
        packagePrefix = prefix;
    }

    /**
     * Starts the server
     *
     */
    @Override
    public void start() {

        try {
            System.out.print("Starting command server ...");

            SocketConfig config = SocketConfig.custom()
                    .setSoTimeout(15000)
                    .setTcpNoDelay(true)
                    .setBacklogSize(2)
                    .setSoKeepAlive(false)
                    .build();

            server = ServerBootstrap.bootstrap()
                    .setListenerPort(listenPort)
                    .setLocalAddress(InetAddress.getLoopbackAddress())
                    .setServerInfo("Java:CommandServer/1.1")
                    .setSocketConfig(config)
                    .setExceptionLogger(ExceptionLogger.NO_OP)
                    .registerHandler("*", new HttpCommandHandler())
                    .create();

            server.start();

            System.out.println("done!");
        } catch (IOException ex) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean wasStopped() {
        return wasStopped;
    }

    /**
     * Stops the server.
     */
    @Override
    public void stop() {
        try {
            wasStopped = true;
            server.shutdown(1, TimeUnit.MICROSECONDS);
            server.awaitTermination(1, TimeUnit.MINUTES);
            server = null;
        } catch (InterruptedException ex) {
            if (!wasStopped) {
                Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return server != null;
    }

}
