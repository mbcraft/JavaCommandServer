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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * TODO : rimuovere il pattern singleton. Utilizzare un singleton potrebbe
 * creare problemi considerando che i metodi tengono traccia dell'ultima response,
 * oppure si potrebbe usare un ThreadLocal.
 * 
 * Questa classe contiene alcuni metodi per effettuare semplici chiamate
 * http GET e POST, ritornando il risultato o lanciando un'eccezione
 * se la risposta non è 200 OK.
 *
 * @author Marco Bagnaresi <marco.bagnaresi@gmail.com>
 */
public class EasyHttp {
    
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private CloseableHttpResponse lastResponse = null;
    
    private static EasyHttp _instance = null;
    
    private EasyHttp() {
    }
    
    /**
     * Ritorna l'unica istanza del client http.
     * 
     * @return Il client http.
     */ 
    public static EasyHttp getInstance() {
        if (_instance==null) _instance = new EasyHttp();
        return _instance;
    }

    /**
     * Controlla che il client http sia utilizzabile.
     */
    private void checkNotClosed() {
        if (httpClient==null)
            throw new IllegalStateException("Il client è stato chiuso.");
    }
    
    /**
     * Esegue una get all'indirizzo specificato.
     * 
     * @param address L'indirizzo a cui effettuare la get, contenente anche eventuali parametri
     * 
     * @return il codice della response
     * 
     * @throws it.mbcraft.commandserver.engine.http.HttpErrorException if the return status is not 200
     */
    public String doGet(String address) throws HttpErrorException {
        checkNotClosed();
        try {
            HttpGet get = new HttpGet(address);
            get.setHeader("Connection","close");

            lastResponse = httpClient.execute(get);
            int statusCode = lastResponse.getStatusLine().getStatusCode();
            String responseContent = getLastResponseAsString();
            if (statusCode == 200) {
                return responseContent;
            } else
                throw new HttpErrorException(statusCode,responseContent);
        } catch (IOException ex) {
            Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new IllegalStateException("Unable to execute get request.");
    }
    
    /**
     * Esegue una post all'indirizzo specificato
     * 
     * @param address L'indirizzo a cui effettuare la post
     * @param params I parametri della richiesta
     * @return Il codice della response
     * 
     * @throws it.mbcraft.commandserver.engine.http.HttpErrorException if the return status is not 200
     */
    public String doPost(String address,Properties params) throws HttpErrorException {
        checkNotClosed();
        try {
            HttpPost post = new HttpPost(address);
            
            post.setHeader("Connection","close");
            
            List<NameValuePair> postParams = new ArrayList<>();
            for (Object key : params.keySet()) {
                postParams.add(new BasicNameValuePair((String)key,(String)params.get(key)));
            }
            
            UrlEncodedFormEntity paramEntity = new UrlEncodedFormEntity(postParams);
            post.setEntity(paramEntity);
             
            lastResponse = httpClient.execute(post);
            int statusCode = lastResponse.getStatusLine().getStatusCode();
            String responseContent = getLastResponseAsString();
            if (statusCode == 200) {
                return responseContent;
            } else
                throw new HttpErrorException(statusCode,responseContent);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new IllegalStateException("Unable to execute post request.");
    }
    
    /**
     * Ritorna la response come stringa.
     * 
     * @return Il contenuto della response come stringa.
     */
    public String getLastResponseAsString() {
        try {
            String result = EntityUtils.toString(lastResponse.getEntity());
            EntityUtils.consume(lastResponse.getEntity());
            lastResponse.close();
            lastResponse = null;
            return result;
        } catch (IOException ex) {
            Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new IllegalStateException("Unable to get last response as String.");
    }
    
    /**
     * Salva la response su un file.
     * 
     * @param target Il file su cui salvare la response.
     */
    public void saveLastResponseToFile(File target) {
        try {
            byte[] responseBytes = EntityUtils.toByteArray(lastResponse.getEntity());
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(responseBytes);
                fos.flush();
            }
            EntityUtils.consume(lastResponse.getEntity());
            lastResponse.close();
            lastResponse = null;
        } catch (IOException ex) {
            Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Chiude l'ultima response, liberando le risorse.
     */
    public void dropLastResponse() {
        if (lastResponse!=null) {
            try {
                lastResponse.close();
                lastResponse = null;
            } catch (IOException ex) {
                Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Ritorna la validità dell'ultima richiesta eseguita.
     * 
     * @param lastResponseCode Il codice della richiesta
     * @return true se è valida, false altrimenti
     */
    public boolean isLastResponseOK(int lastResponseCode) {
        return lastResponseCode==200;
    }
    
    /**
     * Dealloca il client http.
     */
    public void dispose() {
        try {
            httpClient.close();
            httpClient = null;
        } catch (IOException ex) {
            Logger.getLogger(EasyHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
