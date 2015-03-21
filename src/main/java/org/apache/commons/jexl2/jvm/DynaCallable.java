package org.apache.commons.jexl2.jvm;

import org.apache.commons.jexl2.extension.TypeUtility;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by noga on 17/03/15.
 */
public interface DynaCallable {

    public static final String SCRIPT_ENTRY = "__script_body__";

    Object __call__(Object[] args);

    Object __script_body__(Object[] args) throws Exception;

    HashMap<String,Object>  __context__() ;

    public static class TemplateClass implements DynaCallable {

        public static boolean empty(Object o) {
            if (o == null) {
                return true;
            }
            if (o instanceof Collection) {
                return ((Collection) o).isEmpty();
            }
            if (o.getClass().isArray()) {
                return (Array.getLength(o) == 0);
            }
            return false;
        }

        public static Integer size(Object o) {
            if (o == null) {
                return null;
            }
            if (o instanceof Collection) {
                return ((Collection) o).size();
            }
            if (o.getClass().isArray()) {
                return Array.getLength(o);
            }
            return null;
        }

        public static boolean b(boolean b){ return b; }

        public static boolean b(Object o){ return TypeUtility.castBoolean(o,false); }

        public static boolean B(Object o){ return TypeUtility.castBoolean(o,null); }

        public static int i(int i){ return i ; }

        public static int i(Object o){ return TypeUtility.castInteger(o,0); }

        public static int I(Object o){ return TypeUtility.castInteger(o,null); }

        public static double D(Object o){ return TypeUtility.castDouble(o,null); }

        public static double r(Object o){ return TypeUtility.castDouble(o, 0); }

        public static double r(double d){ return d; }

        public static double r(float f){ return f; }

        public static double d(Object o){ return TypeUtility.castDouble(o, 0); }

        public static double d(double d){ return d; }

        public static double d(float f){ return f; }

        public static Object O(char c){ return Character.valueOf(c) ;}
        public static Object O(int i){ return Integer.valueOf(i) ;}
        public static Object O(short s){ return Short.valueOf(s) ;}
        public static Object O(double d){ return Double.valueOf(d) ;}
        public static Object O(long l){ return Long.valueOf(l) ;}
        public static Object O(boolean b){ return Boolean.valueOf(b) ;}
        public static Object O(Object o){ return o ;}

        public static List l(Object args){  return TypeUtility.combine(args); }

        public static Set s(Object args){  return TypeUtility.set(args); }

        public static Object __return__ = null;

        public static Object __error__ = null;

        @Override
        public HashMap<String,Object>  __context__() {
            HashMap<String,Object> context = new HashMap<>();
            Field[] fields = this.getClass().getDeclaredFields();
            for ( int i = 0 ; i < fields.length;i++ ){
                String name = fields[i].getName();
                Object value = null ;
                try {
                    value = fields[i].get(this);
                }catch (Exception e){

                }
                context.put(name,value);
            }
            return context;
        }

        @Override
        public Object __script_body__(Object[] args) throws Exception {
            return null;
        }

        @Override
        public Object __call__(Object[] args) {
            __return__ = null;
            __error__ = null;
            try {
                __return__ = __script_body__(args);
            } catch (Throwable throwable) {
                __error__ = throwable;
            }
            return __return__;
        }
    }
}