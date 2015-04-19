package com.noga.njexl.testing.dataprovider;

import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */
/**
 * Created by noga on 06/02/15.
 */
public abstract class DataSource {

    protected String loc;

    public String location(){ return  loc ; }

    public HashMap<String,DataSourceTable> tables;

    protected abstract HashMap<String,DataSourceTable> init(String location) throws Exception;

    public DataSource(String location) throws Exception{
        this.loc = location;
        this.tables = init(location);
    }

}

