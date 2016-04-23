/*
 * 
 *    Copyright MBCRAFT di Marco Bagnaresi - © 2015
 *    All rights reserved - Tutti i diritti riservati
 * 
 *    Mail : info [ at ] mbcraft [ dot ] it 
 *    Web : http://www.mbcraft.it
 * 
 */

package it.mbcraft.command_server.engine.http;

/**
 * Questa classe contiene l'errore http e la risposta restituita dal server in 
 * caso di status code != 200.
 * 
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public class HttpErrorException extends Exception {
    
    private final int statusCode;
    
    public HttpErrorException(int code, String content) {
        super(content);
        this.statusCode = code;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
