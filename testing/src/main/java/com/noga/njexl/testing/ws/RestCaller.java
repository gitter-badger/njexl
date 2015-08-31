/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.noga.njexl.testing.ws;


import com.noga.njexl.lang.extension.TypeUtility;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by noga on 12/05/15.
 */
public class RestCaller {

    public enum CallType{
        GET,
        POST
    }

    public static final String ENCODING = "UTF-8";

    protected URL base;

    //disabling switching... probably makes sense this way?
    public final CallType method;

    protected int connectionTimeout;

    protected int readTimeout;


    public RestCaller(String url, String method, int connectionTimeout, int readTimeOut) throws Exception{
        base = new URL(url) ;
        this.method = Enum.valueOf( CallType.class, method);
        this.connectionTimeout = connectionTimeout ;
        this.readTimeout = readTimeOut ;
    }

    public String createRequest(Map<String,String> args) throws Exception{
        if (args == null || args.isEmpty()){
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for ( String name : args.keySet()){
            buffer.append(name).append("=");
            String value = args.get(name);
            value = URLEncoder.encode( value, ENCODING) ;
            buffer.append(value).append("&");
        }
        String query = buffer.substring(0, buffer.length() - 1); // remove last
        return query ;
    }

    public String get(Map<String,String> args) throws Exception {
        String finalUrl = base.toString() + "?" + createRequest(args);
        String result = TypeUtility.readToEnd(finalUrl, connectionTimeout, readTimeout );
        return result;
    }

    /**
     * http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
     * @param args the arguments to pass
     * @return the response
     * @throws Exception in case of any issue
     */
    public String post(Map<String,String> args) throws Exception {
        URLConnection  connection =  base.openConnection();
        connection.setDoOutput(true); // make it post
        connection.setRequestProperty("Accept-Charset", ENCODING);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + ENCODING);
        connection.setConnectTimeout( connectionTimeout );
        connection.setReadTimeout( readTimeout );
        String query = createRequest(args);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write( query.getBytes(ENCODING) );
        InputStream inputStream = connection.getInputStream();
        return TypeUtility.readStream(inputStream);
    }

    public String call(Map<String,String> args) throws Exception {
        switch ( method ){
            case GET:
                return get(args);
            case POST:
                return post(args);
            default:
                return "Unsupported Protocol!" ;
        }
    }
}
