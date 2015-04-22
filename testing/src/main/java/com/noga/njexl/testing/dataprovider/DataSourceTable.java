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

import com.noga.njexl.testing.dataprovider.collection.XStreamIterator;

import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */

public abstract class DataSourceTable {

    protected HashMap<String,Integer> columns;

    public HashMap<String,Integer> columns(){
        if ( columns == null ){
            populateColumns();
        }
        return columns ;
    }

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