package org.apache.commons.jexl2.extension.dataaccess;

import org.apache.commons.jexl2.Interpreter.AnonymousParam;
import org.apache.commons.jexl2.extension.ListSet;
import org.apache.commons.jexl2.extension.SetOperations;
import org.apache.commons.jexl2.extension.TypeUtility;
import org.apache.commons.jexl2.extension.iterators.RangeIterator;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.ObjDoubleConsumer;

/**
 * Created by noga on 03/04/15.
 */
public class DataMatrix {

    public ListSet<String> columns;

    public ArrayList<ArrayList<String>> rows;

    public HashMap<String,ArrayList<Integer>> keys;

    public static DataMatrix loadExcel(String file, Object...args){
        System.err.println("Excel Not Implemented yet, Noga, the, is thinking");
        return null;
    }

    public static DataMatrix loadText(String file, Object...args) throws Exception{
        String sep="\t";
        boolean header = true ;
        if ( args.length > 0 ){
            sep = args[0].toString();
            if ( args.length > 1 ){
                header = TypeUtility.castBoolean(args[1],false);
            }
        }
        List<String> lines = Files.readAllLines(new File(file).toPath());
        ListSet cols = null;
        if ( header ){
            String[] words =  lines.get(0).split(sep);
            cols = new ListSet(Arrays.asList(words));
            lines.remove(0);
        }
        ArrayList rows = new ArrayList();
        for ( String line : lines ){
            String[] words =  line.split(sep);
            ArrayList row = new ArrayList(Arrays.asList(words));
            rows.add(row);
        }
        if ( header ){
            return new DataMatrix(rows,cols);
        }
        return new DataMatrix(rows);
    }

