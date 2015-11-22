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

package com.noga.njexl.lang.extension;

import com.noga.njexl.lang.*;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.dataaccess.XmlMap;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.datastructures.XList;
import com.noga.njexl.lang.extension.iterators.DateIterator;
import com.noga.njexl.lang.extension.iterators.SymbolIterator;
import com.noga.njexl.lang.extension.iterators.YieldedIterator;
import com.noga.njexl.lang.extension.oop.ScriptClassInstance;
import com.noga.njexl.lang.extension.oop.ScriptMethod;
import com.noga.njexl.lang.parser.ASTReturnStatement;
import com.noga.njexl.lang.parser.ASTStringLiteral;
import com.noga.njexl.lang.parser.JexlNode;
import com.noga.njexl.lang.extension.iterators.RangeIterator;
import com.noga.njexl.lang.parser.Parser;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.noga.njexl.lang.Interpreter.AnonymousParam;
import static com.noga.njexl.lang.Interpreter.NULL ;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <pre>
 *     This is the extension which would ensure
 *     it is a proper language.
 * </pre>
 */
public class TypeUtility {


    public static final String TYPE = "type";

    /**
     * ***** The Casting Calls  ******
     */

    public static final String INT = "int";
    public static final String CHAR = "char";
    public static final String SHORT = "short";
    public static final String BYTE = "byte";
    public static final String DOUBLE = "double";
    public static final String FLOAT = "float";
    public static final String LONG = "long";
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String STRING = "str";
    public static final String BOOL = "bool";
    public static final String BIGINT = "INT";
    public static final String BIGDECIMAL1 = "DEC";
    public static final String BIGDECIMAL2 = "NUM";

    public static final String LOAD_PATH = "load";

    public static final String BYE = "bye";

    public static final String ASSERT = "assert";
    public static final String TEST = "test";

    /**
     * ******* The Utility Calls  *********
     */

    public static final String LIST = "list";
    public static final String FILTER = "filter";
    public static final String SELECT = "select";
    public static final String PARTITION = "partition";


    public static final String FIND = "find";
    public static final String INDEX = "index";
    public static final String RINDEX = "rindex";

    public static final String JOIN = "join";


    public static final String PROJECT = "project";
    public static final String SUBLIST = "sub";

    public static final String LITERAL_LIST = "listLiteral";
    public static final String ARRAY = "array";
    public static final String ARRAY_FROM_LIST = "arrayFromList";
    public static final String SET = "set";
    public static final String MULTI_SET1 = "multiset";
    public static final String MULTI_SET2 = "mset";
    public static final String MULTI_SET3 = "setm";


    public static final String DICTIONARY = "dict";
    public static final String RANGE = "range";
    public static final String MINMAX = "minmax";
    public static final String SQLMATH = "sqlmath";
    public static final String SORT_ASCENDING = "sorta";
    public static final String SORT_DESCENDING = "sortd";

    public static final String RANDOM = "random";
    public static final String SHUFFLE = "shuffle";
    public static final String LEFT_FOLD = "lfold";
    public static final String RIGHT_FOLD = "rfold";


    public static final String TRY = "try";

    public static final String SYSTEM = "system";

    public static final String THREAD = "thread";

    public static final String TIMING_BENCHMARK = "clock";
    public static final String POLL = "until";

    /**
     * IO calls
     */

    public static final String READ = "read";
    public static final String READ_LINES = "lines";
    public static final String WRITE = "write";
    public static final String SEND = "send";

    public static final String FOPEN = "fopen";


    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String TOKEN = "tokens";
    public static final String HASH = "hash";

    public static final String MATRIX = "matrix";

    /**
     * <pre>
     * Take a look around here.
     * http://stackoverflow.com/questions/5606338/cast-primitive-type-array-into-object-array-in-java
     * </pre>
     */
    public static final Class<?>[] ARRAY_PRIMITIVE_TYPES = {
            int[].class, float[].class, double[].class, boolean[].class,
            byte[].class, short[].class, long[].class, char[].class};


    public static Object matrix(Object...args) throws Exception {
        if ( args.length == 0 ) return  null ;
        String loc  = String.valueOf(args[0]);
        args = shiftArrayLeft(args,1);
        return DataMatrix.loc2matrix(loc,args);
    }

    /**
     * Opens a file for read/write
     * @param args arguments, 2nd one is the mode "r/w/a"
     * @return PrintStream or BufferedReader
     * @throws Exception in case of any error
     */
    public static Object fopen(Object...args) throws Exception{
        if ( args.length == 0 ){
            return new BufferedReader(new InputStreamReader(System.in));
        }
        String file = String.valueOf(args[0]) ;
        if ( args.length == 1 ){
            return new BufferedReader(
                    new InputStreamReader( new FileInputStream(file)));
        }
        String mode = String.valueOf(args[1]);
        if ( "r".equalsIgnoreCase(mode)){
            if ( "@IN".equalsIgnoreCase( file )) {
                return new BufferedReader(new InputStreamReader(System.in));
            }
            return new BufferedReader(
                    new InputStreamReader( new FileInputStream(file)));
        }
        if ( "w".equalsIgnoreCase(mode)){
            if ( "@OUT".equalsIgnoreCase( file )) return System.out ;
            if ( "@ERR".equalsIgnoreCase( file )) return System.err ;
            return new PrintStream( file ) ;
        }
        if ( "a".equalsIgnoreCase(mode)){
            return new PrintStream( new FileOutputStream(file, true) ) ;
        }
        return System.err;
    }

    /**
     * Tokenizes a string
     * @param args first is the string, next the regex, then true|false for the case matching
     * @return a matcher if there is no anonymous arg, else a list of result of all matches
     */
    public static Object tokenize(Object... args){
        if ( args.length == 0 ) return false;
        AnonymousParam anon = null;
        if ( args[0] instanceof AnonymousParam ){
            anon = (AnonymousParam)args[0];
            args = shiftArrayLeft(args,1);
        }
        if ( args.length < 2 ) return false;
        String text = args[0].toString();
        String regex = args[1].toString();
        int flags = Pattern.DOTALL|Pattern.MULTILINE ;
        if ( args.length > 2 ){
            if ( castBoolean(args[2])){
                flags |= Pattern.CASE_INSENSITIVE ;
            }
        }
        Pattern pattern = Pattern.compile(regex,flags);
        Matcher matcher = pattern.matcher(text);
        if ( anon == null ) return matcher ;
        List l = new XList<>();
        // else ?
        int i = -1 ;
        while ( matcher.find() ){
            anon.setIterationContextWithPartial(text, matcher.group(), ++i, l);
            Object o ;
            try {
                o = anon.execute();
                if ( o instanceof JexlException.Continue ){
                    if ( ((JexlException.Continue) o).hasValue ){
                        l.add(((JexlException.Continue) o).value);
                    }
                    continue;
                }
                if ( o instanceof JexlException.Break ){
                    if ( ((JexlException.Break) o).hasValue ){
                        l.add(((JexlException.Break) o).value);
                    }
                    break;
                }

            }catch (Exception e){
                o = null;
            }
            l.add(o);
        }
        return l;
    }

