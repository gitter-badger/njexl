/**
*Copyright 2015 Nabarun Mondal
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

package com.noga.njexl.lang.extension.dataaccess;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.datastructures.ListSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Manages Database related affairs
 * Created by noga on 02/04/15.
 */
public final class DBManager {

    public static final String DB_CONFIG_FILE_LOC = "db.json";

    /**
     * The data base dom, which stores database connection properties
     */
    public static class DatabaseDOM {

        /**
         * Name of the connection
         */
        public final String name;

        /**
         * Database name
         */
        public final String dbName;

        /**
         * The driver class to connect
         */
        public final String driverClass;

        /**
         * URL of the data base
         */
        public final String url;

        /**
         * User name of th user
         */
        public final String user;

        /**
         * Password of the user
         */
        public final String pass;

        /**
         * creates a dom, useless one
         */
        public DatabaseDOM() {
            name = "";
            dbName = "";
            driverClass = "";
            url = "";
            user = "";
            pass = "";
        }

        /**
         * Create from a map
         * @param n the name of the entry
         * @param entry the map
         */
        public DatabaseDOM(String n, Map<String,String> entry) {
            name = n;
            dbName = entry.get("dbName");
            driverClass = entry.get("driverClass");
            url = entry.get("url");
            user = entry.get("user");
            pass = entry.get("pass");
        }

        @Override
        public String toString(){
            return String.format("%s : [%s,%s] @%s : %s", name,dbName,driverClass,url,user);
        }
    }

    /**
     * The database connectiondoms with names
     */
    public static ConcurrentHashMap<String,DatabaseDOM> dataBaseDOMHash ;

    /**
     * Current connections - cached
     */
    public static final ConcurrentHashMap<String, Connection> connectionMap = new ConcurrentHashMap<>();

    /**
     * Creates the database dom
     * @param jsonFile the file from where the dom would be created
     * @return the db dom hash @{dataBaseDOMHash}
     * @throws Exception in case of any error
     */
    public static ConcurrentHashMap<String, DatabaseDOM> getDatabaseDetails(String jsonFile) throws Exception {

        ConcurrentHashMap<String,DatabaseDOM> mapOfDoms = new ConcurrentHashMap<>();
        HashMap<String,HashMap> config = (HashMap) TypeUtility.json(jsonFile);
        for ( String key : config.keySet() ){
            DatabaseDOM dom = new DatabaseDOM( key, config.get(key));
            mapOfDoms.put(dom.name,dom);
        }
        return mapOfDoms;
    }

    // Selecting select in any query...
    public static Pattern SELECT_PATTERN = Pattern.compile("^\\s*select\\s+.*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * Initialize the db connectivity
     * @param configFile the json file
     * @return true if done, false if failed
     */
    public static boolean init(String configFile) {
        try {
            dataBaseDOMHash = getDatabaseDetails(configFile);
            System.out.println("Successfully Loaded DB Config");
            System.out.println(dataBaseDOMHash);
            return true;
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("I will have no DB Connectivity!");
        }
        return false;
    }

    /**
     * On the fly add new connection
     * @param n name of the connection
     * @param entry map entry
     * @return true if successful, false if already exist
     */
    public static boolean addCon(String n, Map entry){
        DatabaseDOM dom = new DatabaseDOM(n, entry);
        if ( dataBaseDOMHash.containsKey(dom.name )){
            return false ;
        }
        dataBaseDOMHash.put(dom.name,dom);
        return true;
    }

    /**
     * Gets connection using id
     * @param dbConnectionId the id
     * @return the connection
     * @throws Exception in case of error
     */
    private static Connection getConnection(String dbConnectionId) throws Exception {

        Connection conn = null;

        DatabaseDOM d = dataBaseDOMHash.get(dbConnectionId);
        try {
            Class.forName( d.driverClass );
        } catch (ClassNotFoundException e) {
            throw e;
        }
        try {

            if (connectionMap.containsKey(dbConnectionId)) {
                conn = connectionMap.get(dbConnectionId);
                if (!conn.isClosed()) {
                    return conn;
                }
                connectionMap.remove(dbConnectionId);
            }
            conn = DriverManager.getConnection(d.url);
            connectionMap.put(dbConnectionId, conn);

        } catch (SQLException e) {
            if (conn != null) {
                conn.close();
            }
            throw e;
        }
        return conn;
    }

    /**
     * Executes sql
     * @param dbConnectionId the connection
     * @param sql the text sql
     * @return either a result set or an int
     * @throws Exception in case of error
     */
    public static Object exec(String dbConnectionId, String sql) throws Exception {

        Connection conn = getConnection(dbConnectionId);

        sql = sql.trim();

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        try {
            if (SELECT_PATTERN.matcher(sql).matches()) {
                return stmt.executeQuery(sql);
            } else {
                return stmt.executeUpdate(sql);
            }
        }catch (Throwable e){
            System.err.println(e.getMessage());
            throw e;
        }
    }

    /**
     * Slightly better return values
     * @param dbConnectionId the id
     * @param sql the sql text
     * @return Returns @{DataMatrix} or int
     * @throws Exception in case of error
     */
    public static Object results(String dbConnectionId, String sql) throws Exception {
        Object retObject = exec(dbConnectionId, sql);

        ArrayList results = new ArrayList();
        ArrayList columns = new ArrayList();

        if (retObject instanceof ResultSet) {
            ResultSet rs = (ResultSet)retObject;
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            for ( int i =1; i <= cols; i++ ) {
                String value =  rsmd.getColumnName(i);
                columns.add(value);
            }
            while(rs.next()) {
                ArrayList row = new ArrayList();
                for ( int i =1; i <= cols; i++ ) {
                    String value =  rs.getString(i);
                    row.add(value);
                }
                results.add(row);
            }
        } else {
            return retObject;
        }
        return new DataMatrix(results, new ListSet<>(columns));
    }

}
