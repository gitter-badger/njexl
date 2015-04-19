package com.noga.njexl.testing.dataprovider;

import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.dataprovider.excel.ExcelDataSource;

import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */
public final class ProviderFactory {

    public static final HashMap<String,String> dataSources = new HashMap<>();

    static{
        dataSources.put(".xls", ExcelDataSource.class.getName());
        dataSources.put(".xlsx", ExcelDataSource.class.getName());
    }

    public static DataSource  dataSource(String location){
        String l = location.toLowerCase();
        for ( String key : dataSources.keySet() ){
            if ( l.endsWith(key) ){
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
