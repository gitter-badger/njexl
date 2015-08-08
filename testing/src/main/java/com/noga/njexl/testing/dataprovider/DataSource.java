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
import com.noga.njexl.lang.extension.dataaccess.DataMatrix.DataLoader;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * A Data Table type to ease out data reading
 */
public abstract class DataSource implements DataLoader {

    protected String loc;

    public String location(){ return  loc ; }

    public Map<String,DataSourceTable> tables;

    protected abstract Map<String,DataSourceTable> init(String location) throws Exception;

    @Override
    public DataMatrix matrix(String location, Object... args) throws Exception {

        DataSource dataSource = ProviderFactory.dataSource(location);

        if ( args.length ==  0 ){
            throw new Exception("Sorry, no sheet was specified!");
        }
        String sheet = args[0].toString();
        DataSourceTable dataSourceTable = dataSource.tables.get(sheet);
        if ( dataSourceTable == null ){
            throw new Exception("Sorry, no  such sheet as '" + sheet +"'!");
        }
        boolean header = true ;
        if ( args.length >  1 ){
            header = TypeUtility.castBoolean(args[1]);
        }
        ListSet cols = null;
        int row = 0;
        if ( header ){
            String[] words = dataSourceTable.row(0);
            cols = new ListSet(Arrays.asList(words));
            row = 1;
        }
        ArrayList rows = new ArrayList();

        while ( row < dataSourceTable.length() ){
            String[] words = dataSourceTable.row(row);
            ArrayList<String> rowData = new ArrayList<>(Arrays.asList(words));
            rows.add(rowData);
            row++;
        }
        if ( header ){
            return new DataMatrix(rows,cols);
        }
        return new DataMatrix(rows);
    }

    public DataSource(String location) throws Exception{
        this.loc = location;
        this.tables = init(location);
    }

    public DataSource(){
        this.loc = null;
        this.tables = null;
    }
}

