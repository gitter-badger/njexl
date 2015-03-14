package org.apache.commons.jexl2.extension;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.Script;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public static final String BIGDECIMAL = "DEC";

    /**
     * ******* The Utility Calls  *********
     */

    public static final String LIST = "list";
    public static final String LITERAL_LIST = "listLiteral";
    public static final String ARRAY = "array";
    public static final String ARRAY_FROM_LIST = "arrayFromList";
    public static final String SET = "set";
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

    public static final String _ITEM_ = "$_";

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

    public static Double castDouble(Object objectValue) {
        try {
            Double val = Double.valueOf(objectValue.toString());
            return val;
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer castInteger(Object objectValue) {
        Double doubleValue = castDouble(objectValue);
        if (doubleValue != null) {
            int i = (int) Math.floor(doubleValue);
            return new Integer(i);
        }
        return null;

    }

    public static Long castLong(Object objectValue) {
        Double doubleValue = castDouble(objectValue);
        if (doubleValue != null) {
            long l = (long) Math.floor(doubleValue);
            return new Long(l);
        }
        return null;

    }

    public static DateTime castTime(Object objectValue, Object formatValue) {
        if (objectValue == null) {
            return new DateTime();
        }
        if (objectValue instanceof Date) {
            return new DateTime(objectValue);
        }
        if (objectValue instanceof DateTime) {
            return (DateTime) objectValue;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
        if (formatValue != null) {
            dateTimeFormatter = DateTimeFormat.forPattern(formatValue.toString());
        }
        try {
            return DateTime.parse(objectValue.toString(), dateTimeFormatter);
        } catch (Exception e) {

        }
        return null;
    }

    public static Date castDate(Object objectValue, Object formatValue) {
        if (objectValue == null) {
            return new Date();
        }
        if (objectValue instanceof Date) {
            return (Date) objectValue;
        }
        if (objectValue instanceof DateTime) {
            return ((DateTime) objectValue).toDate();
        }

        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
        if (formatValue != null) {
            dateTimeFormatter = new SimpleDateFormat(formatValue.toString());
        }
        try {
            return dateTimeFormatter.parse(objectValue.toString());
        } catch (Exception e) {

        }
        return null;
    }

    public static Object castBoolean(Object objectValue, Object defaultValue) {


        if (defaultValue != null) {
            defaultValue = Boolean.valueOf(defaultValue.toString());

        }
        if (objectValue == null) {
            return defaultValue;
        }

        if (objectValue instanceof Boolean) {
            return objectValue;
        }

        if (objectValue instanceof String) {
            String litValue = objectValue.toString();

            litValue = litValue.toLowerCase();
            if (litValue.equals("true")) {
                return Boolean.TRUE;
            }
            if (litValue.equals("false")) {
                return Boolean.FALSE;
            }
        }
        Double d = castDouble(objectValue);
        if (d != null) {
            return new Boolean(d != 0);
        }
        return defaultValue;
    }

    public static String castString(Object objectValue, Object formatValue) {
        if (objectValue == null) {
            return null;
        }
        if (objectValue instanceof String) {
            return (String) objectValue;
        }
        if (objectValue instanceof DateTime) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
            if (formatValue != null) {
                dateTimeFormatter = DateTimeFormat.forPattern(formatValue.toString());
            }
            try {
                return dateTimeFormatter.print((DateTime) objectValue);
            } catch (Exception e) {

            }
        } else if (objectValue instanceof Date) {
            SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
            if (formatValue != null) {
                dateTimeFormatter = new SimpleDateFormat(formatValue.toString());
            }
            try {
                return dateTimeFormatter.format(objectValue);
            } catch (Exception e) {

            }
        }
        Class c = objectValue.getClass();
        if (List.class.isAssignableFrom(c) || c.isArray()) {
            List l = from(objectValue);

        }
        return objectValue.toString();
    }

    public static ArrayList makeLiteralList(Object[] argv) {
        ArrayList list = new ArrayList();
        if (argv != null) {
            for (int i = 0; i < argv.length; i++) {
                list.add(argv[i]);
            }
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
    public static ArrayList combine(Object[] args) {
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
            for (Object o : list) {
                anon.jexlContext.set(_ITEM_, o);
                Object ret = anon.block.jjtAccept(anon.pv, null);
                l.add(ret);
            }
            list = l;
            anon.jexlContext.set(_ITEM_, null);
        }
        return list;
    }

    public static ArrayList<Integer> range(Object[] args) throws Exception {
        if (args == null) {
            return range(new Object[]{42});
        }
        if (args.length == 0) {
            return range(new Object[]{42});
        }

        int end = 42;
        int start = 1;
        int space = 1;

        if (args.length > 0) {
            end = Integer.valueOf(args[args.length - 1].toString());
        }
        if (args.length > 1) {
            start = Integer.valueOf(args[0].toString());
        }
        if (args.length > 2) {
            space = Integer.valueOf(args[args.length - 2].toString());
        }
        if (space <= 0) {
            space = 1;
        }
        ArrayList<Integer> r = new ArrayList<Integer>();
        for (int i = start; i <= end; i += space) {
            r.add(i);
        }

        return r;
    }

    public static Object[] shiftArrayLeft(Object[] args, int shift) {
        Object[] array = new Object[args.length - shift];
        for (int i = 0; i < array.length; i++) {
            array[i] = args[i + shift];
        }
        return array;
    }

    public static HashSet set(Object[] args) {

        ArrayList list = combine(args);
        HashSet set = new HashSet();

        for (Object obj : list) {
            set.add(obj);
        }

        return set;
    }

    public static Boolean within(Object[] args) throws Exception {
        AnonymousParam anon = null;
        if (args.length > 1) {
            if (args[0] instanceof AnonymousParam) {
                anon = (AnonymousParam) args[0];
                args = shiftArrayLeft(args, 1);
            }
        }

        if (args.length != 2) {
            throw new Exception("At least 2 args are needed for within ");
        }

        Comparable item;
        try {
            item = new XNumber(args[0]);
        } catch (ClassCastException e) {
            item = (Comparable) args[0];
        }

        ArrayList list = combine(shiftArrayLeft(args, 1));
        if (list.size() == 0) {
            return false;
        }
        Comparable minItem = (Comparable) list.get(0);
        Comparable maxItem = (Comparable) list.get(0);
        for (int i = 1; i < list.size(); i++) {
            Comparable listItem = (Comparable) list.get(i);
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

    private static Object sqlmath(Object[] argv) {
        AnonymousParam anon = null;
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

    public static Object interceptCastingCall(String methodName, Object[] argv, Boolean[] success) throws Exception {

        success[0] = true;

        if (methodName.equals(INT)) {

            if (argv.length == 0) {
                return null;
            }
            return castInteger(argv[0]);
        }
        if (methodName.equals(LONG)) {
            if (argv.length == 0) {
                return null;
            }
            return castLong(argv[0]);
        }
        if (methodName.equals(DOUBLE)) {
            if (argv.length == 0) {
                return null;
            }
            return castDouble(argv[0]);
        }
        if (methodName.equals(BIGDECIMAL)) {
            return castBigDecimal(argv);
        }
        if (methodName.equals(BIGINT)) {
            return castBigInteger(argv);
        }
        if (methodName.equals(BOOL)) {
            if (argv.length == 0) {
                return null;
            }
            if (argv.length == 1)
                return castBoolean(argv[0], null);
            return castBoolean(argv[0], argv[1]);
        }
        if (methodName.equals(STRING)) {
            if (argv.length == 0) {
                return null;
            }
            if (argv.length == 1)
                return castString(argv[0], null);
            return castString(argv[0], argv[1]);
        }
        if (methodName.equals(TIME)) {
            if (argv.length == 0) {
                return new DateTime();
            }
            if (argv.length == 1)
                return castTime(argv[0], null);
            return castTime(argv[0], argv[1]);
        }
        if (methodName.equals(DATE)) {
            if (argv.length == 0) {
                return new Date();
            }
            if (argv.length == 1)
                return castDate(argv[0], null);
            return castDate(argv[0], argv[1]);
        }
        if (methodName.equals(LITERAL_LIST)) {
            return makeLiteralList(argv);
        }
        if (methodName.equals(LIST)) {
            return combine(argv);
        }
        if (methodName.equals(ARRAY)) {
            return argv;
        }
        if (methodName.equals(SET)) {
            return set(argv);
        }
        if (methodName.equals(DICTIONARY)) {
            return makeDict(argv);
        }
        if (methodName.equals(RANGE)) {
            return range(argv);
        }
        if (methodName.equals(ARRAY_FROM_LIST)) {
            return getArray(makeLiteralList(argv));
        }
        if (methodName.equals(WITHIN)) {
            return within(argv);
        }
        if (methodName.equals(SQLMATH)) {
            return sqlmath(argv);
        }

        if (methodName.equals(READ)) {
            return read(argv);
        }
        if (methodName.equals(READ_LINES)) {
            return readLines(argv);
        }
        if (methodName.equals(WRITE)) {
            writeFile(argv);
        }
        success[0] = false;

        return methodName;
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
            } else {
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
