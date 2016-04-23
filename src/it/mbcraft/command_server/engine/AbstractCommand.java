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
import java.io.File;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public abstract class AbstractCommand implements ICommand {

    private final Map<String,Object> paramValues = new HashMap<>();
    protected final StringBuffer executionLog = new StringBuffer();
    private final Map<String,ParameterDescription> parameters = new HashMap<>();
    
    protected String getParameterAsString(String key) {
        return (String)getParameter(key);
    }
    
    protected File getParameterAsFile(String key) {
        return new File(getParameterAsString(key));
    }
    
    protected int getParameterAsInteger(String key) {
        return Integer.parseInt(getParameterAsString(key));
    }
    
    protected int getParameterAsId(String key) {
        return Integer.parseInt(getParameterAsString(key));
    }
    
    protected void addParameter(String key,ParameterType type,String description,ParameterCardinality cardinality) {
        parameters.put(key, new ParameterDescription(key,type,description,cardinality));
    }
    
    @Override
    public Map<String,ParameterDescription> getParameterMap() {
        return parameters;
    }
    
    @Override
    public void setParameter(String key, Object value) {
        paramValues.put(key,value);
    }

    @Override
    public Object getParameter(String key) {
        return paramValues.get(key);
    }
    
    @Override
    public void execute() throws ValidationException, ExecutionException {
        validate();
        executeImpl();
    }
    
    protected abstract void executeImpl() throws ExecutionException;
    
    @Override
    public final void validate() throws ValidationException {
        Map<String,ParameterDescription> paramMap = getParameterMap();
        Collection<ParameterDescription>  params = paramMap.values();
        for (ParameterDescription pd : params) {
            pd.check(paramValues);
        }
    }
    
    @Override
    public int getExecutionLogCharLength() {
        return executionLog.length();
    }

    @Override
    public BufferedReader getExecutionLogReader() {
        return new BufferedReader(new StringReader(executionLog.toString()));
    }
    
    @Override
    public String getExecutionLogAsString() {
        return executionLog.toString();
    }
    
}
