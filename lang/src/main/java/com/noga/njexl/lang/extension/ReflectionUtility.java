/*
* Copyright 2016 Nabarun Mondal
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

package com.noga.njexl.lang.extension;

import jodd.util.ClassLoaderUtil;

import java.io.File;
import java.net.URL;

/**
 * Some Jar loading Utility Functions
 *
 * Created by noga on 15/03/15.
 */
public class ReflectionUtility {

    /**
     * Loads a specific jar from a path
     * @param arg the path to the jar
     *            can be a file, a string path or an URL
     * @return true if success, false if failed
     */
    public static boolean load_jar(Object  arg){
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

    /**
     * Load multiple jars one by one
     * @param args list of jars
     * @return true if all successful, false if at least one failed loading
     */
    public static boolean load(Object... args){
        boolean ret = true ;
        for ( Object o :args ){
            ret = ret && load_jar(o);
        }
        return ret;
    }

    /**
     * Searches directories and loads jars : only one level
     * @param args the list of directories
     * @return true if all is well, false if one failed
     */
    public static boolean load_path(Object...args){
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

    /**
     * Gets a class loader
     * @param args strings :
     *             "ctx" : ContextClassLoader
     *             "sys" : SystemClassLoader
     *             anything else : DefaultClassLoader
     * @return the class loader
     */
    public static ClassLoader loader(Object...args){
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

    /**
     * Reloads a class
     * @param args the name of the class, using the class loader
     * @return true if successful, false if failed
     */
    public static boolean reload(Object... args){
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
