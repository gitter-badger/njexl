package com.noga.njexl.testing.dataprovider.uri;

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by noga on 21/04/15.
 */
public class URIDataSource extends DataSource{

    public static class HTMLTable extends DataSourceTable{

        DataSource dataSource ;

        public final Element table;

        ArrayList<String[]> data;

        public final int pos;

        @Override
        public DataSource dataSource() {
            return dataSource;
        }

        @Override
        public String name() {
            return Integer.toString(pos);
        }

        @Override
        public int length() {
            return data.size();
        }

        @Override
        public String[] row(int rowIndex) {
            return data.get(rowIndex);
        }

        private void populateData(){

            for (Element row : table.select("tr")) {
                int colSize = 0;
                ArrayList<String> cols = new ArrayList<>();
                for (Element col : row.children()) {
                    colSize++;
                    cols.add( col.text());
                }
                String[] d = new String[colSize];
                d = cols.toArray(d);
                data.add(d);
            }
        }

        public HTMLTable(URIDataSource dataSource, Element table, int pos) {
            data = new ArrayList<>();
            this.dataSource = dataSource;
            this.table = table;
            this.pos = pos ;
            populateData();
        }
    }

    public static final Pattern LOADER_PATTERN = Pattern.compile("^http[s]?://.+", Pattern.CASE_INSENSITIVE);

    public static final DataMatrix.DataLoader DATA_LOADER = new URIDataSource();

    public URIDataSource(String location) throws Exception {
        super(location);
    }

    public URIDataSource(){}

    Document doc ;

    @Override
    protected HashMap<String, DataSourceTable> init(String location) throws Exception {

        HashMap<String,DataSourceTable> tables = new HashMap<>();
        URL url = new URL(location);
        doc = Jsoup.parse(url, 3000);
        int pos = 0 ;
        Elements elements =  doc.select("table");
        for ( Element elem : elements  ){
            HTMLTable table = new HTMLTable( this, elem,pos++);
            tables.put( table.name(), table);
        }
        return tables;
    }
}
