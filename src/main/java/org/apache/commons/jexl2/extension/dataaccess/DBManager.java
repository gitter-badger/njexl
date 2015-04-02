package org.apache.commons.jexl2.extension.dataaccess;

import org.apache.commons.jexl2.extension.TypeUtility;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by noga on 02/04/15.
 */
public final class DBManager {

    public static String DB_CONFIG_FILE_LOC = "db.json";

    public static class DatabaseDOM {

        public String name;

        public String dbName;

        public String driverClass;

        public String url;

        public String user;

        public String pass;

        public DatabaseDOM() {
            name = "";
            dbName = "";
            driverClass = "";
            url = "";
            user = "";
            pass = "";
        }

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

    public static ConcurrentHashMap<String,DatabaseDOM> dataBaseDOMHash ;

    public static ConcurrentHashMap<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, DatabaseDOM> getDatabaseDetails(String jsonFile) throws Exception {

        ConcurrentHashMap<String,DatabaseDOM> mapOfDoms = new ConcurrentHashMap<>();
        HashMap<String,HashMap> config = (HashMap)TypeUtility.json(jsonFile);
        for ( String key : config.keySet() ){
            DatabaseDOM dom = new DatabaseDOM( key, config.get(key));
            mapOfDoms.put(dom.name,dom);
        }
        return mapOfDoms;
    }

    // Selecting select in any query...
    public static Pattern SELECT_PATTERN = Pattern.compile("^\\s*select\\s+.*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static void init(String configFile) {
        try {
            dataBaseDOMHash = getDatabaseDetails(configFile);
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("I will have no DB Connectivity!");
        }
    }

    public static boolean addCon(String n, Map entry){
        DatabaseDOM dom = new DatabaseDOM(n, entry);
        if ( dataBaseDOMHash.containsKey(dom.name )){
            return false ;
        }
        dataBaseDOMHash.put(dom.name,dom);
        return true;
    }

    private static Connection getConnection(String dbName) throws Exception {

        Connection conn = null;

        DatabaseDOM d = dataBaseDOMHash.get(dbName);
        try {
            Class.forName( d.driverClass );
        } catch (ClassNotFoundException e) {
            throw e;
        }
        try {

            if (connectionMap.containsKey(dbName)) {
                conn = connectionMap.get(dbName);
                if (!conn.isClosed()) {
                    return conn;
                }
                connectionMap.remove(dbName);
            }
            conn = DriverManager.getConnection(d.url);
            connectionMap.put(dbName, conn);

        } catch (SQLException e) {
            if (conn != null) {
                conn.close();
            }
            throw e;
        }
        return conn;
    }

    public static Object exec(String dbName, String sql) throws Exception {

        Connection conn = getConnection(dbName);

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

    public static Object results(String dbName, String sql) throws Exception {
        Object retObject = exec(dbName, sql);
        int count = 0 ;
        ArrayList results = new ArrayList();
        if (retObject instanceof ResultSet) {
            ResultSet rs = (ResultSet)retObject;
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            while(rs.next()) {
                count++;
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
        return results;
    }

}
