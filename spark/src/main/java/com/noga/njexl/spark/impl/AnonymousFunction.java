/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.spark.impl;

import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.Interpreter.AnonymousParam ;
import com.noga.njexl.lang.extension.TypeUtility;
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

    public static final class XVoidFunction extends AnonymousFunction
            implements VoidFunction{

        public XVoidFunction(AnonymousParam a){
            super(a);
        }

        @Override
        public void call(Object o) throws Exception {
            Object ret = safeCall(o);
            if ( Interpreter.NULL == ret) throw new Exception("XFunction has thrown exception!");
        }
    }

    public static final class XFunction extends AnonymousFunction
            implements Function{

        public XFunction(AnonymousParam a){
            super(a);
        }

        @Override
        public Object call(Object v1) throws Exception {
            Object ret = safeCall(v1);
            if ( Interpreter.NULL == ret) throw new Exception("XFunction has thrown exception!");
            return ret;
        }
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

    public static final class XDoubleFunction extends AnonymousFunction
            implements DoubleFunction {
        public XDoubleFunction(AnonymousParam a){
            super(a);
        }

        @Override
        public double call(Object o) throws Exception {
            Object safe = safeCall(o);
            if ( safe instanceof Number ){
                return ((Number) safe).doubleValue();
            }
            throw new Exception("XDoubleFunction failed!");
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

    public static final class XFlatMapFunction extends AnonymousFunction
            implements FlatMapFunction,PairFlatMapFunction, DoubleFlatMapFunction {

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
