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

import com.noga.njexl.lang.internal.logging.Log;
import com.noga.njexl.lang.internal.logging.LogFactory;
import jodd.util.ClassLoaderUtil;

import java.io.File;
import java.net.URL;

/**
 * Some Jar loading Utility Functions
 *
 * Created by noga on 15/03/15.
 */
public class ReflectionUtility {

    private static final Log LOG = LogFactory.getLog( ReflectionUtility.class);

    /**
     * Searches directories and loads jars : only one level
     * @param args the list of directories
     * @return true if all is well, false if one failed
     */
    public static boolean load_path(Object...args){
        if ( args.length == 0){
            return false;
        }
        boolean ret = false;
        try {
            for (Object o : args) {
                File f = new File(String.valueOf(o));
                String name = f.getName();
                if (f.isFile()) {
                    if (name.endsWith(".jar")) {
                        ClassLoaderUtil.addFileToClassPath(f, ClassLoaderUtil.getSystemClassLoader());
                        LOG.info(String.format("Loading jar : %s", f.getPath()));
                        return true;
                    }
                    return false;
                }
                File[] files = f.listFiles();
                for (File file : files) {
                    name = file.getName();
                    if (file.isFile() && name.endsWith(".jar")) {
                        ClassLoaderUtil.addFileToClassPath(file, ClassLoaderUtil.getSystemClassLoader());
                        LOG.info(String.format("Loading jar : %s", file.getPath()));
                        // at least one jar was hit
                        ret = true;
                        continue;
                    }
                    if (file.isDirectory()) {
                        ret = (ret || load_path(file.getPath()));
                    }
                }
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        return ret;
    }
}
