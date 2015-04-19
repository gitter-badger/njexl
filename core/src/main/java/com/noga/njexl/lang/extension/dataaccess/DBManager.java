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

    public static boolean addCon(String n, Map entry){
        DatabaseDOM dom = new DatabaseDOM(n, entry);
        if ( dataBaseDOMHash.containsKey(dom.name )){
            return false ;
        }
        dataBaseDOMHash.put(dom.name,dom);
        return true;
    }

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
