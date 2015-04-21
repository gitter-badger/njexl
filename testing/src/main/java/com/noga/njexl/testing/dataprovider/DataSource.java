package com.noga.njexl.testing.dataprovider;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix.DataLoader;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */
public abstract class DataSource implements DataLoader {

    protected String loc;

    public String location(){ return  loc ; }

    public HashMap<String,DataSourceTable> tables;

    protected abstract HashMap<String,DataSourceTable> init(String location) throws Exception;

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

