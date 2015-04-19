package com.noga.njexl.testing.dataprovider.excel;

import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */
public class ExcelDataSource extends DataSource {

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
    protected HashMap<String, DataSourceTable> init(String location) throws Exception {
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

}
