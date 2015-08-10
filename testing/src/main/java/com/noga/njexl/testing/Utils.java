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

package com.noga.njexl.testing;


import com.noga.njexl.lang.extension.TypeUtility;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to do a lot of utility stuff
 */
public final class Utils {

    public static class Mailer{

        public static final String SMTP_HOST_KEY = "SMTP_HOST";

        public static  String SMTP_HOST;

        public static final String SMTP_PORT_KEY = "SMTP_PORT";

        public static  int  SMTP_PORT;

        public static final String MAIL_FROM_KEY = "MAIL_FROM";

        public static  String MAIL_FROM;

        public static final String MAIL_DOMAIN_KEY = "SMTP_DOMAIN";

        public static  String MAIL_DOMAIN;

        public static final String MAIL_USER_KEY = "SMTP_USER";

        public static  String MAIL_USER;

        public static final String MAIL_PASS_KEY = "SMTP_PASS";

        public static  String MAIL_PASS;


        public static DefaultAuthenticator defaultAuthenticator;


        public static int getEnvDefault(String name, int defaultValue){
            String s = System.getenv(name);
            return TypeUtility.castInteger(s,defaultValue);
        }

        public static String getEnvDefault(String name, String defaultValue){
            String s = System.getenv(name);
            if ( s != null ) return s ;
            return defaultValue ;
        }


        static{
            try{

                SMTP_HOST = getEnvDefault(SMTP_HOST_KEY, "smtp.gmail.com");
                SMTP_PORT = getEnvDefault(SMTP_PORT_KEY , 587) ;
                MAIL_FROM = getEnvDefault(MAIL_FROM_KEY, "nobody@gmail.com");
                MAIL_DOMAIN = getEnvDefault(MAIL_DOMAIN_KEY , "gmail.com");
                MAIL_USER = getEnvDefault(MAIL_USER_KEY , "");
                MAIL_PASS = getEnvDefault(MAIL_PASS_KEY , "");
                defaultAuthenticator = new DefaultAuthenticator( MAIL_USER , MAIL_PASS );

            }catch (Exception e){

            }
        }
        static Email createEmail(String subject, String from) {

            try {
                Email email = new SimpleEmail();
                email.setHostName(SMTP_HOST);
                email.setSmtpPort(SMTP_PORT);
                email.setAuthenticator(defaultAuthenticator);
                if ( !from.contains("@")){
                    from += "@" + MAIL_DOMAIN ;
                }
                email.setFrom(from);
                email.setSubject(subject);
                return email;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        public static boolean send(String from, String subject, String body , String... to ) {

            Email email = createEmail(subject,from);
            if ( email == null ){
                return false;
            }
            try {

                for (int i = 0; i < to.length; i++) {
                    String mailTo = to[i].trim();
                    if ( mailTo.isEmpty() ) continue;
                    if ( !mailTo.contains("@")){
                        mailTo += "@" + MAIL_DOMAIN ;
                    }
                    email.addTo(mailTo);
                }
                email.setMsg(body);
                email.send();
                return true;

            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }
    }

    public static final String REDIRECT_TO_FILE = "@" ;

    public static final Pattern RELOCATE_PATH_PATTERN
            = Pattern.compile("\"_/(?<path>[^\"]*)\"", Pattern.MULTILINE);

    public static final Pattern VAR_SUBST =
            Pattern.compile("\\$\\{(?<name>[_a-zA-Z][_a-zA-Z\\.0-9]*)\\}",
                    Pattern.MULTILINE);

    public static Object createInstance(String className, Object... params) {
        try {
            Class clazz = Class.forName(className);
            if ( params.length == 0 ) {
                return clazz.newInstance();
            }
            //else get the matching constructor ...
            Constructor[] constructors = clazz.getDeclaredConstructors();
            for ( int i = 0 ; i < constructors.length; i++ ){
                if ( params.length == constructors[i].getParameterCount() ){
                    // may be this ?
                    return constructors[i].newInstance( params );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readToEnd(String fileName) throws Exception{
        List<String> lines = Files.readAllLines(new File(fileName).toPath());
        StringBuffer buffer = new StringBuffer();
        for(String l : lines ){
            buffer = buffer.append(l).append("\n");
        }
        return buffer.toString();
    }

    public static void writeFile(String fileName,String data) throws Exception{
        Files.write( new File(fileName).toPath(), data.getBytes()) ;
    }

    public static String getScript(String script)  {
        String actual = script ;
        if (script.startsWith(REDIRECT_TO_FILE)) {
            script = script.substring(1);
            try {
                actual = readToEnd(script);
            }catch (Exception e){
                // nothing...
            }
        }
        return actual ;
    }

    public static String relocatePathInXml(String loc, String xml) throws Exception{
        Matcher matcher = RELOCATE_PATH_PATTERN.matcher(xml);
        while(matcher.find()){
            File f = new File( loc + "/" + matcher.group("path") );
            String path = f.getCanonicalPath() ;
            path = path.replace('\\','/');
            xml = matcher.replaceFirst("\"" + path + "\"" );
            matcher = RELOCATE_PATH_PATTERN.matcher(xml);
        }
        return xml ;
    }

    public static String substituteVariableInXml(String xml, Map<String,String> variables)
            throws Exception{
        // there is no point?
        if ( variables.isEmpty() ) { return xml ; }
        // if not, perhaps...?
        Matcher matcher = VAR_SUBST.matcher(xml);
        while(matcher.find()){
            String varName = matcher.group("name");
            if ( !variables.containsKey( varName ) ){
                throw new Exception(String.format("[%s] is not there in the context", varName));
            }
            String value = variables.get(varName);
            xml = matcher.replaceFirst( value );
            matcher = VAR_SUBST.matcher(xml);
        }
        return xml ;
    }

    public static String ts(){
        return TypeUtility.castString(new Date(), "yyyyMMdd-hhmmss");
    }

    public static boolean copyFileFromUrl(String url,String dest) {
        try {
            URL website = new URL(url);
            Path target = new File(dest).toPath();
            long size = Files.copy(website.openStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return size >= 0 ;
        }catch (Exception e){
        }
        return false;
    }
}
