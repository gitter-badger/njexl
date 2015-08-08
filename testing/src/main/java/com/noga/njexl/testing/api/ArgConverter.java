/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.testing.api;
import com.noga.njexl.lang.internal.logging.Log;
import com.noga.njexl.lang.internal.logging.LogFactory;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.ProviderFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * Standard Spring/Hibernate/xBatis magic
 * of converting string to object - also known as DeSerialization
 * This does the conversion of method parameters from relational strings
 * </pre>
 */
public class ArgConverter {

    private static Log logger = LogFactory.getLog( ArgConverter.class );

    public static String TLN(Class clazz){
        return clazz.getName().toLowerCase();
    }

    public static final String OBJECT = TLN ( Object.class) ;
    public static final String CLASS = TLN ( Class.class) ;
    public static final String STRING = TLN(String.class);
    public static final String DATE = TLN ( Date.class);
    // INT TYPES
    public static final String LONG_B = TLN (Long.class);
    public static final String LONG = "long";

    public static final String INT = "int";
    public static final String INTEGER = TLN(Integer.class);
    public static final String BIG_INTEGER = TLN(BigInteger.class);
    public static final String SHORT_B = TLN(Short.class);
    public static final String SHORT = "short";

    public static final String CHARACTER = TLN( Character.class) ;
    public static final String CHAR = "char";
    public static final String BYTE = "byte";
    public static final String BYTE_B = TLN(Byte.class);

    // The Number types
    public static final String DOUBLE = "double";
    public static final String DOUBLE_B = TLN(Double.class);

    public static final String FLOAT = "float";
    public static final String FLOAT_B = TLN(Float.class);
    public static final String BIG_DECIMAL = TLN(BigDecimal.class);

    // The Logic Type
    public static final String BOOLEAN = "boolean";
    public static final String BOOLEAN_B = TLN(Boolean.class);

    // The list Type
    public static final String LIST = "list";
    public static final String ARRAY = "[L";
    // Other Strings
    // This is the signature to pass NULL
    public static final String NULL_OBJECT= "#null#";
    // This is the signature to pass empty set, hash, list, array or String
    public static final String EMPTY_CONTAINER = "#empty#";
    public static final String SPLIT_STRING = ":";
    public static final String LIST_SEP_STRING = ",";
    public static final String HASH_SEP_STRING = "=>";

    public static final String CALL_CONSTRUCTOR = "#";

    public static final String LIST_REDIRECT = "@";

    public static final SimpleDateFormat  DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy" ) ;

    /**
     * Checks if the class is a primitive type
     *
     * @param c The class type
     * @return Is the class 'c' primitive?
     */
    public static boolean IsPrimitive(Class<?> c) {

        String name = c.getName();
        name = name.toLowerCase();
        // Enum is a primitive type
        if (__IsPrimitive(name) || c.isEnum()) {
            return true;
        }
        return c.isPrimitive();
    }

    /**
     * Checks if the full name of the class belongs to a primitive type
     *
     * @param name Fully qualified Name of the class
     * @return boolean yes/no answer
     */
    private static boolean __IsPrimitive(String name) {
        if (name.equals(STRING) || name.equals(OBJECT) || name.equals(DATE)
        /* Now the Un-Boxed types */
                || name.equals(INT) || name.equals(LONG) || name.equals(BYTE)
                || name.equals(SHORT) || name.equals(CHAR)
                || name.equals(FLOAT) || name.equals(DOUBLE)
                || name.equals(BOOLEAN)
                /* The Boxed Types */
                || name.equals(INTEGER) || name.equals(BIG_DECIMAL)
                || name.equals(LONG_B) || name.equals(CHARACTER)
                || name.equals(DOUBLE_B)
                || name.equals(FLOAT_B) || name.equals(BYTE_B)
                || name.equals(SHORT_B) || name.equals(BIG_INTEGER)
                || name.equals(BOOLEAN_B) || name.equals(CLASS)) {
            return true;
        }
        return false;
    }

    /**
     * <pre>
     * Checks if the class type is a list type or not. Basically tells you if it
     * is a List or an array type
     * </pre>
     *
     * @param c The class type
     * @return A boolean yes/no
     */
    public static boolean IsList(Class<?> c) {
        return List.class.isAssignableFrom( c ) ;
    }

    /**
     * <pre>
     * Checks if the class type is an Array type or not. Basically tells you if it
     * is an array type
     * </pre>
     *
     * @param c The class type
     * @return A boolean yes/no
     */
    public static boolean IsArray(Class<?> c) {
        if (c.isArray()) {
            return true;
        }

        return false;
    }

