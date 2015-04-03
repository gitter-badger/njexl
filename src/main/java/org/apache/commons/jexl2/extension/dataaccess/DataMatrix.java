package org.apache.commons.jexl2.extension.dataaccess;

import org.apache.commons.jexl2.Interpreter.AnonymousParam;
import org.apache.commons.jexl2.extension.ListSet;
import org.apache.commons.jexl2.extension.TypeUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by noga on 03/04/15.
 */
public class DataMatrix {

    ListSet<String> columns;

    ArrayList<ArrayList<String>> rows;

    public DataMatrix(ArrayList<ArrayList<String>> rows,ListSet<String> cols){
        this.rows = rows;
        this.columns = cols;
    }

    public DataMatrix(ArrayList<ArrayList<String>> rows){
        this.rows = rows;
        this.columns = new ListSet<>();
        for ( Integer i = 0 ; i < rows.get(0).size(); i++ ){
            this.columns.add(i.toString());
        }
    }

    public HashMap rh(int r){
        if ( r >= rows.size() ){
            return null;
        }
        HashMap<String,String> map = new HashMap<>();
        for ( int i = 0 ; i < columns.size();i++ ){
            map.put(columns.get(i),rows.get(r).get(i));
        }
        return map;
    }

    public ArrayList select(Object...args) throws Exception {
        if ( args.length ==  0 ){
            return rows;
        }
        AnonymousParam anon = null;
        if ( args[0] instanceof AnonymousParam ){
            anon = (AnonymousParam)args[0];
            args = TypeUtility.shiftArrayLeft(args,1);
        }

        HashSet<Integer> colIndexes = new HashSet<>();

        if ( args.length == 0 ){
            //select all
            for ( int i = 0 ; i < columns.size();i++ ){
                colIndexes.add(i);
            }
        }else {
            // select specific
            for (int i = 0; i < args.length; i++) {
                int pos = -1;
                if (args[i] instanceof Integer) {
                    pos = (int) args[i];
                } else {
                    pos = columns.indexOf(args[i]);
                }
                if (pos < 0) {
                    throw new Exception("No such header : " + args[i]);
                }
                colIndexes.add(pos);
            }
        }

        // now do the stuff
        ArrayList rs = new ArrayList();
        for ( int i = 0 ; i < rows.size();i++  ){
            if ( anon != null ){
                //process this ...
                anon.setIterationContext(this,rh(i),i);
                Object ret = anon.execute();
                if ( !TypeUtility.castBoolean(ret,false)){
                    continue;
                }
            }
            ArrayList cs = new ArrayList();
            ArrayList dataRow = rows.get(i);
            for ( int j = 0 ;j < columns.size();j++ ) {
                if (colIndexes.contains(j)) {
                    cs.add(dataRow.get(j));
                }
            }
            rs.add(cs);
        }
        if ( anon != null ){
            anon.removeIterationContext();
        }
        return rs;
    }

    @Override
    public String toString(){
        return "< " + columns + " , " + rows + " >" ;
    }
}