    public static DataMatrix file2matrix(String file,Object...args) throws Exception{
        String name = file.toLowerCase();
        if ( name.endsWith(".txt")||name.endsWith(".csv")||name.endsWith(".tsv")){
            return loadText(file,args);
        }
        if ( name.endsWith(".xls")||name.endsWith(".xlsx")){
            return loadExcel(file, args);
        }
        return null;
    }

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
        HashMap<Object,String> map = new HashMap<>();
        for ( int i = 0 ; i < columns.size();i++ ){
            String value = rows.get(r).get(i);
            // accessible by column name
            map.put(columns.get(i),value);
            //accessible by indices
            map.put(i, value);
        }
        return map;
    }

    public ArrayList<String> c(int c){
        return c(c,null);
    }

    public ArrayList<String> c(int c, ArrayList<Integer> agg ){
        ArrayList l = new ArrayList();
        for ( int r = 0; r < rows.size() ; r++ ){
            if ( agg == null ||
                    agg!= null && agg.contains( r ) ){
                String value = rows.get(r).get(c);
                l.add(value);
            }
        }
        return l;
    }

    private AnonymousParam setup(HashSet<Integer> colIndexes, Object[]args) throws Exception{
        AnonymousParam anon = null;

        if ( args.length == 0 ){
            //select all
            for ( int i = 0 ; i < columns.size();i++ ){
                colIndexes.add(i);
            }
        }else {
            if ( args[0] instanceof AnonymousParam ){
                anon = (AnonymousParam)args[0];
                args = TypeUtility.shiftArrayLeft(args,1);
            }
            // select specific
            for (int i = 0; i < args.length; i++) {
                int pos = -1;
                if (args[i] instanceof Integer) {
                    pos = (int) args[i];
                } else if ( args[i] instanceof RangeIterator){
                    Iterator<Long> itr = (RangeIterator)args[i];
                    while(itr.hasNext()){
                        colIndexes.add(itr.next().intValue());
                    }
                    continue;
                }
                else {
                    pos = columns.indexOf(args[i]);
                }
                if (pos < 0) {
                    throw new Exception("No such header : " + args[i]);
                }
                colIndexes.add(pos);
            }
        }
        return anon;
    }

    public ArrayList select(Object...args) throws Exception {
        if ( args.length ==  0 ){
            return rows;
        }
        HashSet<Integer> colIndexes = new HashSet<>();
        AnonymousParam anon = setup(colIndexes,args);

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

    public DataMatrix keys(Object...args) throws Exception{
        keys = new HashMap<>();
        HashSet<Integer> colIndexes = new HashSet<>();
        AnonymousParam anon = setup(colIndexes,args);

        // now do the stuff
        for ( int i = 0 ; i < rows.size();i++  ){
            String key = "";
            if ( anon != null ){
                //process this ...
                anon.setIterationContext(this,rh(i),i);
                Object ret = anon.execute();
                key = ret.toString();
            }
            else{
                String sep = "Ø";
                for ( int j = 0 ; j < columns.size(); j++ ){
                    if ( colIndexes.contains(j) ){
                        key += rows.get(i).get(j) + sep ;
                    }
                }
            }
            if ( !keys.containsKey(key)){
                keys.put(key, new ArrayList<>());
            }
            keys.get(key).add(i);
        }
        if ( anon != null ){
            anon.removeIterationContext();
        }
        return this;
    }

    public DataMatrix aggregate(Object...args) throws Exception {
        if ( keys == null ){
            keys();
        }
        HashSet<Integer> colIndexes = new HashSet<>();
        AnonymousParam anon = setup(colIndexes,args);

        ListSet  aColumns = new ListSet();

        for ( int c = 0 ; c < columns.size() ; c++  ) {
            if (colIndexes.contains(c)) {
                // add this column
                aColumns.add(columns.get(c));
            }
        }

        HashMap<String,ArrayList<Integer>> aKey = new HashMap<>();
        ArrayList aRows = new ArrayList();
        // aggregate rows
        int rowNum = 0 ;
        for ( String key : keys.keySet() ){
            ArrayList rowData = new ArrayList();
            ArrayList<Integer> agg = keys.get(key);
            for ( int c = 0 ; c < columns.size() ; c++  ){
                if ( colIndexes.contains(c)){
                    ArrayList<String> data = c(c,agg);
                    String value ;
                    if ( anon != null ){
                        anon.setIterationContext(this,data,c);
                        Object ret = anon.execute();
                        value = String.format("%s", ret);
                    }else{
                        Double[] a = TypeUtility.sqlmath(data);
                        value = String.format("%s", a[2]);
                    }
                    //create a row with aggregated rows for the column
                    rowData.add(value);
                }
            }
            ArrayList r = new ArrayList();
            r.add(rowNum);
            aRows.add(rowData);
            aKey.put(key, r);
            rowNum++;
        }
        if ( anon != null ){
            anon.removeIterationContext();
        }
        DataMatrix dm = new DataMatrix(aRows,aColumns);
        dm.keys = aKey ;
        return dm;
    }

    public static Set[] key_diff( DataMatrix d1, DataMatrix d2 ) throws Exception{
        if ( d1.keys == null ){
            d1.keys();
        }
        if ( d2.keys == null ){
            d2.keys();
        }
        Set[] retVal = new Set[2];
        retVal[0] = SetOperations.set_d( d1.keys.keySet(), d2.keys.keySet() );
        retVal[1] = SetOperations.set_d( d2.keys.keySet(), d1.keys.keySet() );
        return retVal ;
    }

    public static Object[] diff (Object... args) throws Exception {
        AnonymousParam anon = null;
        if ( args.length == 0 ){
            return null;
        }
        if ( args.length > 0 ){
            if ( args[0] instanceof AnonymousParam ){
                anon = (AnonymousParam)args[0];
                args = TypeUtility.shiftArrayLeft(args,1);
            }
        }
        DataMatrix left = (DataMatrix)args[0];
        DataMatrix right = (DataMatrix)args[1];
        if ( left.keys == null ){
            left.keys();
        }
        if ( right.keys == null ){
            right.keys();
        }
        Object[] result = new Object[3];
        // Keys which are not in left but in right
        Set[] diffKey = key_diff(left,right);
        ArrayList d1 = new ArrayList();
        for (  Object i : diffKey[0] ){
            d1.add(  left.keys.get(i) );
        }
        ArrayList d2 = new ArrayList();
        for (  Object i : diffKey[1] ){
            d1.add(  right.keys.get(i) );
        }
        result[0] = d1;
        result[1] = d2 ;
        ArrayList diff = new ArrayList();
        //now the rest
        Set intersection = SetOperations.set_i(  left.keys.keySet(), right.keys.keySet() );
        for ( Object i : intersection ){
            ArrayList<Integer> l = left.keys.get(i);
            ArrayList<Integer> r = right.keys.get(i);
            if ( l.size() != r.size() && l.size() != 1 ){
                throw new Exception("After Keying, multiple rows with same key! did you forget aggregate?");
            }
            int lIndex = l.get(0) ;
            int rIndex = r.get(0) ;
            HashMap L =  left.rh( lIndex );
            HashMap R =  right.rh(rIndex);

            if ( anon != null ){
                Object context = new Object[]{ left, right };
                Object cur = new Object[]{ L, R };
                Object index = new Object[] { lIndex , rIndex };
                anon.setIterationContext(context,cur,index);
                Object ret = anon.execute();
                if ( !TypeUtility.castBoolean(ret,false)){
                    // log it
                    diff.add( new Object[] { left.rows.get(lIndex) , right.rows.get(rIndex) } );
                }
            }else{
                Set colIntersect = SetOperations.set_i( L.keySet(), R.keySet() );
                for ( Object c : colIntersect ){
                    Object valLeft = L.get(c);
                    Object valRight = R.get(c);
                    if ( !valLeft.equals(valRight )){
                        diff.add( new Object[] { left.rows.get(lIndex) , right.rows.get(rIndex) } );
                    }
                }
            }
        }
        result[2] = diff ;

        if ( anon != null ){
            anon.removeIterationContext();
        }

        return result;
    }


    @Override
    public String toString(){
        return "< " + columns + " , " + rows + " >" ;
    }
}
