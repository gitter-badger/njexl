package com.noga.njexl.lang.extension.datastructures;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.Main;

import java.util.HashMap;
import java.util.List;

/**
 * Created by noga on 04/04/15.
 */
public class Tuple  {

    public final HashMap<String,Integer> names;

    private final String mapping ;

    public final Object[] t;

    public Tuple( List<String> n, Object f){
        t = TypeUtility.array(f);
        StringBuffer buf = new StringBuffer( "[" ) ;
        names = new HashMap<>() ;
        for ( int i = 0 ; i < t.length ; i++ ){
            String c = n.get(i);
            names.put(c, i);
            buf.append(String.format("'%s'->%d ", c,i));
        }
        buf.append("]");
        mapping = buf.toString();
    }

    public Object get(int index){
        return t[index];
    }

    public Object set(int index,Object object){
        return ( t[index] = object ) ;
    }

    public Object get(String name){
        return t[ names.get(name) ] ;
    }

    public Object set(String name, Object object){
        return ( t[names.get(name)] = object ) ;
    }

    @Override
    public String toString(){
        return String.format( "<%s,%s>", mapping, Main.strArr(t) );
    }
}
