package com.noga.njexl.testing.dataprovider;

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
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

    public static DataSource  dataSource(String location){
        for ( Pattern key : dataSources.keySet() ){
            if ( key.matcher(location).matches() ){
                String className = dataSources.get(key);
                try {
                    Object ds = Utils.createInstance(className, location);
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
