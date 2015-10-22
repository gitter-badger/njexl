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

import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.Script;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.datastructures.Tuple;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.datastructures.XList;
import com.noga.njexl.lang.extension.iterators.RangeIterator;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A generic Data Matrix class to manipulate on data
 * Created by noga on 03/04/15.
 */
public class DataMatrix {

    /**
     * A Generic diff structure for any sort of matrices
     */
    public static class MatrixDiff{

        /**
         * Is this diff qualifies as a diff or not
         * @return true if it is, false if it is not
         */
        public boolean diff(){

            if ( lr != null && !lr.isEmpty() ){
                return true;
            }
            if ( rl!= null && !rl.isEmpty() ){
                return true;
            }
            if ( id!= null && !id.isEmpty() ){
                return true;
            }
            return false ;
        }

        /**
         * where the  key list : left - right did not match
         */
        public List lr ;
        /**
         * The the key list : right - left did not match
         */
        public List rl ;
        /**
         * The key intersection portion if any,
         * but where the equality broke down
         */
        public List id ;

        @Override
        public String toString(){
            return String.format("%s : < %s %s %s>", diff(), lr, rl, id );
        }
    }

    /**
     * A standard interface to load data matrices from data sources
     */
    public interface DataLoader{

        /**
         * loads a data matrix
         * @param location the location from where it should load it
         * @param args corresponding args
         * @return a data matrix
         * @throws Exception if fails
         */
        DataMatrix matrix(String location,Object...args) throws Exception;

    }

    /**
     * A standard implementation of data loader,
     * used to load text like files
     */
    public static class TextDataLoader implements DataLoader{

        @Override
        public DataMatrix matrix(String location, Object... args) throws Exception {
            String sep="\t";
            boolean header = true ;
            if ( args.length > 0 ){
                sep = args[0].toString();
                if ( args.length > 1 ){
                    header = TypeUtility.castBoolean(args[1], false);
                }
            }
            List<String> lines = Files.readAllLines(new File(location).toPath());
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
    }

    /**
     * Various registered data loaders
     */
    public static final HashMap<Pattern,DataLoader> dataLoaders = new HashMap<>();

    static {

        final TextDataLoader textDataLoader = new TextDataLoader() ;
        dataLoaders.put(Pattern.compile(".+\\.tsv$",Pattern.CASE_INSENSITIVE), textDataLoader);
        dataLoaders.put(Pattern.compile(".+\\.csv$",Pattern.CASE_INSENSITIVE), textDataLoader);
        dataLoaders.put(Pattern.compile(".+\\.txt$",Pattern.CASE_INSENSITIVE), textDataLoader);
    }

    /**
     * The columns of the data matrix
     */
    public ListSet<String> columns;

    /**
     * The actual data rows, they are not including the column
     */
    public List<List<String>> rows;

    /**
     * For comparison, one needs to generate the row key.
     * If confused, see the @link{http://en.wikipedia.org/wiki/Candidate_key}
     * They generated and gets stored here
     */
    public Map<String,List<Integer>> keys;

    /**
     * The Factory of data matrix
     * @param location from where to be loaded
     * @param args the corresponding args
     * @return a data matrix
     * @throws Exception in case fails
     */
    public static DataMatrix loc2matrix(String location, Object... args) throws Exception{
        StringBuffer buffer = new StringBuffer();
        for ( Pattern p : dataLoaders.keySet() ){
            if ( p.matcher(location).matches()){
                DataLoader dataLoader =dataLoaders.get(p);
                return dataLoader.matrix(location,args);
            }
            buffer.append(p).append(";");
        }
        System.err.printf("No pattern matched for [%s] for Data Load!\n Registered Patterns are : %s\n",
                location, buffer);
        return null;
    }

    /**
     * Creates a data matrix
     * @param rows the rows of data
     * @param cols the column headers
     */
    public DataMatrix(List<List<String>> rows,ListSet<String> cols){
        this.rows = rows;
        this.columns = cols;
    }

    /**
     * This would be created column header free
     * @param rows only rows of data
     */
    public DataMatrix(List<List<String>> rows){
        this.rows = rows;
        this.columns = new ListSet<>();
        for ( Integer i = 0 ; i < rows.get(0).size(); i++ ){
            this.columns.add(i.toString());
        }
    }

    /**
     * A row, as a tuple structure.
     * See @link{http://en.wikipedia.org/wiki/Tuple}
     * @param r the row index
     * @return the ruple corrseponding to the row
     */
    public Tuple tuple(int r){
        if ( r >= rows.size() ){
            return null;
        }
        Tuple t = new Tuple(columns, rows.get(r));
        return t;
    }

