package com.noga.njexl.testing.dataprovider.excel;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix.DataLoader;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */
public class ExcelDataSource extends DataSource {

    private static final HashMap<String,ExcelDataSource> dataSources = new HashMap<>();

    public static class ExcelDataLoader implements DataLoader{

        public static final ExcelDataLoader excelDataLoader = new ExcelDataLoader();

        protected ExcelDataLoader(){
            DataMatrix.dataLoaders.put(".xls",this);
            DataMatrix.dataLoaders.put(".xlsx",this);
            DataMatrix.dataLoaders.put(".xlsm",this);
        }
        @Override
        public DataMatrix matrix(String location, Object... args) throws Exception {
            ExcelDataSource excelDataSource ;
            if ( !dataSources.containsKey( location )) {
                excelDataSource = new ExcelDataSource(location);
            }else{
                excelDataSource = dataSources.get(location);
            }

            if ( args.length ==  0 ){
                throw new Exception("Sorry, no sheet was specified!");
            }
            String sheet = args[0].toString();
            DataSourceTable dataSourceTable = excelDataSource.tables.get(sheet);
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
    }

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
        dataSources.put(location,this);
    }

}
