/*
 * 
 *    Copyright MBCRAFT di Marco Bagnaresi - © 2013-2016
 *    All rights reserved - Tutti i diritti riservati
 * 
 *    Mail : info [ at ] mbcraft [ dot ] it 
 *    Web : http://www.mbcraft.it
 * 
 */

package it.mbcraft.command_server.engine.core_commands;

import it.mbcraft.command_server.engine.AbstractCommand;
import it.mbcraft.command_server.engine.CommandServer;
import it.mbcraft.command_server.engine.ExecutionException;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public class StopCommand extends AbstractCommand {

    @Override
    protected void executeImpl() throws ExecutionException {
        System.out.print("Stopping command server ...");
        executionLog.append("COMMAND SERVER STOPPED.");
        CommandServer.getInstance().stop();
        System.out.println("done!");
    }
    
}
