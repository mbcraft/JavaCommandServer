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

import java.io.BufferedReader;
import java.util.Map;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public interface ICommand {
    
    public Map<String,ParameterDescription> getParameterMap();
    
    public void setParameter(String key,Object value);
    
    public Object getParameter(String key);
        
    public void validate() throws ValidationException;
    
    public void execute() throws ValidationException,ExecutionException;
    
    public int getExecutionLogCharLength();
    
    public BufferedReader getExecutionLogReader();
    
    public String getExecutionLogAsString();
}