    /**
     * <pre>
     *     Is the class c belongs to :-
     *     [1] Map
     *     [2] Set
     *     [3] Dictionary
     *
     * </pre>
     *
     * @param c a class to be tested
     * @return then return true, else return false
     */
    public static boolean IsHash(Class<?> c) {
        if (Map.class.isAssignableFrom(c)) {
            return true;
        }
        if (Set.class.isAssignableFrom(c)) {
            return true;
        }
        if (Dictionary.class.isAssignableFrom(c)) {
            return true;
        }

        return false;
    }

    /**
     * A field map for fields of classes
     */
    public static ConcurrentHashMap<Class,HashMap<String,Field>> cachedFieldMap = new ConcurrentHashMap<>();

    /**
     * Gets the field given class and field name
     * @param clazz the class
     * @param fieldName the field
     * @return the reflective field, if exists
     */
    public static Field getField(Class clazz, String fieldName) {
        HashMap<String,Field> map = cachedFieldMap.get(clazz);
        if ( map == null ){
            map = new HashMap<>();
            Field[] fields = clazz.getDeclaredFields();
            for( Field f : fields ){
                f.setAccessible(true);
                map.put(f.getName(),f);
            }
            cachedFieldMap.put(clazz,map);
        }
        Field f =  map.get(fieldName);
        if ( f != null){ return f ; }
        // get the super too
        f = getField( clazz.getSuperclass(),fieldName);
        return f ;
    }

    Method method;

    public Method method(){ return  method ; }

    Type[]  parameterTypes;

    Class[] parameterClasses;

    protected String formattedMethodSignature = "" ;

    /**
     * Generates method signature given a method
     * @param method the method
     * @return the compilable method signature for that method
     */
    public static String getMethodSignature(Method method){
        String[] cols = method.getGenericReturnType().toString().split(" ");
        String retType = cols[cols.length - 1];
        String methodName = method.getName();
        Type[] typed_params =  method.getGenericParameterTypes();

        StringBuffer output = new StringBuffer( String.format("%s#%s(", retType,
                methodName) );
        for (int i = 0; i < typed_params.length; i++) {

            cols = typed_params[i].toString().split(" ");
            output.append( String.format("%s", cols[cols.length - 1]) );
            output.append( "," );

        }
        String ret = output.substring( 0, output.length() - 1);
        ret += ")" ;
        logger.info(ret);
        System.out.println(ret);
        return ret;
    }

    /**
     * Prints the method formatted
     */
    private void printMethod() {

        String output = getMethodSignature( this.method );
        formattedMethodSignature = output ;
        logger.debug("Matched Method is :: " + output);
        System.out.println("=========matched method==========");
        System.out.println(output);
        System.out.println("=================================");
    }

    DataSource dataSource ;

    String dataTableName;

    /**
     * Gets the associated data source
     * @return the data source
     */
    public DataSource dataSource(){ return  dataSource ; }

    /**
     * Create a call container from the given data source
     * @param row the row of the data table
     * @return a call container
     */
    public CallContainer container(int row){
        String[] values = dataSource.tables.get(dataTableName).row(row);
        if ( parameterTypes.length > values.length ){
            return null;
        }
        CallContainer container = new CallContainer();
        container.parameters = new Object[ parameterTypes.length ] ;
        String[] headers = dataSource.tables.get(dataTableName).row(0);

        for ( int i = 0 ; i < container.parameters.length; i++ ){
            try {
                container.parameters[i] = object(values[i], parameterClasses[i], parameterTypes[i], headers[i]);
            }catch (Exception e){
                logger.error(String.format("Error converting parameter %d : %s", i, parameterTypes[i]), e);
            }
        }

        container.method = method ;
        container.rowId = row ;
        container.dataTable = dataSource.tables.get(dataTableName) ;

        return container;
    }

    /**
     * Gets all the containers from the data table
     * @param removeDisable if true, disabled items won't be added
     * @return an array of @{CallContainer} objects
     */
    public CallContainer[] allContainers(boolean removeDisable){
        int size = dataSource.tables.get(dataTableName).length();
        ArrayList<CallContainer> containers = new ArrayList<>();
        for ( int i = 1; i < size; i++ ){
            CallContainer cc = container(i);
            if ( removeDisable && cc.disabled() ){ continue; }
            containers.add(cc);
        }
        CallContainer[] ccArr = new CallContainer[containers.size()];
        containers.toArray( ccArr );
        return ccArr ;
    }