    /**
     * Folds as in functional programming
     * https://en.wikipedia.org/wiki/Fold_(higher-order_function)
     * @param right : if true, does a right fold
     * @param args the arguments to the fold
     * @return the result of the fold
     */
    public static Object fold(boolean right, Object...args){
        if ( args.length == 0 ) return  null ;
        AnonymousParam anon = null;
        if ( args[0] instanceof AnonymousParam ){
            anon = (AnonymousParam)args[0];
            args = shiftArrayLeft(args,1);
        }
        if ( args.length == 0 ) return  null ;
        Object partial = null;
        if ( args.length > 1 ){
            partial = args[1];
        }
        if ( args[0] instanceof YieldedIterator ){
            // this is a separate branch of stuff
            YieldedIterator itr = (YieldedIterator)args[0];
            if ( right ){
                // switch it...
                itr = itr.inverse();
            }
            if ( anon == null ){ return castString( itr.list() ); }
            int i = -1;
            while ( itr.hasNext() ){
                i++;
                Object o = itr.next();
                anon.setIterationContextWithPartial(itr,o,i,partial);
                try {
                    Object r = anon.execute();
                    if ( r instanceof JexlException.Continue ){
                        if ( ((JexlException.Continue) r).hasValue ){
                            partial = ((JexlException.Continue) r).value ;
                        }
                        continue;
                    }
                    if ( r instanceof JexlException.Break ){
                        if ( ((JexlException.Break) r).hasValue ){
                            partial = ((JexlException.Break) r).value ;
                        }
                        break;
                    }
                    partial = r ;
                }catch (Throwable e){
                    System.err.println(e);
                }
            }
            return partial;
        }

        List l = combine(args[0]);

        int size = l.size() ;
        if ( anon == null ){
            if ( right ){
                StringBuffer sb = new StringBuffer();
                for ( int i = 0 ; i < size ; i++ ){
                    sb.append( l.get(size - i -1) );
                    sb.append(SetOperations.SEP);
                }
                String s = sb.toString();
                s = s.substring(0, s.lastIndexOf(SetOperations.SEP));
                return s;
            }
            return castString(l, SetOperations.SEP );
        }
        for ( int i = 0 ; i < size; i++ ){
            int j =  right? (size -i -1) : i ;
            Object o = l.get(j);
            anon.setIterationContextWithPartial(l,o,i,partial);
            try {
                Object r = anon.execute();
                if ( r instanceof JexlException.Continue ){
                    if ( ((JexlException.Continue) r).hasValue ){
                        partial = ((JexlException.Continue) r).value ;
                    }
                    continue;
                }
                if ( r instanceof JexlException.Break ){
                    if ( ((JexlException.Break) r).hasValue ){
                        partial = ((JexlException.Break) r).value ;
                    }
                    break;
                }
                partial = r ;
            }catch (Throwable e){
                System.err.println(e);
            }
        }
        return partial;
    }

    /**
     * http://en.wikipedia.org/wiki/Fisher–Yates_shuffle#The_modern_algorithm
     * @param args the argument
     * @return true if did shuffle, false if could not
     */
    public static boolean shuffle(Object... args){

        if ( args.length == 0 ) return false;
        if ( args[0] == null ) return false;

        SecureRandom random = new SecureRandom();

        if ( args[0] instanceof List ){
            List l = (List)args[0];
            int size = l.size();
            for ( int i = size-1; i>=0;i--){
                int j = random.nextInt(i+1);
                Object tmp = l.get(j);
                l.set(j, l.get(i));
                l.set(i,tmp);
            }
        }
        if ( args[0].getClass().isArray() ){
            //different treatment
            int size = Array.getLength(args[0]);
            for ( int i = size-1; i>=0;i--){
                int j = random.nextInt(i+1);
                Object tmp = Array.get(args[0], j);
                Array.set( args[0], j, Array.get(args[0],i));
                Array.set( args[0], i,tmp);
            }
        }
        return true;
    }

    public static Object random(Object...args){
        SecureRandom random = new SecureRandom();
        if ( args.length == 0 ) return random ;
        if ( args[0] == null ) return random ;

        if ( args[0] instanceof YieldedIterator ){
            args[0] = ((YieldedIterator)args[0]).list();
        }
        else if ( args[0] instanceof Iterator){
            args[0] = YieldedIterator.list((Iterator) args[0]);
        }
        if ( args[0] instanceof String){
            String l = (String)args[0];
            int index = random.nextInt(  l.length() );
            if  ( args.length > 1 ){
                // how many stuff we need?
                int count = castInteger(args[1], 1);
                StringBuffer buf = new StringBuffer();
                while(count-- > 0 ){
                    char c = l.charAt(index);
                    buf.append(c);
                    index = random.nextInt(  l.length() );
                }
                return buf.toString();
            }
            return l.charAt(index);
        }

        if ( args[0] instanceof List){
            List l = (List)args[0];
            int index = random.nextInt(  l.size() );
            if  ( args.length > 1 ){
                // how many stuff we need?
                int count = castInteger(args[1], 1);
                List r = new ArrayList<>();
                while(count-- > 0 ){
                    Object o = l.get(index);
                    r.add(o);
                    index = random.nextInt(  l.size() );
                }
                return r;
            }
            return l.get(index);
        }
        if ( args[0].getClass().isArray()){
            int size = Array.getLength(args[0]);
            int index = random.nextInt(size);
            if  ( args.length > 1 ){
                // how many stuff we need?
                int count = castInteger(args[1], 1);
                List r = new ArrayList<>();
                while(count-- > 0 ){
                    Object o = Array.get(args[0], index);
                    r.add(o);
                    index = random.nextInt(size);
                }
                return r;
            }
            return Array.get( args[0], index);
        }
        if ( args[0].getClass().isEnum()){
            Object[] values = args[0].getClass().getEnumConstants();
            int index = random.nextInt(values.length);
            if  ( args.length > 1 ){
                // how many stuff we need?
                int count = castInteger(args[1], 1);
                List r = new ArrayList<>();
                while(count-- > 0 ){
                    Object o = values[index];
                    r.add(o);
                    index = random.nextInt(values.length);
                }
                return r;
            }
            return values[index];
        }
        if ( args[0] instanceof Map ){
            Map m = (Map)args[0];
            List  l = new ListSet<>(m.keySet()) ;
            int index = random.nextInt(  l.size() );
            if  ( args.length > 1 ){
                // how many stuff we need?
                int count = castInteger(args[1], 1);
                Map r = new HashMap<>();
                while(count-- > 0 ){
                    Object k = l.get(index);
                    r.put(k,m.get(k));
                    index = random.nextInt(  l.size() );
                }
                return r;
            }
            // return the tuple
            return new Object[] { l.get(index) , m.get( l.get(index) ) };
        }
        return null;
    }

