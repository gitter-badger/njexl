package com.noga.njexl.testing.dataprovider.dir;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Use a directory as data source,
 * Idea is to use any TSV file inside as table
 */
public class DirectoryDataSource extends DataSource {

    public static final Pattern LOADER_PATTERN = Pattern.compile("^[fF][iI][lL][eE]\\://.+",Pattern.CASE_INSENSITIVE);

    public static final DataMatrix.DataLoader DATA_LOADER = new DirectoryDataSource();

    public static class TextDataFile extends DataSourceTable{

        public static final String DELIMITER = "\t";

        DataSource dataSource ;

        String location;

        List<String[]> data ;

        @Override
        public DataSource dataSource() {
            return dataSource;
        }

        @Override
        public String name() {
            return location;
        }

        @Override
        public int length() {
            return data.size();
        }

        @Override
        public String[] row(int rowIndex) {
            if ( rowIndex >= data.size() || rowIndex < 0 ) {
                return null;
            }
            return data.get(rowIndex);
        }

        public TextDataFile(String file) throws Exception{
            data = new ArrayList<>();
            List<String> lines = TypeUtility.readLines(file);
            for ( int i = 0 ; i < lines.size();i++ ){
                String line =  lines.get(i);
                String[] row = line.split(DELIMITER);
                data.add(row);
            }
            location = new File(file).getName();
            if ( location.contains(".")) {
                location = location.substring(0, location.lastIndexOf("."));
            }
        }
    }

    @Override
    protected Map<String, DataSourceTable> init(String location) throws Exception {
        File file = new File(location);
        if ( !file.isDirectory() ) { return Collections.emptyMap(); }
        String[] files = file.list();
        HashMap<String,DataSourceTable> map = new HashMap<>();
        for ( int i = 0 ; i < files.length ; i++ ){
            DataSourceTable table = new TextDataFile(files[i]);
            map.put(table.name(), table);
        }
        return map;
    }

    public DirectoryDataSource() {}

    public DirectoryDataSource(String location) throws Exception {
        super(location);
    }
}