    /**
     * Gets all the containers from the data table
     * removing the disabled items
     * @return an array of @{CallContainer} objects
     */
    public CallContainer[] allContainers(){
        return allContainers(true);
    }

    /**
     * Creates an object from serialized relational form
     * @param value the string value
     * @param clazz the object's class
     * @param type the object's type
     * @param header header ( metadata ) information
     * @return instance of an object if it could
     * @throws Exception if it can not create one
     */
    public Object object(String value, Class clazz, Type type, String header) throws Exception {
        if ( value.equalsIgnoreCase(NULL_OBJECT) ){
            return null;
        }
        Object object ;
        if ( IsPrimitive(clazz) ){
            object =  primitive(value, clazz, header);
        }
        else if ( IsList(clazz) ){

            if ( value.equalsIgnoreCase( EMPTY_CONTAINER ) ){
                return new ArrayList<>();
            }
            object = list(value, type, header);
        }
        else if ( IsHash(clazz)){
            if ( value.equalsIgnoreCase( EMPTY_CONTAINER ) ){
                if ( Dictionary.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
                    return new HashMap<>();
                }
                return new HashSet<>();
            }

            object = map(value, clazz, type, header);
        }
        else {
            object = complex(value, clazz);
        }
        return object ;
    }

    private Object primitive(String value, Class clazz, String header) throws Exception {
        if ( clazz.equals( String.class ) ){
            if ( value.equalsIgnoreCase( EMPTY_CONTAINER ) ){
                return "";
            }
            return value ;
        }
        if ( clazz.equals( Date.class) ){
            String[] arr = header.split(SPLIT_STRING);
            if ( arr.length > 1 ) {
                SimpleDateFormat sdf = new SimpleDateFormat(arr[1].trim()) ;
                return sdf.parse( value );
            }
            return DATE_FORMAT.parse(value);
        }
        if ( clazz.isEnum() ){
            return Enum.valueOf( clazz, value );
        }
        if ( Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)){
            return Boolean.parseBoolean(value);
        }
        if ( Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz) ){
            return Byte.parseByte(value);
        }
        if ( Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)){
            return Short.parseShort(value);
        }
        if ( Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)){
            return Integer.parseInt(value);
        }
        if ( Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)){
            return Float.parseFloat(value);
        }
        if ( Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)){
            return Double.parseDouble(value);
        }
        if ( BigInteger.class.isAssignableFrom(clazz) ){
            return new BigInteger(value,10);
        }
        if ( BigDecimal.class.isAssignableFrom(clazz) ){
            return new BigDecimal(value);
        }
        logger.error(String.format("Why %s is in here?",clazz));
        return null;
    }

    /**
     * Gets constructor of a class from metadata info
     * @param clazz the class
     * @param headers the metadata information
     * @return a matching constructor object
     */
    public Constructor constructor(Class clazz, String[] headers){
        Constructor[] constructors = clazz.getConstructors();
        Constructor c = null;
        for ( int i = 0 ; i < constructors.length; i++){
            if ( constructors[i].getParameterCount() == headers.length ){
                // possible return, but try checking the
                c = constructors[i];
                boolean match = true;
                Parameter[] params = c.getParameters();
                for ( int j = 0 ; j < params.length; j++){
                    match = params[j].getName().equals(headers[j]);
                    if ( !match ){
                        break;
                    }
                }
                if ( match ){
                    return c;
                }
            }
        }
        return c;
    }

    private Object complex(String value, Class clazz) {
        String[] arr = value.split(SPLIT_STRING);
        Object instance = null;
        String tableName = arr[0].trim();
        int rowNum = Integer.parseInt(arr[1].trim());
        boolean constructor = false ;
        if ( tableName.startsWith(CALL_CONSTRUCTOR) ){
            tableName = tableName.substring(1);
            //call constructor
            constructor = true ;
        }

        if ( constructor ){
            String[] headers = dataSource.tables.get(tableName).row(0);
            String[] values = dataSource.tables.get(tableName).row(rowNum);
            if ( values == null ){
                logger.error("There is no value in the table with that row 0 based!");
                return null;
            }

            Object[] params = new Object[values.length] ;
            boolean findConstructor = false ;
            for ( int i = 0 ; i < headers.length ;i++ ){
                try {
                    arr = headers[i].split(SPLIT_STRING) ;
                    if ( arr.length > 1 ) {
                        String paramClassName = arr[1].trim();
                        Class paramType = Class.forName(paramClassName);
                        params[i] = object(values[i], paramType, paramType, headers[i]);
                    }else{
                        findConstructor = true;
                        break;
                    }

                }catch (Exception e){
                    logger.error(
                            String.format("Error setting up constructor parameter value : %s : %s", clazz, headers[i]),
                            e);
                }
            }
            // am here now
            if ( findConstructor ){
                Constructor c = constructor(clazz, headers);
                if ( c == null){
                    logger.error( String.format("Error creating object through constructor by intelligence: %s", clazz));
                    return null;
                }
                Class[] parameters = c.getParameterTypes();
                Type[] types = c.getGenericParameterTypes();
                for ( int i = 0 ; i < parameters.length ;i++ ){
                    try {
                        params[i] = object(values[i], parameters[i], types[i], headers[i]);

                    }catch (Exception e){
                        logger.error(
                                String.format("Error setting up constructor parameter value : %s : %s", clazz, headers[i]),
                                e);
                    }
                }
                try {
                    instance = c.newInstance(params);
                } catch (Exception e) {
                    logger.error( String.format("Error creating object through constructor by intelligence: %s", clazz));
                }
            }

            else{
                try {
                    instance = Utils.createInstance(clazz.getName(), params);
                } catch (Exception e) {
                    logger.error(
                            String.format("Error creating object through constructor by parameter type : %s", clazz),e);
                }
            }
        }
        else{
            instance = Utils.createInstance(clazz.getName());
            String[] headers = dataSource.tables.get(tableName).row(0);
            String[] values = dataSource.tables.get(tableName).row(rowNum);
            for ( int i = 0 ; i < headers.length ;i++ ){
                logger.debug( String.format("Working field with : [%s]",headers[i]));
                arr = headers[i].split(SPLIT_STRING) ;
                String fieldName = arr[0].trim();

                Field  field = getField(clazz,fieldName);
                if ( field == null ){
                    logger.error(
                            String.format("In the column no [%d] : A field with name [%s] does not exist in the class [%s]!",
                            i, fieldName,clazz.getName())
                    );
                    continue;
                }
                try {
                    Object fieldValue = object(values[i], field.getType(), field.getGenericType(), headers[i]);
                    field.set( instance, fieldValue);
                }catch (Exception e){
                    logger.error(String.format("Error setting up field value : %s : %s", clazz, fieldName),e);
                }
            }
        }

        return instance;
    }

    private Object map(String value, Class clazz, Type type, String header) {
        if ( Map.class.isAssignableFrom( clazz) || Dictionary.class.isAssignableFrom(clazz) ){
            return new HashMap();
        }
        if ( Set.class.isAssignableFrom(clazz)){
            return new HashSet<>();
        }
        return null;
    }

    /**
     * For container types, tries to get inner type
     * @param type the type
     * @return inner type, if possible
     */
    public static String getInnerType(Type type){
        String name = type.getTypeName();
        int firstIndex = name.indexOf('<');
        int lastIndex = name.lastIndexOf('>');
        name = name.substring(firstIndex+1,lastIndex);
        return name;
    }

    private Object list(String value, Type type, String header) {
        ArrayList list = new ArrayList();
        String innerName = getInnerType(type);
        try{
            String[] values;
            Class innerClass = Class.forName(innerName);
            if ( value.startsWith( LIST_REDIRECT ) ){
                value = value.substring(1);
                String[] arr = value.split(SPLIT_STRING);
                String tableName = arr[0].trim();
                int rowNum = Integer.parseInt(arr[1].trim());
                values = dataSource.tables.get(tableName).row(rowNum);
            }else{
                values = value.split(LIST_SEP_STRING);
            }

            for ( int i = 0 ; i < values.length;i++ ){
                Object item = object(values[i], innerClass, innerClass, "");
                list.add(item);
            }

        }catch (Exception e){
            logger.error(String.format("Failing to find inner class : %s", innerName),e);
        }
        return list;
    }

    /**
     * Given metadata information creates one instance
     * @param methodRunInformation metadata information
     */
    public ArgConverter(Annotations.MethodRunInformation methodRunInformation){
        method = methodRunInformation.method ;
        parameterTypes = method.getGenericParameterTypes();
        parameterClasses = method.getParameterTypes();
        String dsLoc = methodRunInformation.base + "/" + methodRunInformation.nApi.dataSource();
        dataSource = ProviderFactory.dataSource(dsLoc);
        dataTableName = methodRunInformation.nApi.dataTable();
    }
}