    public static Object type(Object...args){
        if ( args.length == 0 ){
            return null;
        }
        if ( args[0] == null ){
            return null;
        }
        if ( args[0] instanceof ScriptClassInstance ){
            return ((ScriptClassInstance)args[0]).getNClass();
        }
        return args[0].getClass();
    }

    public static final String HASH_MD5 = "MD5" ;


    public static String hash(Object...args) {
        if ( args.length == 0 ) return  "" ;
        String algorithm = HASH_MD5 ;
        String text = args[0].toString() ;
        if ( args.length > 1 ){
            algorithm = text ;
            text = args[1].toString() ;
        }
        try {
            MessageDigest m = MessageDigest.getInstance(algorithm);
            m.update(text.getBytes(), 0, text.length());
            BigInteger bi = new BigInteger(1, m.digest());
            return bi.toString(16);
        } catch (Exception e) {

        }
        return new Integer(text.hashCode()).toString();
    }

    public static Object json(Object... args) throws Exception {
        if (args.length == 0) {
            return null;
        }
        String text = "" ;
        if ( args.length > 1 ){
            String directive = args[0].toString().toLowerCase();
            text = args[1].toString();
            if ( directive.startsWith("f")){
                // file
                File file = new File(text);
                if ( file.exists() ) {
                    // this is the file name
                    text = readToEnd(text);
                }else{
                    return  null;
                }
            }
        }
        else{
            // auto resolve mode...
            text = args[0].toString();
            File file = new File(text);
            if ( file.exists() ) {
                // this is the file name
                text = readToEnd(text);
            }
        }

        text = text.replaceAll("[\\r\\n]", " ");
        // this is for the empty dict issue : {:}
        text = text.replaceAll("\\{[ \t\n\r]*\\}", "\\{:\\}");

        JexlEngine jexlEngine = new JexlEngine();
        Script sc = jexlEngine.createScript(text);
        JexlContext context = new MapContext();
        return sc.execute(context);
    }

    public static Object xml(Object... args) throws Exception {
        if (args.length == 0) {
            return null;
        }
        if ( args[0] instanceof Node){
            Node n = (Node)args[0];
            XmlMap.XmlElement e = new XmlMap.XmlElement(n, null);
            return e;
        }
        if ( args[0] instanceof NodeList){
            NodeList nL = (NodeList)args[0];
            ArrayList list = new ArrayList();
            for ( int i = 0 ; i <  nL.getLength() ; i++ ){
                Node n = nL.item(i);
                XmlMap.XmlElement e = new XmlMap.XmlElement(n, null);
                list.add(e);
            }
            return list;
        }

        String text = args[0].toString();
        File file = new File(text);
        if ( file.exists() ) {
            // this is the file name
            return XmlMap.file2xml(file.getAbsolutePath());
        }
        return XmlMap.string2xml(text);
    }

