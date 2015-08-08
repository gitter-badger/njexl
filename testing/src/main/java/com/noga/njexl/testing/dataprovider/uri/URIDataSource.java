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

package com.noga.njexl.testing.dataprovider.uri;

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation of {DataSource} for arbitrary URI
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

    public static final Pattern LOADER_PATTERN_1 = Pattern.compile("^http[s]?://.+", Pattern.CASE_INSENSITIVE);

    public static final Pattern LOADER_PATTERN_2 = Pattern.compile(".*html>.*|.*<html.*",
            Pattern.MULTILINE|Pattern.DOTALL| Pattern.CASE_INSENSITIVE);

    public static final DataMatrix.DataLoader DATA_LOADER = new URIDataSource();

    public URIDataSource(String location) throws Exception {
        super(location);
    }

    public URIDataSource(){}

    Document doc ;

    @Override
    protected Map<String, DataSourceTable> init(String location) throws Exception {

        HashMap<String,DataSourceTable> tables = new HashMap<>();
        try {
            URL url = new URL(location);
            doc = Jsoup.parse(url, 30000);
        }catch (MalformedURLException e){
            // perhaps data as HTML String ?
            doc = Jsoup.parse( location );
        }

        int pos = 0 ;
        Elements elements =  doc.select("table");
        for ( Element elem : elements  ){
            HTMLTable table = new HTMLTable( this, elem,pos++);
            tables.put( table.name(), table);
        }
        return tables;
    }
}
