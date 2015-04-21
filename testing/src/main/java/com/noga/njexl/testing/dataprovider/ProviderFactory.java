package com.noga.njexl.testing.dataprovider;

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
        dataSources.put(Pattern.compile(".+\\.xls[x]?$", Pattern.CASE_INSENSITIVE),
                ExcelDataSource.class.getName());

        dataSources.put(Pattern.compile("^http[s]?://.+", Pattern.CASE_INSENSITIVE),
                URIDataSource.class.getName());
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
