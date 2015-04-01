package org.apache.commons.jexl2.extension;

import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.extension.iterators.RangeIterator;
import org.apache.commons.jexl2.parser.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.jexl2.Interpreter.AnonymousParam;

/**
 * <pre>
 *     This is the extension which would ensure
 *     it is a proper language.
 * </pre>
 */
public class TypeUtility {

    /**
     * ***** The Casting Calls  ******
     */
    public static final String INT = "int";
    public static final String DOUBLE = "double";
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

    public static final String TEST = "test";

    /**
     * ******* The Utility Calls  *********
     */

    public static final String LIST = "list";
    public static final String FILTER = "filter";
    public static final String SELECT = "select";
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
    public static final String WITHIN = "within";
    public static final String SQLMATH = "sqlmath";

    /**
     * IO calls
     */

    public static final String READ = "read";
    public static final String READ_LINES = "lines";
    public static final String WRITE = "write";

    public static final String _ITEM_ = "$";

    public static final String _INDEX_ = "_";


    /**
     * <pre>
     * Take a look around here.
     * http://stackoverflow.com/questions/5606338/cast-primitive-type-array-into-object-array-in-java
     * </pre>
     */
    public static final Class<?>[] ARRAY_PRIMITIVE_TYPES = {
            int[].class, float[].class, double[].class, boolean[].class,
            byte[].class, short[].class, long[].class, char[].class};

    public static String readToEnd(String fileName) throws Exception{
        List<String> lines = Files.readAllLines(new File(fileName).toPath());
        StringBuffer buffer = new StringBuffer();
        for(String l : lines ){
            buffer = buffer.append(l).append("\n");
        }
        return buffer.toString();
    }

    public static void writeFile(Object... args) throws Exception{
        if ( args.length == 0 ){
            System.out.println();
        }
        if ( args.length == 1 ){
            System.out.println(args[0]);
        }
        String fileName = args[0].toString();
        String data = args[1].toString();
        Files.write( new File(fileName).toPath(), data.getBytes()) ;
    }
    public static String read(Object... args) throws Exception{
        if ( args.length == 0 ){
            return System.console().readLine();
        }
        return readToEnd(args[0].toString());
    }