    public static String readStream(InputStream inputStream) throws Exception {
        StringBuffer buf = new StringBuffer();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            buf.append(inputLine);
            buf.append("\n");
        }
        in.close();
        return buf.toString();
    }

    public static String readUrl(URL url, Object...args) throws Exception{
        // set reasonable timeout
        int conTimeOut = 10000 ;
        // set reasonable read timeout
        int readTimeOut = 10000 ;
        if ( args.length > 0 ) {
            conTimeOut = castInteger(args[0], conTimeOut);
            if (args.length > 1) {
                readTimeOut = castInteger(args[1], readTimeOut);
            }
        }
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(conTimeOut);
        conn.setReadTimeout(readTimeOut);
        return readStream(conn.getInputStream());
    }

    public static String readToEnd(String location, Object... args) throws Exception {
        // if the fileName is URL?
        String name = location.toLowerCase();
        if( name.startsWith("http://") ||
                name.startsWith("https://") ||
                name.startsWith("ftp://") ){
            URL url = new URL(location);
            return readUrl(url,args);
        }

        List<String> lines = Files.readAllLines(new File(location).toPath());
        StringBuffer buffer = new StringBuffer();
        for (String l : lines) {
            buffer = buffer.append(l).append("\n");
        }
        return buffer.toString();
    }

    public static final Pattern URL =
            Pattern.compile("^http(s)?://.+", Pattern.CASE_INSENSITIVE );


    public static String send(String u, String method, Map<String,String> params ) throws Exception{

        boolean get = false ;

        if ( "GET".equalsIgnoreCase(method )) {
            get = true ;
        }
        StringBuffer buf = new StringBuffer();
        Iterator<String> iterator = params.keySet().iterator();
        if ( iterator.hasNext() ){
            String k = iterator.next();
            String v = params.get(k);
            buf.append(k).append("=").append( v );
            while ( iterator.hasNext() ){
                buf.append("&");
                k = iterator.next();
                v = params.get(k) ;
                buf.append(k).append("=").append(v);
            }
        }
        String urlParameters = buf.toString();

        if ( get ){
            URL url = new URL(u +"?" + urlParameters );
            return readUrl(url );
        }

        URL url = new URL(u);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        ((HttpURLConnection)conn).setRequestMethod(method);
        String type = "application/x-www-form-urlencoded";
        conn.setRequestProperty( "Content-Type", type );
        conn.setRequestProperty( "Content-Length", String.valueOf(urlParameters.length()));

        OutputStream writer = conn.getOutputStream();
        writer.write(urlParameters.getBytes());
        writer.flush();

        String line;
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        buf = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            buf.append(line).append("\n");
        }
        reader.close();
        writer.close();
        return buf.toString();
    }

    public static Object writeFile(Object... args) throws Exception {
        if (args.length == 0) {
            System.out.println();
            return NULL;
        }
        String path = String.valueOf(args[0]);
        if ( URL.matcher(path).matches() ){
            args = shiftArrayLeft(args,1);
            return send(path,"POST", makeDict(args));
        }

        PrintStream ps = System.out ;
        if ( args[0] instanceof PrintStream ){
            ps = (PrintStream)args[0];
            args = shiftArrayLeft(args,1);
        }
        if ( args.length == 0 ){
            ps.println();
            return NULL;
        }
        if (args.length == 1) {
            ps.printf("%s\n", args[0]);
            return NULL;
        }

        String fmt = String.valueOf(args[0]);
        args = shiftArrayLeft(args,1);
        if ( fmt.contains("%")){
            // formats...
            ps.printf(fmt,args);
            return NULL;
        }

        String fileName = fmt ;
        String data = "42" ;
        if ( args.length > 0 ){
            data = String.valueOf(args[0]);
        }
        Files.write(new File(fileName).toPath(), data.getBytes());
        return NULL;
    }

    public static String read(Object... args) throws Exception {
        if (args.length == 0) {
            return System.console().readLine();
        }
        if ( args[0] instanceof InputStream ){
            return readStream((InputStream) args[0]);
        }
        if ( args[0] instanceof String ) {
            String loc = (String) args[0];
            args = shiftArrayLeft(args,1);
            return readToEnd(loc,args);
        }
        if ( args[0] instanceof URL ) {
            URL loc = (URL) args[0];
            args = shiftArrayLeft(args,1);
            return readUrl(loc, args);
        }
        if ( args[0] instanceof Path) {
            return readToEnd( ((Path)args[0]).toFile().getCanonicalPath() ) ;
        }
        return null;
    }

    public static XList<String> readLines(Object... args) throws Exception {
        XList<String> lines = null;
        if (args.length == 0) {
            lines = new XList<>();
            while (true) {
                String l = System.console().readLine();
                if (l == null) {
                    break;
                }
                lines.add(l);
            }
            return lines;
        }
        lines = new XList<>( Files.readAllLines(new File(args[0].toString()).toPath()) );
        return lines;
    }

    public static BigInteger castBigInteger(Object... args) {
        if (args.length == 0) {
            return BigInteger.ZERO;
        }
        int base = 10;
        if (args.length > 1) {
            base = castInteger(args[1]);
        }
        return new BigInteger(args[0].toString(), base);
    }

    public static BigDecimal castBigDecimal(Object... args) {
        if (args.length == 0) {
            return BigDecimal.ZERO;
        }
        if (args[0] instanceof BigInteger) {
            return new BigDecimal((BigInteger) args[0]);
        }
        return new BigDecimal(args[0].toString(), MathContext.UNLIMITED);
    }

    public static Double castDouble(Object... args) {
        if (args.length == 0) {
            return 0.0;
        }
        try {
            if (args[0] instanceof Double) {
                return (Double) args[0];
            }
            if (args[0] instanceof Float) {
                return ((Float) args[0]).doubleValue();
            }
            Object objectValue = args[0];
            Double val = Double.valueOf(objectValue.toString());
            return val;
        } catch (Exception e) {
            if (args.length > 1) {
                return castDouble(args[1]);
            }
            return null;
        }
    }

    public static Float castFloat(Object... args) {
        Double doubleValue = castDouble(args);
        if (doubleValue != null) {
            float f = doubleValue.floatValue();
            return new Float(f);
        }
        return null;
    }

    public static Byte castByte(Object... args) {
        Integer i = castInteger(args);
        if (i != null) {
            return new Byte(i.byteValue());
        }
        Character c = castChar(args);
        if (c != null) {
            new Byte(new Integer(c).byteValue());
        }
        return null;
    }

    public static Character castChar(Object... args) {
        if (args.length == 0) {
            return 0;
        }
        if (args[0] instanceof String) {
            String s = ((String) args[0]);
            if ( s.length() != 1 ){ return  null; }
            return s.charAt(0);
        }
        Double doubleValue = castDouble(args);
        if (doubleValue != null) {
            char[] cc = Character.toChars(doubleValue.intValue());
            return new Character(cc[0]);
        }
        return null;
    }

    public static Short castShort(Object... args) {
        Integer i = castInteger(args);
        if (i != null) {
            return new Short(i.shortValue());
        }
        Character c = castChar(args);
        if (c != null) {
            return new Short(new Integer(c).shortValue());
        }
        return null;
    }

    public static Integer castInteger(Object... args) {
        Double doubleValue = castDouble(args);
        if (doubleValue != null) {
            int i = (int) Math.floor(doubleValue);
            return new Integer(i);
        }
        return null;
    }

    public static Long castLong(Object... args) {
        Double doubleValue = castDouble(args);
        if (doubleValue != null) {
            long l = (long) Math.floor(doubleValue);
            return new Long(l);
        }
        return null;
    }

    public static DateTime castTime(Object... args) {
        if (args.length == 0) {
            return new DateTime();
        }
        if (args[0] instanceof Date || args[0] instanceof DateTime) {
            return new DateTime(args[0]);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
        try {
            if (args.length > 1) {
                dateTimeFormatter = DateTimeFormat.forPattern(args[1].toString());
            }
            return DateTime.parse(args[0].toString(), dateTimeFormatter);
        } catch (Exception e) {
        }
        return null;
    }

    public static Date castDate(Object... args) {
        if (args.length == 0) {
            return new Date();
        }
        if (args[0] instanceof Date) {
            return (Date) args[0];
        }
        if (args[0] instanceof DateTime) {
            return ((DateTime) args[0]).toDate();
        }

        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
        try {
            dateTimeFormatter.setLenient(false);
            if (args.length > 1) {
                dateTimeFormatter = new SimpleDateFormat(args[1].toString());
                if (args.length > 2) {
                    dateTimeFormatter.setLenient(castBoolean(args[2],false));
                    if ( args.length > 3 ){
                        dateTimeFormatter.setTimeZone(TimeZone.getTimeZone(args[3].toString()));
                    }
                }
            }

            return dateTimeFormatter.parse(args[0].toString());
        } catch (Exception e) {
        }
        return null;
    }

    public static Boolean castBoolean(Object... args) {
        if (args.length == 0) {
            return Boolean.FALSE;
        }

        if (args[0] instanceof Boolean) {
            return (Boolean) args[0];
        }

        if (args[0] instanceof String) {
            String litValue = (String) args[0];

            litValue = litValue.toLowerCase();
            if (litValue.equals("true")) {
                return Boolean.TRUE;
            }
            if (litValue.equals("false")) {
                return Boolean.FALSE;
            }
        }
        Double d = castDouble(args);
        if (d != null) {
            return new Boolean(d != 0);
        }
        if (args.length > 1) {
            List opts = from(args[1]);
            if ( opts.size() > 1 ){
                // options are passed to match
                Object t = opts.get(0);
                Object f = opts.get(1);
                if ( Objects.equals(args[0], t ) ) return true ;
                if ( Objects.equals(args[0], f ) ) return false ;
            }
            return castBoolean(args[1]);
        }
        return null;
    }

    public static String castString(Object... args) {
        if (args.length == 0) {
            return "";
        }
        if ( args[0] == null ){
            /* what should I return?
                Theoretically null, "" , "null" are possible.
                But then, I choose 'null'.
            */
            return "null" ; // because it is nJexl null param
        }

        if (args[0] instanceof AnonymousParam) {
            if ( args.length > 1 ) {
                if ( args[1] instanceof Collection || args[1].getClass().isArray() ) {
                    Object[] _args = new Object[]{args[0], args[1]};
                    String sep = SetOperations.SEP ;
                    if (args.length > 2) {
                        sep = String.valueOf(args[2]);
                    }
                    List l = combine(_args);
                    return castString(l,sep);
                }
            }
            AnonymousParam anon = (AnonymousParam) args[0];
            args = shiftArrayLeft(args, 1);
            if ( args.length > 0 ) {
                anon.setIterationContext(args[0], args[0], -1);
                Object ret = anon.execute();
                anon.removeIterationContext();
                args[0] = ret; //set it up
                return castString(args);
            }
            return  "" ;
        }

        if (args[0] instanceof String) {
            return (String) args[0];
        }
        if (args[0] instanceof DateTime) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
            if (args.length > 1 && args[1] != null) {
                dateTimeFormatter = DateTimeFormat.forPattern(args[1].toString());
            }
            try {
                return dateTimeFormatter.print((DateTime) args[0]);
            } catch (Exception e) {
                return args[0].toString() ; // make it return faster...
            }
        } if (args[0] instanceof Date) {
            SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
            if (args.length > 1 && args[1] != null) {
                dateTimeFormatter = new SimpleDateFormat(args[1].toString());
            }
            try {
                return dateTimeFormatter.format(args[0]);
            } catch (Exception e) {
                return args[0].toString() ;
            }
        } if ( args[0] instanceof Float ||
                args[0] instanceof Double ||
                 args[0] instanceof BigDecimal){
            if ( args.length > 1 ){
                if ( args[1] instanceof Integer ){
                    String fmt = String.format("%%.%df", args[1]);
                     return String.format(fmt, args[0]);
                }
                if ( args[1] instanceof String ){
                    DecimalFormat format = new DecimalFormat(args[1].toString());
                    return format.format( args[0] );
                }
            }
            return args[0].toString();
        }
        Class c = args[0].getClass();
        if (List.class.isAssignableFrom(c) || c.isArray()) {
            List l = from(args[0]);
            StringBuffer buf = new StringBuffer();
            String sep = ",";
            if (args.length > 1 && args[1] != null) {
                sep = args[1].toString();
            }
            for (Object o : l) {
                buf.append(o).append(sep);
            }
            if ( buf.length() == 0 ){ return  "" ; }
            String ret = buf.substring(0,buf.lastIndexOf(sep));
            return ret;
        }
        if ( args[0] instanceof BigInteger ){
            if ( args.length > 1 ){
                return ((BigInteger)args[0]).toString( castInteger(args[1] ));
            }
        }
        return args[0].toString();
    }

    public static XList makeLiteralList(Object... argv) {
        XList list = new XList();
        for (int i = 0; i < argv.length; i++) {
            list.add(argv[i]);
        }
        return list;
    }

    public static Object[] getArray(Object val) {
        Class<?> valKlass = val.getClass();
        Object[] outputArray = null;

        for (Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES) {
            if (valKlass.isAssignableFrom(arrKlass)) {
                int arrlength = Array.getLength(val);
                outputArray = new Object[arrlength];
                for (int i = 0; i < arrlength; ++i) {
                    outputArray[i] = Array.get(val, i);
                }
                break;
            }
        }
        if (outputArray == null) // not primitive type array
            outputArray = (Object[]) val;

        return outputArray;
    }

    /**
     * <pre>
     *     I am given an element, I want to make a mutable list out of it.
     *     If This is actually a list, then creates a list exactly the same, only mutable
     *     If this is an array, converts to a list
     *     If this is a single element, create a list containing this element
     * </pre>
     *
     * @param object input object which needs to be unwinded
     * @return a list unwinding the object
     */
    public static XList from(Object object) {
        if (object == null) {
            return null;
        }
        XList list = new XList();
        if (object instanceof Collection) {
            Collection l = (Collection) object;
            list.addAll(l);
        } else if ( object instanceof YieldedIterator ){
            list.addAll(((YieldedIterator)object).list());
        }
        else if (object.getClass().isArray()) {
            Object[] array = getArray(object);
            List l = Arrays.asList(array);
            list.addAll(l);
        } else if (object instanceof Map ) {
            Map m = ((Map)object);
            list = new XList(m);
        }
        else {
            list.add(object);
        }
        return list;
    }

    /**
     * <pre>
     *     Very important list combine routine
     *     Given ( arg_0 , arg_1 , arg_2 , ... )
     *     It would check if arg_xxx is a list type, then would add all element of arg_xxx to the return list.
     *
     * </pre>
     *
     * @param args individual objects to be passed
     * @return a list combining the unwinded args
     */
    public static List combine(Object... args) {
        AnonymousParam anon = null;
        XList list = new XList();
        if (args.length > 1) {
            if (args[0] instanceof AnonymousParam) {
                anon = (AnonymousParam) args[0];
                args = shiftArrayLeft(args, 1);
            }

        }
        for (int i = 0; i < args.length; i++) {
            List l = from(args[i]);
            list.addAll(l);
        }

        if (anon != null) {
            XList l = new XList();
            int i = 0;
            for (Object o : list) {
                anon.setIterationContextWithPartial(list, o, i,l);
                Object ret = anon.execute();
                if ( ret instanceof JexlException.Continue ){
                    continue;
                }
                if ( ret instanceof JexlException.Break ){
                    JexlException.Break br = ((JexlException.Break)ret) ;
                    if ( br.hasValue ){
                        l.add(br.value);
                    }
                    break;
                }
                l.add(ret);
                i++;
            }
            list = l;
            anon.removeIterationContext();
        }

        return list;
    }

    public static List[] partition(Object... args) {
        AnonymousParam anon = null;
        XList list = new XList();
        if (args.length > 1) {
            if (args[0] instanceof AnonymousParam) {
                anon = (AnonymousParam) args[0];
                args = shiftArrayLeft(args, 1);
            }
        }
        for (int i = 0; i < args.length; i++) {
            List l = from(args[i]);
            list.addAll(l);
        }
        XList reject = new XList();
        if (anon != null) {
            XList l = new XList();
            int i = 0;
            for (Object o : list) {
                boolean broken = false ;
                anon.setIterationContextWithPartial(list, o, i,l);
                Object ret = anon.execute();
                if ( ret instanceof JexlException.Continue ){
                    continue;
                }
                if ( ret instanceof JexlException.Break ){
                    JexlException.Break br = (JexlException.Break)ret;
                    /* breaks inclusive : by default takes the item
                      unless explicitly specified not to. */
                    ret = true ;
                    if ( br.hasValue ){
                        ret = br.value ;
                    }
                    broken = true ;
                }

                if (castBoolean(ret, false)) {
                    //should add _ITEM_ 's value, if anyone modified it
                    l.add(anon.interpreter.getContext().get(Script._ITEM_));
                }else{
                    reject.add( anon.interpreter.getContext().get(Script._ITEM_) );
                }
                if ( broken ){ break; }
                i++;
            }
            list = l;
            anon.removeIterationContext();
        }
        return new XList[]{ list , reject};
    }

    public static List filter(Object... args) {
        List[] partition = partition(args);
        return partition[0];
    }

    public static int index(Object... args) {
        AnonymousParam anon = null;
        XList list = new XList();
        if (args.length > 1) {
            if (args[0] instanceof AnonymousParam) {
                anon = (AnonymousParam) args[0];
                args = shiftArrayLeft(args, 1);
            }
        }
        if ( args.length < 1){ return -1; }
        Object item = args[0];
        int start = 0;
        if (anon == null) {
            if ( args.length < 2){ return -1; }
            if ( args[1] instanceof CharSequence ){
                if ( item == null ) return -1;
                return args[1].toString().indexOf( item.toString() );
            }
            if ( JexlArithmetic.areListOrArray( item , args[1] )){
                String l = castString(item,SetOperations.SEP);
                String r = castString(args[1],SetOperations.SEP);
                int inx = r.indexOf(l);
                // how many seps are there?
                int c = 0;
                char sep = SetOperations.SEP.charAt(0);
                for ( int i = 0 ; i < inx; i++ ){
                    if (  r.charAt(i) == sep ){
                        c++;
                    }
                }
                return inx - c ;
            }

            start = 1;
        }
        for (int i = start; i < args.length; i++) {
            List l = from(args[i]);
            if ( l == null ) continue;
            list.addAll(l);
        }
        if (anon == null) {
            return list.indexOf(item);
        }

        int i = 0;

        boolean found = false;
        for (Object o : list) {
            anon.setIterationContext(list, o, i);
            Object ret = anon.execute();
            if ( ret instanceof JexlException.Continue ){
                continue;
            }
            found = castBoolean(ret, false);
            if (found) {
                break;
            }
            i++;
        }
        anon.removeIterationContext();
        if (found) {
            return i;
        }

        return -1;
    }

    public static int rindex(Object... args) {
        AnonymousParam anon = null;
        XList list = new XList();
        if (args.length > 1) {
            if (args[0] instanceof AnonymousParam) {
                anon = (AnonymousParam) args[0];
                args = shiftArrayLeft(args, 1);
            }
        }
        if ( args.length < 1){ return -1; }
        Object item = args[0];
        int start = 0;
        if (anon == null) {
            if ( args.length < 2){ return -1; }
            if ( args[1] instanceof CharSequence ){
                if ( item == null ) return -1;
                return args[1].toString().lastIndexOf( item.toString() );
            }
            if ( JexlArithmetic.areListOrArray( item , args[1] )){
                String l = castString(item,SetOperations.SEP);
                String r = castString(args[1],SetOperations.SEP);
                if ( l.isEmpty() ){
                    if ( args[1] instanceof List ){
                        return ((List)args[1]).size() - 1;
                    }
                    return Array.getLength(args[1]) - 1;
                }
                int inx = r.lastIndexOf(l);
                // how many seps are there?
                int c = 0;
                char sep = SetOperations.SEP.charAt(0);
                for ( int i = 0 ; i < inx; i++ ){
                    if (  r.charAt(i) == sep ){
                        c++;
                    }
                }
                return inx - c ;
            }

            start = 1;
        }
        for (int i = start; i < args.length; i++) {
            List l = from(args[i]);
            if ( l == null ) continue;
            list.addAll(l);
        }
        if (anon == null) {
            return list.lastIndexOf(item);
        }

        int i = list.size() - 1 ;

        boolean found = false;
        for (; i >= 0 ; i-- ) {
            anon.setIterationContext(list, list.get(i), i);
            Object ret = anon.execute();
            if ( ret instanceof JexlException.Continue ){
                continue;
            }
            found = castBoolean(ret, false);
            if (found) {
                break;
            }
        }
        anon.removeIterationContext();
        if (found) {
            return i;
        }

        return -1;
    }


    public static Iterator range(Object... args) throws Exception {

        if (args.length == 0) {
            return new RangeIterator();
        }
        long end = 42;
        long start = 1;

        if (args.length > 0) {
            if ( args[0] instanceof Date || args[0] instanceof DateTime ){
                // a different iterator ...
                DateTime et = castTime(args[0]);
                if ( args.length > 1 ){
                    if ( args[1] instanceof Date || args[1] instanceof DateTime  ){
                        DateTime st = castTime(args[1]);
                        if ( args.length > 2 ){
                            String d = args[2].toString() ;
                            try {
                                long dur = Long.parseLong(d);
                                return new DateIterator(et, st, new Duration(dur));
                            }catch (Exception e){
                                return new DateIterator(et, st, new Duration( DateIterator.parseDuration(d)));
                            }
                        }
                        return new DateIterator(et,st);
                    }
                }
                return new DateIterator(et);
            }
            if ( args[0] instanceof String ){
                // a different iterator ...
                Character et = castChar(args[0]);
                if ( args.length > 1 ){
                    if ( args[1] instanceof String ){
                        Character st = castChar(args[1]);
                        if ( args.length > 2 ){
                            String d = args[2].toString() ;
                            try {
                                short dur = Short.parseShort(d);
                                return new SymbolIterator(et, st,dur);
                            }catch (Exception e){
                                return new SymbolIterator(et, st,(short)1);
                            }
                        }
                        return new SymbolIterator(et,st);
                    }
                }
                return new SymbolIterator(et);
            }
            end = Long.valueOf(args[0].toString());
            if (args.length > 1) {
                start = Long.valueOf(args[1].toString());
                if (args.length > 2) {
                    long space = Long.valueOf(args[2].toString());
                    return new RangeIterator(end, start, space);
                }
            }
        }
        return new RangeIterator(end, start);
    }

    public static Object[] shiftArrayLeft(Object[] args, int shift) {
        Object[] array = new Object[args.length - shift];
        for (int i = 0; i < array.length; i++) {
            array[i] = args[i + shift];
        }
        return array;
    }

    public static ListSet set(Object... args) {
        List list = combine(args);
        ListSet set = new ListSet();
        set.addAll(list);
        return set;
    }

    public static Object[] minmax(Object... args) throws Exception {
        if ( args.length == 0 ) return null ;
        if ( args[0] instanceof AnonymousParam ){
            AnonymousParam  anon = (AnonymousParam)args[0];
            args = shiftArrayLeft(args,1);
            List l = combine(args);
            if ( l.size() == 0 ) return  null;
            Object min = l.get(0);
            Object max = min;
            for ( int i = 1 ; i < l.size() ;i++ ){
                Object item = l.get(i);
                Object o = new Object[]{ item , min };
                anon.setIterationContext(l, o ,i);
                Object ret = anon.execute();
                boolean lt = castBoolean(ret,false);
                if ( lt ){
                    // change min
                    min = item ;
                }
                o = new Object[]{ max , item };
                anon.setIterationContext(l, o ,i);
                ret = anon.execute();
                lt = castBoolean(ret,false);
                if ( lt ){
                    // change max
                    max = item ;
                }
            }
            Object[] container = new Object[]{ min, max };
            return container ;
        }
        List l = combine(args);
        if ( l.size() == 0 ) return  null;
        Object min = l.get(0);
        Object max = min;
        for ( int i = 1 ; i < l.size() ;i++ ){
            Comparable item = (Comparable)l.get(i);
            if ( item.compareTo(min) < 0 ){
                min = item ;
            }
            if ( item.compareTo(max) > 0 ){
                max = item ;
            }
        }
        Object[] container = new Object[]{ min, max };
        return container ;
    }

    public static HashMap makeDict(Object... args) throws Exception {
        HashMap map = new HashMap();
        // empty dict
        if ( args.length == 0 ){
            return map;
        }
        // clone the dict
        if (args.length == 1 && args[0] instanceof Map ) {
            map.putAll((Map) args[0]);
            return map;
        }
        if ( args[0] instanceof AnonymousParam ){
            // now what we do?
            AnonymousParam anon = (AnonymousParam)args[0];
            args = shiftArrayLeft(args,1);
            if ( args.length == 1 ) {
                List list = from(args[0]);
                for ( int i = 0 ; i < list.size();i++ ){
                    Object o = list.get(i);
                    anon.setIterationContext(list,o,i);
                    Object ret = anon.execute();
                    if ( ret == null ){
                        map.put(o,null);
                    }
                    else if ( ret instanceof Map ){
                        map.putAll( (Map)ret );
                    }else if ( ret.getClass().isArray() ){
                        Object key = Array.get( ret, 0 );
                        Object value = Array.get( ret, 1 );
                        map.put(key,value);
                    }
                }
            }else{
                List keyList = from(args[0]);
                List valueList = from(args[1]);
                if ( keyList.size() != valueList.size()){
                    return map;
                }
                int size = keyList.size();
                for ( int i = 0 ; i < size ;i++ ){
                    Object k = keyList.get(i);
                    Object v = valueList.get(i);

                    anon.setIterationContext(new Object[]{ keyList,valueList}
                            ,new Object[]{k,v},i);
                    Object ret = anon.execute();
                    if ( ret == null ){
                        break;
                    }
                    if ( ret instanceof Map ){
                        map.putAll( (Map)ret );
                    }else if ( ret.getClass().isArray() ){
                        Object key = Array.get( ret, 0 );
                        Object value = Array.get( ret, 1 );
                        map.put(key,value);
                    }
                }
            }
            anon.removeIterationContext();
            return map;
        }

        if (args.length != 2) {
            return map;
        }
        List keyList = from(args[0]);
        List valueList = from(args[1]);
        if (keyList.size() != valueList.size()) {
            throw new Exception("Key and Value Arrays/Lists are not of same length!");
        }

        for (int i = 0; i < keyList.size(); i++) {
            map.put(keyList.get(i), valueList.get(i));
        }
        return map;

    }

    public static Object[] sqlmath(Object... argv) {
        Object[] math = new Object[]{null, null, null};
        JexlArithmetic arithmetic = new JexlArithmetic(false);
        List list = combine(argv);
        if (list.size() > 0) {
            int index = 0;
            Object obj = list.get(index);

            math[0] = obj;
            math[1] = math[0];
            math[2] = math[0];
            for (index = 1; index < list.size(); index++) {
                obj = list.get(index);

                if (arithmetic.lessThan(obj, math[0])) {
                    math[0] = obj;  // MIN
                }
                if (arithmetic.greaterThan(obj, math[1])) {
                    math[1] = obj;   // MAX
                }
                math[2] = arithmetic.add(math[2], obj); // SUM
            }
        }
        return math;
    }

    public static void bye(Object... args) {
        String msg = "BYE received, we will exit this script...";
        Integer exitStatus = null;

        System.out.println(Main.strArr(args));

        if (args.length > 1 && args[0] instanceof Number) {
            exitStatus = ((Number) args[0]).intValue();
        }
        if (exitStatus != null) {
            System.exit(exitStatus);
        }
        JexlNode node = new ASTReturnStatement(Parser.JJTARRAYLITERAL);
        JexlNode child = new ASTStringLiteral(Parser.STRING_LITERAL);
        child.image = "__bye__";
        node.jjtAddChild(child, 0);
        throw new JexlException.Return(node, msg, args);
    }

    public static boolean test(Object... args) {
        if (args.length == 0) {
            return true;
        }
        boolean ret;
        Object args0 = args[0];
        args = shiftArrayLeft(args,1);
        if ( args0 instanceof AnonymousParam ){
            ((AnonymousParam)args0).setIterationContext(args,args,0);
            try {
                Object o = ((AnonymousParam) args0).execute();
                ret = castBoolean(o);
            }catch (Throwable t){
                ret = false ;
            }
        }
        else {
            ret = castBoolean(args0, false);
        }
        if ( !ret ){
            bye(args);
        }
        // log it - later problem - not now
        return ret;
    }

    public static Object[] array(Object... args) {
        List l = combine(args);
        Object[] a = new Object[l.size()];
        l.toArray(a);
        return a;
    }

    public static class AnonymousComparator implements Comparator{

        public final AnonymousParam anon;

        public final Collection collection;

        public final boolean reverse;

        public AnonymousComparator(AnonymousParam anon,Collection collection,boolean reverse){
            this.anon = anon ;
            this.collection = collection ;
            this.reverse = reverse ;
        }

        @Override
        public int compare(Object o1, Object o2) {
            Object[] pair = new Object[]{ o1, o2 };
            anon.setIterationContext(collection, pair , -1);
            Object ret = anon.execute();
            anon.removeIterationContext();
            boolean smaller = castBoolean(ret,false);
            if ( reverse ){
                if ( smaller ){
                    return 1 ;
                }
                return -1;
            }
            if ( smaller ){
                return -1 ;
            }
            return 1 ; // unstable... but fine I suppose
        }
    }

    public static Collection ascending(Object...args){
        if ( args.length == 0 ){
            return Collections.emptyList();
        }
        if ( args.length > 0 ){
            if ( args[0] instanceof AnonymousParam ){
                AnonymousParam anon = (AnonymousParam)args[0];
                args = shiftArrayLeft(args, 1);
                List l = combine( args);
                AnonymousComparator comparator = new AnonymousComparator(anon,l,false) ;
                Collections.sort( l, comparator);
                return l;
            }
        }
        List l = combine( args);
        Collections.sort(l);
        return l;
    }

    public static Collection descending(Object...args){
        if ( args.length == 0 ){
            return Collections.emptyList();
        }
        if ( args.length > 0 ){
            if ( args[0] instanceof AnonymousParam ){
                AnonymousParam anon = (AnonymousParam)args[0];
                args = shiftArrayLeft(args, 1);
                List l = combine( args);
                AnonymousComparator comparator = new AnonymousComparator(anon,l,true) ;
                Collections.sort( l, comparator);
                return l;
            }
        }
        List l = combine(args) ;
        Collections.sort(l, Collections.reverseOrder());
        return l;
    }

    public static Object guardedBlock(Object...args){
        if ( args.length == 0 ){
            return null;
        }
        if ( !(args[0] instanceof AnonymousParam) ){
            return args[0];
        }
        AnonymousParam anon = (AnonymousParam)args[0];
        args = shiftArrayLeft(args,1);
        try{
            return anon.execute();
        }catch (Throwable throwable){
            if ( args.length == 0 ){
                return throwable.getCause() ;
            }
            return args[0];
        }
    }

    public static Object benchmark(Object...args){
        Object[] ret = new Object[ ]{0,null};
        if ( args.length == 0 ){
            return ret;
        }
        if ( !(args[0] instanceof AnonymousParam) ){
            return ret;
        }
        AnonymousParam anon = (AnonymousParam)args[0];
        args = shiftArrayLeft(args,1);
        long start = System.nanoTime();
        long end;

        try{
            ret[1] = anon.execute();
        }catch (Throwable throwable){
            ret[1] = throwable ;
        }finally {
            end = System.nanoTime();
        }
        ret[0] = end - start ;
        return ret ;
    }

    public static boolean wait(Object...args) throws Exception {

        int pollInterval = 100 ; // in ms
        int duration = 3000 ; // in ms

        if ( args.length == 0 ){
            Thread.sleep(duration);
            return true ;
        }
        AnonymousParam anon = null ;
        if ( args[0] instanceof AnonymousParam) {
            anon = (AnonymousParam) args[0];
            args = shiftArrayLeft(args, 1);
        }

        if ( args.length > 0 ){
            duration = castInteger(args[0],duration);
            if ( anon == null ){
                Thread.sleep(duration);
                return true ;
            }
            if ( args.length > 1 ){
                pollInterval = castInteger(args[1],pollInterval);
            }
        }

        long start = System.currentTimeMillis();
        while(true){
            try {
                Thread.sleep(pollInterval);
                Object er = anon.execute();
                boolean ret = castBoolean(er,false);
                if ( ret ) return true ;
            } catch (Throwable throwable) {
                // do nothing
            }
            long cur = System.currentTimeMillis();
            if ( cur - start > duration ) break;
        }
        return false;
    }

    public static Object interceptCastingCall(String methodName, Object[] argv, Boolean[] success) throws Exception {

        if (success != null) {
            success[0] = true;
        }
        // when length is non zero and last arg is this __args__ then we are overwriting
        if ( argv.length > 0 && argv[argv.length-1] instanceof Interpreter.NamedArgs ){
            Interpreter.NamedArgs na = (Interpreter.NamedArgs)argv[argv.length-1];
            if ( !Script.ARGS.equals(na.name )) { throw new Exception("Named Args is not " + Script.ARGS ) ; }
            Object[] args = array(na.value );
            if ( argv.length == 2 && argv[0] instanceof AnonymousParam ){
                AnonymousParam anon = ( AnonymousParam)argv[0];
                argv = new Object[args.length + 1];
                argv[0] = anon ;
                for ( int i = 1 ; i < argv.length ;i++){
                    argv[i] = args[i-1];
                }
            }else{
                // overwrite everything
                argv = args ;
            }
        }

        switch (methodName) {
            case TYPE :
                return type(argv);
            case BYTE:
                return castByte(argv);
            case CHAR:
                return castChar(argv);
            case SHORT:
                return castShort(argv);
            case INT:
                return castInteger(argv);
            case LONG:
                return castLong(argv);
            case FLOAT:
                return castFloat(argv);
            case DOUBLE:
                return castDouble(argv);
            case BIGDECIMAL1:
            case BIGDECIMAL2:
                return castBigDecimal(argv);
            case BIGINT:
                return castBigInteger(argv);
            case BOOL:
                return castBoolean(argv);
            case STRING:
                return castString(argv);
            case TIME:
                return castTime(argv);
            case DATE:
                return castDate(argv);
            case LITERAL_LIST:
                return makeLiteralList(argv);
            case LIST:
                return combine(argv);
            case PROJECT:
            case SUBLIST:
                return sublist(argv);
            case FILTER:
            case SELECT:
                return filter(argv);
            case PARTITION:
                return partition(argv);
            case FIND:
            case INDEX:
                return index(argv);
            case RINDEX:
                return rindex(argv);
            case JOIN:
                return SetOperations.join_c(argv);
            case ARRAY:
                return array(argv);
            case SET:
                return set(argv);
            case DICTIONARY:
                return makeDict(argv);
            case RANGE:
                return range(argv);
            case ARRAY_FROM_LIST:
                return getArray(makeLiteralList(argv));
            case MINMAX:
                return minmax(argv);
            case SQLMATH:
                return sqlmath(argv);
            case READ:
                return read(argv);
            case READ_LINES:
                return readLines(argv);
            case WRITE:
                return writeFile(argv);
            case SEND:
                return send(String.valueOf(argv[0]),
                        String.valueOf(argv[1]), makeDict(argv[2]));
            case BYE:
                bye(argv);
                break;
            case ASSERT:
            case TEST:
                return test(argv);
            case LOAD_PATH:
                return ReflectionUtility.load_path(argv);
            case JSON:
                return json(argv);
            case XML:
                return xml(argv);
            case MULTI_SET1:
            case MULTI_SET2:
            case MULTI_SET3:
                return SetOperations.multiset(argv);
            case SORT_ASCENDING:
                return ascending(argv);
            case SORT_DESCENDING:
                return descending(argv);
            case TRY:
                return guardedBlock(argv);
            case TIMING_BENCHMARK:
                return benchmark(argv);
            case POLL:
                return wait(argv);
            case SYSTEM:
                return system(argv);
            case SHUFFLE:
                return shuffle(argv);
            case RANDOM:
                return random(argv);
            case THREAD:
                return thread(argv);

            case LEFT_FOLD:
                return fold(false,argv);
            case RIGHT_FOLD:
                return fold(true,argv);
            case TOKEN:
                return tokenize(argv);
            case HASH:
                return hash(argv);
            case FOPEN:
                return fopen(argv);
            case MATRIX:
                return matrix(argv);
            default:
                if (success != null) {
                    success[0] = false;
                }
                break;
        }
        return methodName;
    }

    private static Object sublist(Object... args) {
        if (args.length == 0) {
            return null;
        }
        List l = from(args[0]);
        int start = 0;
        int end = l.size() - 1;
        if (args.length > 1) {
            start = castInteger(args[1], start);
            if (args.length > 2) {
                end = castInteger(args[2], end);
            }
        }
        XList r = new XList();
        for (int i = start; i <= end; i++) {
            r.add(l.get(i));
        }
        return r;
    }

    public static int system(Object...args) throws Exception {

        if ( args.length ==  1 ){
            Process p = Runtime.getRuntime().exec(args[0].toString());
            p.waitFor();

            // terrible nomenclature in Java
            BufferedReader or = new BufferedReader( new InputStreamReader(p.getInputStream()));
            BufferedReader er = new BufferedReader( new InputStreamReader(p.getErrorStream()));
            String line ;
            while( (line = or.readLine())!= null ){
                System.out.println(line);
            }
            while( (line = er.readLine())!= null ){
                System.err.println(line);
            }
            return p.exitValue();
        }
        return 0;
    }

    public static Thread thread(Object...args) throws Exception{
        if ( args.length == 0 ){
            return Thread.currentThread();
        }
        if ( args[0] instanceof AnonymousParam ){
            AnonymousParam anonymousParam = (AnonymousParam)args[0];
            args = shiftArrayLeft(args,1);
            Thread t = new Thread(anonymousParam);
            anonymousParam.setIterationContext(args, t, t.getId() );
            t.start();
            return t;
        }
        return Thread.currentThread();
    }
}