    /**
     * A column
     * @param c the column index
     * @return the whole column row by row
     */
    public List<String> c(int c){
        return c(c,null);
    }

    /**
     * Selects only specific rows,
     * @param c the column index
     * @param agg these rows will be selected
     * @return list of selected row values for the column
     */
    public List<String> c(int c, List<Integer> agg ){
        XList l = new XList();
        for ( int r = 0; r < rows.size() ; r++ ){
            if ( agg == null ||
                    agg!= null && agg.contains( r ) ){
                String value = rows.get(r).get(c);
                l.add(value);
            }
        }
        return l;
    }

    private static class SelectSetup{
        private Interpreter.AnonymousParam anon;
        private HashSet<Integer> colIndexes;
    }

    private SelectSetup setup(Object... args) throws Exception{

        SelectSetup selectSetup = new SelectSetup();
        selectSetup.colIndexes = new HashSet<>();

        if ( args.length > 0 ){
            if ( args[0] instanceof Interpreter.AnonymousParam){
                selectSetup.anon = (Interpreter.AnonymousParam)args[0];
                args = TypeUtility.shiftArrayLeft(args,1);
            }
        }

        if ( args.length == 0 ){
            //select all
            for ( int i = 0 ; i < columns.size();i++ ){
                selectSetup.colIndexes.add(i);
            }
        }else {
            // select specific
            for (int i = 0; i < args.length; i++) {
                int pos = -1;
                if (args[i] instanceof Integer) {
                    pos = (int) args[i];
                } else if ( args[i] instanceof RangeIterator){
                    Iterator<Long> itr = (RangeIterator)args[i];
                    while(itr.hasNext()){
                        selectSetup.colIndexes.add(itr.next().intValue());
                    }
                    continue;
                }
                else {
                    pos = columns.indexOf(args[i]);
                }
                if (pos < 0) {
                    throw new Exception("No such header : " + args[i]);
                }
                selectSetup.colIndexes.add(pos);
            }
        }
        return selectSetup;
    }

    /**
     * The revered select function
     * @param args parameters
     * @return selected rows
     * @throws Exception in error
     */
    public List select(Object...args) throws Exception {
        if ( args.length ==  0 ){
            return rows;
        }
        SelectSetup setup = setup(args);
        // now do the stuff
        List rs = _select_op_(setup.anon, setup.colIndexes);
        return rs;
    }

    /**
     * The sub-matrix function
     * @param args parameters
     * @return a data matrix
     * @throws Exception in error
     */
    public DataMatrix sub(Object...args) throws Exception {

        if ( args.length ==  0 ){
            return this; // risky? May be. I don't know
        }

        SelectSetup setup = setup(args);

        ListSet nColumns = new ListSet();

        for ( int j = 0 ;j < columns.size();j++ ) {
            if (setup.colIndexes.contains(j)) {
                nColumns.add(columns.get(j));
            }
        }
        List rs = _select_op_(setup.anon,setup.colIndexes);

        return new DataMatrix(rs,nColumns);
    }

    private List  _select_op_(Interpreter.AnonymousParam anon, Set<Integer> colIndexes ) throws Exception{
        // now do the stuff
        XList rs = new XList();
        HashMap<Integer,Tuple> selectedRows = new HashMap<>();
        for ( int i = 0 ; i < rows.size();i++ ){
            if ( anon != null ){
                //process this ...
                anon.setIterationContextWithPartial(this, tuple(i),i,rs);
                Object ret = anon.execute();
                if ( !TypeUtility.castBoolean(ret,false)){
                    continue;
                }
                // get back the values if over written ?
                selectedRows.put(i,(Tuple)anon.getVar(Script._ITEM_));
            }
            ArrayList cs = new ArrayList();
            List<String> dataRow = rows.get(i);
            for ( int j = 0 ;j < columns.size();j++ ) {
                if (colIndexes.contains(j)) {
                    Object val = dataRow.get(j) ;
                    if ( anon == null ){
                        cs.add(val);
                    }else {
                        Object var = selectedRows.get(i).get(j);
                        // avoid stupidity, add Tuple value always
                        cs.add(var);
                    }
                }
            }
            rs.add(cs);
        }
        if ( anon != null ){
            anon.removeIterationContext();
        }
        return rs;
    }

