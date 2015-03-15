package org.apache.commons.jexl2.extension;

import jodd.util.ClassLoaderUtil;

import java.io.File;
import java.net.URL;

/**
 * Created by noga on 15/03/15.
 */
public class ReflectionUtility {

    public boolean load_jar(Object  arg){
        try {
            if ( arg instanceof String) {
                ClassLoaderUtil.addFileToClassPath(new File(arg.toString()), ClassLoaderUtil.getDefaultClassLoader());
                return true;
            }
            if ( arg instanceof File) {
                ClassLoaderUtil.addFileToClassPath((File)arg, ClassLoaderUtil.getDefaultClassLoader());
                return true;
            }
            if ( arg instanceof URL){
                ClassLoaderUtil.addUrlToClassPath((URL) arg, ClassLoaderUtil.getDefaultClassLoader());
                return true;
            }
            return false;
        }catch (Exception e){
          System.err.println(e);
        }
        return false;
    }

    public boolean load(Object... args){
        boolean ret = true ;
        for ( Object o :args ){
            ret = ret && load_jar(o);
        }
        return ret;
    }

    public boolean load_path(Object...args){
        if ( args.length == 0){
            return false;
        }
        boolean ret = true;
        for ( Object o : args){
            File f = new File(o.toString());
            if ( !f.isDirectory()) {
                continue;
            }
            File[] files = f.listFiles();
            for (File file : files ){
                String name = file.getName();
                if ( !name.endsWith(".jar")){
                    continue;
                }
                ret = ret && load_jar(file);
            }
        }
        return ret;
    }

    public ClassLoader loader(Object...args){
        if ( args.length > 0 ){
            if ( args[0].toString().equalsIgnoreCase("ctx")){
                return Thread.currentThread().getContextClassLoader();
            }
            if ( args[0].toString().equalsIgnoreCase("sys")){
                return ClassLoader.getSystemClassLoader();
            }
        }
        return ClassLoaderUtil.getDefaultClassLoader();
    }

    public boolean reload(Object... args){
        if ( args.length > 0 ){
            String className = args[0].toString();
            Object[] arr = TypeUtility.shiftArrayLeft(args,1);
            ClassLoader cl = loader(arr);
            try {
                cl.loadClass(className);
                return true;
            }catch (Exception e){
            }
        }
        return false;
    }
}
