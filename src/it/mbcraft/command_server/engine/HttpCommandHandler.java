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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.ParseException;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
class HttpCommandHandler implements HttpRequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpCoreContext coreContext = HttpCoreContext.adapt(context);
        HttpConnection conn = coreContext.getConnection(HttpConnection.class);

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        //only POST method is supported.
        if (!method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String target = request.getRequestLine().getUri();
        String path = URLDecoder.decode(target, "UTF-8");
        String parts[] = path.split("/");
        String commandRef = parts[parts.length - 1];
        String commandName = getCommandName(commandRef);
        String fullClassName = CommandServer.getInstance().getPackagePrefix();
        for (int i = 0; i < parts.length - 1; i++) {
            fullClassName += parts[i]+".";
        }
        fullClassName += commandName;
        fullClassName = fullClassName.replaceAll("\\.\\.", ".");
        ICommand command = null;
        Class commandClass = null;
        
        try {
            System.out.println("Looking for command class : "+fullClassName);
            commandClass = Class.forName(fullClassName);
        } catch (ClassNotFoundException ex) {
            //nothing to do, try a core command before throwing an error
        }
        
        if (commandClass==null) {
            try {
                fullClassName = CommandServer.INTERNAL_COMMAND_PACKAGE_PREFIX+commandName;
                fullClassName = fullClassName.replaceAll("\\.\\.", ".");
                commandClass = Class.forName(fullClassName);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(HttpCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                commandNotFound(response);
                return;
            }
        }
        
        try {
            
            Object cmd = commandClass.newInstance();
            command = (ICommand) cmd;
            if (request instanceof HttpEntityEnclosingRequest) {
                setupWithRequestParameters((HttpEntityEnclosingRequest) request, command);
            }
            command.execute();
            commandSuccessful(response, command,target);
        } catch (InstantiationException | IllegalAccessException ex) {
            commandInstantiationException(response, fullClassName);
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ValidationException ex) {
            invalidParameters(response, command, ex);
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            executionException(response, command, ex);
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Called when a parameter is not valid.
     * 
     * @param response
     * @param command
     * @param ex 
     */
    private void invalidParameters(HttpResponse response, ICommand command, ValidationException ex) {
        try {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            HttpEntity body = new StringEntity("Parameters error : " + ex.getMessage());
            response.setEntity(body);
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Called for an error during execution.
     * 
     * @param response
     * @param command
     * @param ex 
     */
    private void executionException(HttpResponse response, ICommand command, ExecutionException ex) {
        try {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            HttpEntity body = new StringEntity("Execution error : " + ex.getMessage());
            response.setEntity(body);
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Called when the command instance cannot be created.
     * 
     * @param response
     * @param cmdClass 
     */
    private void commandInstantiationException(HttpResponse response, String cmdClass) {
        try {
            response.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
            HttpEntity body = new StringEntity("Unable to create command instance of class : " + cmdClass);
            response.setEntity(body);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The command does not exist.
     * 
     * @param response 
     */
    private void commandNotFound(HttpResponse response) {
        try {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            HttpEntity body = new StringEntity("Command not found.");
            response.setEntity(body);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Ritorna il percorso corretto del file cercato, a seconda del prefisso
     * utilizzato.
     *
     * @param docRoot La root dei files
     * @param path Il percorso del file musicale, come stringa
     * @return L'oggetto file che punta al file musicale richiesto
     */
    private void commandSuccessful(HttpResponse response, ICommand command,String url) {
        try {
            response.setStatusCode(HttpStatus.SC_OK);
            HttpEntity body = new StringEntity(command.getExecutionLogAsString());
            response.setEntity(body);
            System.out.println("Command "+url+" run successfully.");
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the command name.
     * 
     * @param commandRef
     * @return 
     */
    private String getCommandName(String commandRef) {
        String cmdParts[] = commandRef.split("_");
        String finalCmd = "";
        for (String cmp : cmdParts) {
            finalCmd += cmp.substring(0, 1).toUpperCase() + cmp.substring(1).toLowerCase();
        }
        return finalCmd + "Command";
    }

    /**
     * Executes setup with the request parameters.
     * 
     * @param request
     * @param command 
     */
    private void setupWithRequestParameters(HttpEntityEnclosingRequest request, ICommand command) {
        try {
            String content = EntityUtils.toString(request.getEntity());
            String[] encodedParameters = content.split("&");
            for (String encodedParameter : encodedParameters) {
                String[] parameterParts = encodedParameter.split("=");
                String key = URLDecoder.decode(parameterParts[0],"UTF-8");
                String value = URLDecoder.decode(parameterParts[1],"UTF-8");
                command.setParameter(key,value);
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(HttpCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