    /**
     * This is how you set key to a data matrix
     * @param args parameters
     * @return a keyed matrix
     * @throws Exception in error
     */
    public DataMatrix keys(Object...args) throws Exception{
        keys = new HashMap<>();
        SelectSetup setup = setup(args);

        // now do the stuff
        for ( int i = 0 ; i < rows.size();i++  ){
            String key = "";
            if ( setup.anon != null ){
                //process this ...
                setup.anon.setIterationContext(this, tuple(i),i);
                Object ret = setup.anon.execute();
                key = ret.toString();
            }
            else{
                String sep = "Ø";
                for ( int j = 0 ; j < columns.size(); j++ ){
                    if ( setup.colIndexes.contains(j) ){
                        key += rows.get(i).get(j) + sep ;
                    }
                }
            }
            if ( !keys.containsKey(key)){
                keys.put(key, new ArrayList<>());
            }
            keys.get(key).add(i);
        }
        if ( setup.anon != null ){
            setup.anon.removeIterationContext();
        }
        return this;
    }

    /**
     * This is how you aggregate rows, to merge them into effective single row
     * @param args parameters
     * @return an aggregated matrix based on keys
     * @throws Exception in error
     */
    public DataMatrix aggregate(Object...args) throws Exception {
        if ( keys == null ){
            keys();
        }
        SelectSetup setup = setup(args);
        HashSet<Integer> colIndexes = setup.colIndexes;
        Interpreter.AnonymousParam anon = setup.anon;

        ListSet  aColumns = new ListSet();

        for ( int c = 0 ; c < columns.size() ; c++  ) {
            if (colIndexes.contains(c)) {
                // add this column
                aColumns.add(columns.get(c));
            }
        }

        HashMap<String,List<Integer>> aKey = new HashMap<>();
        ArrayList aRows = new ArrayList();
        // aggregate rows
        int rowNum = 0 ;
        for ( String key : keys.keySet() ){
            XList rowData = new XList();
            List<Integer> agg = keys.get(key);
            for ( int c = 0 ; c < columns.size() ; c++  ){
                if ( colIndexes.contains(c)){
                    List<String> data = c(c,agg);
                    String value ;
                    if ( anon != null ){
                        anon.setIterationContext(this,data,c);
                        Object ret = anon.execute();
                        value = String.format("%s", ret);
                    }else{
                        Object[] a = TypeUtility.sqlmath(data);
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

    /**
     * The API to do a matrix key diff
     * @param d1 1st data matrix
     * @param d2 2nd data matrix
     * @return two sets in array, symmetric delta of d1 and d2 [ (d1-d2) , (d2-d1) ]
     * @throws Exception in error
     */
    public static Set[] key_diff( DataMatrix d1, DataMatrix d2 ) throws Exception{
        if ( d1.keys == null ){
            d1.keys();
        }
        if ( d2.keys == null ){
            d2.keys();
        }
        Set[] retVal = new Set[2];
        retVal[0] = SetOperations.set_d(d1.keys.keySet(), d2.keys.keySet());
        retVal[1] = SetOperations.set_d( d2.keys.keySet(), d1.keys.keySet() );
        return retVal ;
    }

    /**
     * Matrix diff, generates a MatrixDiff structure
     * @param args parameters
     * @return a matrix diff
     * @throws Exception in error
     */
    public static MatrixDiff diff (Object... args) throws Exception {
        Interpreter.AnonymousParam anon = null;
        if ( args.length == 0 ){
            return null;
        }
        if ( args.length > 0 ){
            if ( args[0] instanceof Interpreter.AnonymousParam){
                anon = (Interpreter.AnonymousParam)args[0];
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
        MatrixDiff matrixDiff = new MatrixDiff();
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
        matrixDiff.lr = d1;
        matrixDiff.rl = d2 ;
        List diff = new ArrayList();
        //now the rest
        Set intersection = SetOperations.set_i(  left.keys.keySet(), right.keys.keySet() );
        for ( Object i : intersection ){
            List<Integer> l = left.keys.get(i);
            List<Integer> r = right.keys.get(i);
            if ( l.size() != r.size() && l.size() != 1 ){
                throw new Exception("After Keying, multiple rows with same key! did you forget aggregate?");
            }
            int lIndex = l.get(0) ;
            int rIndex = r.get(0) ;
            Tuple L =  left.tuple(lIndex);
            Tuple R =  right.tuple(rIndex);

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
                Set colIntersect = SetOperations.set_i(L.names.keySet(), R.names.keySet() );
                for ( Object c : colIntersect ){
                    Object valLeft = L.get(c.toString());
                    Object valRight = R.get(c.toString());
                    if ( !Objects.equals(valLeft , valRight )){
                        diff.add( new Object[] { left.rows.get(lIndex) , right.rows.get(rIndex) } );
                    }
                }
            }
        }
        matrixDiff.id = diff ;

        if ( anon != null ){
            anon.removeIterationContext();
        }

        return matrixDiff;
    }

    @Override
    public String toString(){
        return "< " + columns + " , " + rows + " >" ;
    }
}
