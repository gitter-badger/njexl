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

package com.noga.njexl.testing.dataprovider.excel;

import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix.DataLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by noga on 15/04/15.
 */
public class ExcelDataSource extends DataSource {

    public static final Pattern LOADER_PATTERN = Pattern.compile(".+\\.xls[x]?[m]?$",Pattern.CASE_INSENSITIVE);

    public static final DataLoader DATA_LOADER = new ExcelDataSource();

    public static class ExcelDataTable extends DataSourceTable {

        DataSource dataSource;

        String name;

        ArrayList<String[]> data;

        @Override
        public String name() {
            return name;
        }

        @Override
        public int length() {
            return data.size();
        }

        @Override
        public String[] row(int rowIndex) {
            if ( rowIndex < data.size() ) {
                return data.get(rowIndex);
            }
            return null;
        }

        @Override
        public DataSource dataSource() {
            return dataSource;
        }

        private void init(ExcelReader reader){
            data  = new ArrayList<>();
            int colSize = reader.columnCount(name);
            int rowSize = reader.rowCount(name);
            for ( int row = 0 ; row < rowSize; row++ ){
                String[] words = new String[colSize];
                for ( int col = 0 ; col < colSize; col++ ){
                    words[col] = reader.value(name, row, col);
                }
                data.add(words);
            }
        }
        public ExcelDataTable(ExcelReader reader, ExcelDataSource ds, String sheetName){
            name = sheetName ;
            dataSource = ds ;
            init(reader);
        }

    }

    @Override
    protected Map<String, DataSourceTable> init(String location) throws Exception {
        HashMap<String, DataSourceTable> tables = new HashMap<>();
        String l = location.toLowerCase();
        ExcelReader reader ;
        if ( l.endsWith(".xls")){
            reader = new XlsReader(location);
        }
        else{
            reader = new XlsXReader(location);
        }
        String[] sheetNames = reader.sheets();
        for ( int i = 0 ; i < sheetNames.length;i++ ){
            DataSourceTable table = new ExcelDataTable(reader, this, sheetNames[i]);
            tables.put(table.name(), table);
        }
        return tables;
    }

    public ExcelDataSource(String location) throws Exception {
        super(location);
    }
    public ExcelDataSource(){}
}
