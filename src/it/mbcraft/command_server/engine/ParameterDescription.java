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

import java.io.File;
import java.util.Map;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public class ParameterDescription {
    
    private final String _name;
    private final ParameterType _type;
    private final String _description;
    private final ParameterCardinality _cardinality;
    
    public ParameterDescription(String name,ParameterType type, String description,ParameterCardinality cardinality) {
        _name = name;
        _type = type;
        _description = description;
        _cardinality = cardinality;
    }
    
    public void check(Map<String,Object> parameters) throws ValidationException {
        if (_cardinality==ParameterCardinality.MANDATORY && !parameters.containsKey(_name))
            throw new ValidationException("Mandatory parameter '"+_name+"' ("+_description+") not provided.");
        Object value = parameters.get(_name);
        switch (_type) {
            case PATH_IN_DIRECTORY:validatePathInDirectory(value);break;
            case EXISTING_FILE_OR_DIRECTORY:validateExistingFileOrDirectory(value);break;
            case EXISTING_FILE:validateExistingFile(value);break;
            case EXISTING_DIRECTORY:validateExistingDirectory(value);break;
            case STRING:validateString(value);break;
            case INTEGER:validateInteger(value);break;
            case ID:validateId(value);break;
            case BOOLEAN:validateBoolean(value);break;
            default:throw new ValidationException("Unknown parameter type in switch case.");
        }
    }

    private void validatePathInDirectory(Object value) throws ValidationException {
        
        File f = new File(value.toString());
        File dir = f.getParentFile();
        if (!dir.isDirectory() || !dir.exists())
            throw new ValidationException("The parameter "+_name+" is not inside an existing directory : "+value);
        if (!dir.canWrite())
            throw new ValidationException("The parameter "+_name+" is not inside a writable directory : "+value);
        
    }

    private void validateString(Object value) throws ValidationException {
        if (value==null) throw new ValidationException("The parameter "+_name+" is not a valid string (null).");
    }

    private void validateInteger(Object value) throws ValidationException {
        try {
            Integer.parseInt(value.toString());
        } catch (Exception ex) {
            throw new ValidationException("The parameter "+_name+" is not a valid integer value : "+value);
        }
    }

    private void validateId(Object value) throws ValidationException {
        try {
            int val = Integer.parseInt(value.toString());
            if (val<1) throw new Exception("Not positive");
        } catch (Exception ex) {
            throw new ValidationException("The parameter "+_name+" is not a valid id value (integer>0) : "+value);
        }
    }

    private void validateBoolean(Object value) throws ValidationException {
        try {
            Boolean.parseBoolean(value.toString());
        } catch (Exception ex) {
            throw new ValidationException("The parameter "+_name+" is not a boolean value : "+value);
        }
    }

    private void validateExistingFileOrDirectory(Object value) throws ValidationException {
        File f = new File(value.toString());
        if (!f.exists())
            throw new ValidationException("The parameter "+_name+" is not an existing path.");
    }

    private void validateExistingFile(Object value)  throws ValidationException {
        File f = new File(value.toString());
        if (!f.exists() && !f.isFile())
            throw new ValidationException("The parameter "+_name+" is not an existing file.");
    }

    private void validateExistingDirectory(Object value)  throws ValidationException {
        File f = new File(value.toString());
        if (!f.exists() && !f.isDirectory())
            throw new ValidationException("The parameter "+_name+" is not an existing directory.");
    }
}
