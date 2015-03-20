package org.apache.commons.jexl2.jvm;

import org.apache.commons.jexl2.extension.TypeUtility;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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