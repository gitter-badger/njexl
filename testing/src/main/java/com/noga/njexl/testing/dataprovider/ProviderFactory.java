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

package com.noga.njexl.testing.dataprovider;

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.TestSuite;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.dataprovider.excel.ExcelDataSource;
import com.noga.njexl.testing.dataprovider.uri.URIDataSource;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by noga on 15/04/15.
 */
public final class ProviderFactory {

    public static final HashMap<Pattern,String> dataSources = new HashMap<>();

    static{

        dataSources.put(ExcelDataSource.LOADER_PATTERN,ExcelDataSource.class.getName());
        DataMatrix.dataLoaders.put( ExcelDataSource.LOADER_PATTERN, ExcelDataSource.DATA_LOADER);

        dataSources.put(URIDataSource.LOADER_PATTERN, URIDataSource.class.getName());
        DataMatrix.dataLoaders.put( URIDataSource.LOADER_PATTERN, URIDataSource.DATA_LOADER);

    }

    // public so that anyone can clear the cache if need be
    public static final HashMap<String,DataSource> caches = new HashMap<>();

    public static DataSource  dataSource(String location){
        if ( caches.containsKey(location) ){
            return caches.get(location);
        }
        for ( Pattern key : dataSources.keySet() ){
            if ( key.matcher(location).matches() ){
                String className = dataSources.get(key);
                try {
                    Object ds = Utils.createInstance(className, location);
                    caches.put(location,(DataSource)ds);
                    return (DataSource) ds;
                }catch (Exception e){
                    System.err.println("Error creating :" + className );
                    System.err.println(e);
                }
            }
        }
        return null;
    }
}
