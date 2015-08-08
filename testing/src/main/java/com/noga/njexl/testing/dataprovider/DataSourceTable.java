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
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.datastructures.XList;

import java.util.*;

/**
 * A DataSourceTable type for Table like manipulation of data
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

    protected DataMatrix matrix ;

    /**
     * Creates a read/write data matrix from the underlying data
     * Note that changes won't be saved back to the data source
     * @param header if true, header info is passed to matrix to do column wise manipulation
     *               if false, header is taken as data
     * @return a data matrix
     */
    public DataMatrix matrix(boolean header){
        if ( matrix == null ){
            ListSet cols = null;
            int row = 0;
            if ( header ){
                String[] words = row(0);
                cols = new ListSet(Arrays.asList(words));
                row = 1;
            }
            ArrayList rows = new ArrayList();
            int length = length();
            while ( row < length ){
                String[] words = row(row);
                ArrayList<String> rowData = new ArrayList<>(Arrays.asList(words));
                rows.add(rowData);
                row++;
            }
            if ( header ){
                matrix =  new DataMatrix(rows,cols);
            }
            else {
                matrix = new DataMatrix(rows);
            }
        }
        return matrix;
    }
}