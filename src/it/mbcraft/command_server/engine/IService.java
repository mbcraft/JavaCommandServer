/*
 * 
 *    Copyright MBCRAFT di Marco Bagnaresi - © 2013-2016
 *    All rights reserved - Tutti i diritti riservati
 * 
 *    Mail : info [ at ] mbcraft [ dot ] it 
 *    Web : http://www.mbcraft.it
 * 
 */

package it.mbcraft.command_server.engine;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public interface IService {
    
    /**
     * Lancia il servizio
     */
    public void start();
    
    /**
     * Ferma il servizio
     */
    public void stop();
    
    /**
     * Controlla se il servizio è attivo. Se necessario effettua le dovute
     * pulizie.
     */
    public boolean isRunning();
}