    public static ArrayList<String> readLines(Object... args) throws Exception{
        ArrayList<String> lines = null;
        if ( args.length == 0 ){
            lines = new ArrayList<String>();
            while(true){
                String l = System.console().readLine();
                if ( l ==  null ){
                    break;
                }
                lines.add(l);
            }
            return lines;
        }
        lines = (ArrayList)Files.readAllLines(new File(args[0].toString()).toPath());
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
        if ( args.length == 0 ){
            return 0.0;
        }
        try {
            if ( args[0] instanceof Double ) {
                return (Double)args[0];
            }
            if ( args[0] instanceof Float){
                return ((Float)args[0]).doubleValue();
            }
            Object objectValue = args[0];
            Double val = Double.valueOf(objectValue.toString());
            return val;
        } catch (Exception e) {
            if ( args.length > 1 ){
                return castDouble(args[1]);
            }
            return null;
        }
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

    public static DateTime castTime(Object ... args) {
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

    public static Date castDate(Object ... args) {
        if (args.length == 0) {
            return new Date();
        }
        if (args[0] instanceof Date ) {
            return (Date) args[0];
        }
        if (args[0] instanceof DateTime) {
            return ((DateTime) args[0]).toDate();
        }

        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
        try {
            if (args.length > 1) {
                dateTimeFormatter = new SimpleDateFormat(args[1].toString());
            }
            return dateTimeFormatter.parse(args[0].toString());
        } catch (Exception e) {
        }
        return null;
    }

    public static Boolean castBoolean(Object... args) {
        if ( args.length == 0 ){
            return Boolean.FALSE;
        }

        if (args[0] instanceof Boolean) {
            return (Boolean)args[0];
        }

        if (args[0] instanceof String) {
            String litValue = (String)args[0];

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
        if ( args.length > 1 ) {
            return castBoolean(args[1]);
        }
        return null;
    }

    public static String castString(Object... args) {
        if (args.length == 0) {
            return null;
        }
        if ( args[0] instanceof AnonymousParam){
            AnonymousParam anon = (AnonymousParam)args[0];
            args = shiftArrayLeft(args, 1);
            anon.setIterationContext(args[0],-1);
            Object ret = anon.execute();
            anon.removeIterationContext();
            args[0] = ret; //set it up
            return castString(args);
        }

        if (args[0] instanceof String) {
            return (String) args[0];
        }
        if (args[0] instanceof DateTime) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
            if (args.length > 1 && args[1]!=null) {
                dateTimeFormatter = DateTimeFormat.forPattern(args[1].toString());
            }
            try {
                return dateTimeFormatter.print((DateTime) args[0]);
            } catch (Exception e) {
            }
        } else if (args[0] instanceof Date) {
            SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
            if (args.length > 1 && args[1]!=null) {
                dateTimeFormatter = new SimpleDateFormat(args[1].toString());
            }
            try {
                return dateTimeFormatter.format(args[0]);
            } catch (Exception e) {

            }
        }
        Class c = args[0].getClass();
        if (List.class.isAssignableFrom(c) || c.isArray()) {
            List l = from(args[0]);
            StringBuffer buf = new StringBuffer();
            String sep = ",";
            if ( args.length> 1 && args[1] != null ){
                sep = args[1].toString();
            }
            for(Object o : l){
                buf.append(o).append(sep);
            }
            String ret = buf.substring(0, buf.lastIndexOf(sep));
            return ret;
        }
        return args[0].toString();
    }

    public static ArrayList makeLiteralList(Object... argv) {
        ArrayList list = new ArrayList();
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
     * @param object
     * @return
     */
    public static ArrayList from(Object object) {
        if (object == null) {
            return null;
        }
        ArrayList list = new ArrayList();
        if (object instanceof Collection) {
            Collection l = (Collection) object;
            list.addAll(l);
        } else if (object.getClass().isArray()) {
            Object[] array = getArray(object);
            List l = Arrays.asList(array);
            list.addAll(l);
        } else {
            list.add(object);
        }
        return list;
    }

    public static HashMap<String,Method> methodHashMap = new HashMap<>();

    public static Object callAnonMethod(String name,Object val){
        if( !methodHashMap.containsKey(name)){
            String[] arr = name.split("__");
            String className = arr[0];
            try {
                Class c = Class.forName(className);
                Method[] methods = c.getDeclaredMethods();
                for ( Method m : methods ) {
                    if ( m.getName().equals(name)) {
                        methodHashMap.put(name, m);
                        break;
                    }
                }
            }catch (Exception e){
                System.err.println(e);
            }
        }
        Method m = methodHashMap.get(name);
        Object o = null;
        try {
            o = m.invoke(null, val);
        }catch (Exception e){
            System.err.println(e);
        }
        return o;
    }

    /**
     * <pre>
     *     Very important list combine routine
     *     Given ( arg_0 , arg_1 , arg_2 , ... )
     *     It would check if arg_xxx is a list type, then would add all element of arg_xxx to the return list.
     *
     * </pre>
     *
     * @param args
     * @return
     */
    public static ArrayList combine(Object... args) {
        AnonymousParam anon = null;
        ArrayList list = new ArrayList();
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
            ArrayList l = new ArrayList();
            int i = 0 ;
            for (Object o : list) {
                anon.setIterationContext( o,i);
                Object ret = anon.execute();
                l.add(ret);
                i++;
            }
            list = l;
            anon.removeIterationContext();
        }

        return list;
    }

    public static ArrayList filter(Object... args) {
        AnonymousParam anon = null;
        ArrayList list = new ArrayList();
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
            ArrayList l = new ArrayList();
            int i = 0 ;
            for (Object o : list) {
                anon.setIterationContext(o,i);
                Object ret = anon.execute();
                if ( castBoolean(ret,false) ){
                    //should add _ITEM_ 's value, if anyone modified it
                    l.add(anon.interpreter.getContext().get(_ITEM_));
                }
                i++;
            }
            list = l;
            anon.removeIterationContext();
        }
        return list;
    }

    public static Iterator range(Object... args) throws Exception {

        if (args.length == 0) {
            return new RangeIterator();
        }

        long end = 42;
        long start = 1;
        long space = 1;

        if (args.length > 0) {
            end = Long.valueOf(args[0].toString());
            if (args.length > 1) {
                start = Long.valueOf(args[1].toString());
                if (args.length > 2) {
                    space = Long.valueOf(args[2].toString());
                }
                if (space <= 0) {
                    space = 1;
                }
            }
        }

        return new RangeIterator(end,start,space);
    }

    public static Object[] shiftArrayLeft(Object[] args, int shift) {
        Object[] array = new Object[args.length - shift];
        for (int i = 0; i < array.length; i++) {
            array[i] = args[i + shift];
        }
        return array;
    }

    public static ListSet set(Object... args) {
        ArrayList list = combine(args);
        ListSet set = new ListSet();
        set.addAll(list);
        return set;
    }

    public static Boolean within(Object[] args) throws Exception {
        ArrayList list = combine(args);

        if (list.size() < 3) {
            throw new Exception("At least 3 args are needed for within ");
        }
        XNumber item = new XNumber(list.get(0));
        XNumber minItem = new XNumber(list.get(1));
        XNumber maxItem =  minItem;
        for (int i = 2; i < list.size(); i++) {
            XNumber listItem = new XNumber(list.get(i));
            if (listItem.compareTo(minItem) < 0) {
                minItem = listItem;
                continue;
            }
            if (listItem.compareTo(maxItem) > 0) {
                maxItem = listItem;
            }
        }
        try {
            boolean result = item.compareTo(minItem) >= 0 && item.compareTo(maxItem) <= 0;
            return result;
        } catch (Exception e) {
            System.err.printf("The item you passed is not comparable with the items in the list. Check types!\n");
            System.err.printf("Item Type : %s\n", item.getClass().getName());
            System.err.printf("List of Type : %s\n", minItem.getClass().getName());
        }
        return false;
    }

    public static HashMap makeDict(Object[] args) throws Exception {
        HashMap map = new HashMap();

        if (args.length == 1 &&
                Map.class.isAssignableFrom(args[0].getClass())) {
            map.putAll((Map) args[0]);
            return map;
        }
        if (args.length != 2) {
            return map;
        }
        ArrayList keyList = from(args[0]);
        ArrayList valueList = from(args[1]);
        if (keyList.size() != valueList.size()) {
            throw new Exception("Key and Value Arrays/Lists are not of same length!");
        }

        for (int i = 0; i < keyList.size(); i++) {
            map.put(keyList.get(i), valueList.get(i));
        }
        return map;

    }

    public static Object sqlmath(Object[] argv) {
        Double[] math = new Double[]{null, null, null};
        ArrayList list = combine(argv);
        if (list.size() > 0) {
            int index = 0;
            Object obj = list.get(index);

            math[0] = Double.valueOf(obj.toString());
            math[1] = math[0];
            math[2] = math[0];
            for (index = 1; index < list.size(); index++) {
                obj = list.get(index);

                Double value = Double.valueOf(obj.toString());
                if (value < math[0]) {
                    math[0] = value;  // MIN
                }
                if (value > math[1]) {
                    math[1] = value;   // MAX
                }
                math[2] = math[2] + value; // SUM
            }
        }
        return math;
    }

    public static void bye(Object...args){
        String msg = "BYE received, we will exit this script..." ;
        Integer exitStatus = null;
        if ( args.length > 1 && args[0] instanceof Number ){
            exitStatus = ((Number)args[0]).intValue();
        }
        if ( exitStatus != null ) {
            System.exit(exitStatus);
        }
        throw new JexlException.Return(new ASTReturnStatement(ParserConstants.RETURN), msg, args);
    }

    public static boolean test(Object... args){
        if ( args.length == 0  ){
            return true ;
        }
        boolean ret = castBoolean(args[0],false);
        // log it - later problem - not now
        return ret ;
    }

    public static Object interceptCastingCall(String methodName, Object[] argv, Boolean[] success) throws Exception {

        if ( success != null ) {
            success[0] = true;
        }
        switch (methodName){
            case INT:
                return castInteger(argv);
            case LONG:
                return castLong(argv);
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
            case JOIN:
                return SetOperations.join_c(argv);
            case ARRAY:
                return argv;
            case SET:
                return set(argv);
            case DICTIONARY:
                return makeDict(argv);
            case RANGE:
                return range(argv);
            case ARRAY_FROM_LIST:
                return getArray(makeLiteralList(argv));
            case WITHIN:
                return within(argv);
            case SQLMATH:
                return sqlmath(argv);
            case READ :
                return read(argv);
            case READ_LINES:
                return readLines(argv);
            case WRITE:
                writeFile(argv);
                break;
            case BYE:
                bye(argv);
                break;
            case TEST:
                return  test(argv);
            case LOAD_PATH:
                return ReflectionUtility.load_path(argv);
            case MULTI_SET1:
            case MULTI_SET2:
            case MULTI_SET3:
                return SetOperations.multiset(argv);
            default:
                if ( success != null ) {
                    success[0] = false;
                }
                break;
        }
        return methodName;
    }

    private static Object sublist(Object... args) {
        if ( args.length == 0 ){
            return null;
        }
        List l = from(args[0]);
        int start = 0 ;
        int end = l.size()-1;
        if ( args.length > 1 ){
            start = castInteger(args[1],start);
            if ( args.length > 2 ){
                end = castInteger(args[2],end);
            }
        }
        ArrayList r = new ArrayList();
        for(int i = start; i<=end;i++){
            r.add(l.get(i));
        }
        return r;
    }

    public static class XNumber extends Number implements Comparable {
        Number number;

        public XNumber() {
            super();
            number = 0;
        }

        public XNumber(Object number) {
            super();
            if (number instanceof Number) {
                this.number = (Number) number;
            } else if (number instanceof Date) {
                this.number = ((Date) number).getTime();
            } else if (number instanceof DateTime) {
                this.number = ((DateTime) number).toDate().getTime();
            } else if ( number instanceof Boolean){
                this.number = (Boolean)number?1:0;
            }
            else {
                throw new ClassCastException("Can not convert to Number!");
            }
        }


        @Override
        public int intValue() {
            return number.intValue();
        }

        @Override
        public long longValue() {
            return number.longValue();
        }

        @Override
        public float floatValue() {
            return number.floatValue();
        }

        @Override
        public double doubleValue() {
            return number.doubleValue();
        }

        @Override
        public byte byteValue() {
            return number.byteValue();
        }

        @Override
        public short shortValue() {
            return number.shortValue();
        }

        @Override
        public int compareTo(Object o) {

            try {
                double that = new XNumber(o).doubleValue();
                double thisValue = doubleValue();
                if (that > thisValue) {
                    return -1;
                } else if (that < thisValue) {
                    return 1;
                }
                return 0;

            } catch (Exception e) {
            }
            return 0;
        }
    }
}
