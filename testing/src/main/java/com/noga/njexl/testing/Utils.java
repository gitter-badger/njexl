package com.noga.njexl.testing;


import com.noga.njexl.lang.extension.TypeUtility;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by noga on 15/04/15.
 */
public final class Utils {

    public static final String REDIRECT_TO_FILE = "@" ;

    public static final Pattern RELOCATE_PATH_PATTERN = Pattern.compile("\"_/(?<path>[^\"]*)\"", Pattern.MULTILINE);

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
