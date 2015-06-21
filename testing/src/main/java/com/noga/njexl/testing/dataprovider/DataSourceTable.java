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

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.testing.dataprovider.collection.XStreamIterator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by noga on 15/04/15.
 */

public abstract class DataSourceTable {

    protected HashMap<String,Integer> columns;

    /**
     * Gets the columns
     * @return a Map : column name with index
     */
    public Map<String,Integer> columns(){
        if ( columns == null ){
            populateColumns();
        }
        return columns ;
    }

    /**
     * Gets the name of the data source
     * @return the name
     */
    public abstract String name();

    /**
     * The size of the data source
     * @return number of rows
     */
    public abstract int length();

    /**
     * Gets a row
     * @param rowIndex the index
     * @return a string array - corresponding to the row values
     */
    public abstract String[] row(int rowIndex);

    public abstract DataSource dataSource();

    private void populateColumns(){
        columns = new HashMap<>();
        String[] cols = row(0);
        for ( int i = 0 ; i < cols.length;i++ ){
            columns.put(cols[i].trim(), i);
        }
    }

    /**
     * Gets the columns value
     * @param column name of the column ( header )
     * @param rowIndex the row index
     * @return column value
     */
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

    /**
     * A Tuple of column name to value for a row
     * @param rowIndex the row index
     * @return a map header : column value mapping
     */
    public Map<String,String> tuple(int rowIndex){
        String[] columns = row(0);
        String[] values = row(rowIndex);
        try {
            return TypeUtility.makeDict(columns, values);
        }
        catch (Exception e){
            return Collections.EMPTY_MAP ;
        }
    }

    /**
     * A potential infinite iterator
     * @return iterator
     */
    public XStreamIterator iterator(){
        return new XStreamIterator(this);
    }

}