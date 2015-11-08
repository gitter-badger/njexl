package com.noga.njexl.spark.impl;

import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.Interpreter.AnonymousParam ;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by noga on 08/11/15.
 */
public class AnonymousFunction extends ScalaInteract {

    public AnonymousFunction(AnonymousParam anon){
        super(anon);
    }

    public static final class XFunction2 extends AnonymousFunction
            implements org.apache.spark.api.java.function.Function2{

        public XFunction2(AnonymousParam a){
            super(a);
        }

        @Override
        public Object call(Object v1, Object v2) throws Exception {
            Object ret = safeCall(v1,v2);
            if ( Interpreter.NULL == ret) throw new Exception("XFunction2 has thrown exception!");
            return ret;
        }
    }


    public static final class XPairFunction extends AnonymousFunction implements PairFunction{

        public XPairFunction(AnonymousParam a){
            super(a);
        }

        @Override
        public Tuple2 call(Object o) throws Exception {
            Object ret = safeCall(o);
            if ( ret!= null ){
                Object l,r;
                if ( ret.getClass().isArray() ){
                    l = Array.get(ret,0);
                    r = Array.get(ret,1);
                    return new Tuple2(l,r);
                }
                if ( ret instanceof List){
                    l = ((List) ret).get(0);
                    r = ((List) ret).get(1);
                    return new Tuple2(l,r);
                }
            }
            throw new Exception("PairFunction throw error!");
        }
    }

    public static final class XFlatMapFunction extends AnonymousFunction implements FlatMapFunction{

        public XFlatMapFunction(AnonymousParam a){
            super(a);
        }

        @Override
        public Iterable call(Object o) throws Exception {
            Object r = safeCall(o);
            if ( r instanceof Iterable ) return (Iterable)r;
            throw new Exception("FlatMapFunction throw error!");
        }
    }

}
