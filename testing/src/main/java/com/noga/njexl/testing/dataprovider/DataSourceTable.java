package com.noga.njexl.testing.dataprovider;

import com.noga.njexl.testing.dataprovider.collection.XStreamIterator;

import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */

public abstract class DataSourceTable {

    protected HashMap<String,Integer> columns;

    public abstract String name();

    public abstract int length();

    public abstract String[] row(int rowIndex);

    public abstract DataSource dataSource();

    private void populateColumns(){
        columns = new HashMap<>();
        String[] cols = row(0);
        for ( int i = 0 ; i < cols.length;i++ ){
            columns.put(cols[i].trim(), i);
        }
    }

    public String columnValue(String column, int rowIndex){
        if ( columns == null ){
            populateColumns();

        }
        if ( !columns.containsKey(column) ) {
            return null;
        }
        String[] data = row( rowIndex ) ;
        return data[columns.get(column)] ;
    }

    public XStreamIterator iterator(){
        return new XStreamIterator(this);
    }

}