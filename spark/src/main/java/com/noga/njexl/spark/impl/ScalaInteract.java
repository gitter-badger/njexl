/*
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

package com.noga.njexl.spark.impl;

import com.noga.njexl.lang.*;
import com.noga.njexl.lang.Interpreter.AnonymousParam;
import com.noga.njexl.lang.extension.TypeUtility;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import scala.Tuple2;
import scala.collection.Seq;
import scala.reflect.ClassTag;

/**
 * Created by noga on 07/11/15.
 */
public class ScalaInteract implements Serializable {

    public static final String MY_NAME = "$_" ; // java $cala bridge

    public static ClassTag $tag(Class<?> c) {
        return scala.reflect.ClassTag$.MODULE$.apply(c);
    }

    public static final ClassTag $TAG = $tag(Object.class);

    public static final Function1 $I = Function1.IDENTITY;

    public static final Function2 $I2 = Function2.IDENTITY;

    public static <T> scala.collection.immutable.List<T> $l(List<T> javaList) {
        return scala.collection.JavaConversions.asScalaIterable(javaList).toList();
    }

    public static Tuple2 $t(Object o1, Object o2) {
        return new Tuple2(o1,o2);
    }

    public static Seq $s(Object o){
        List l = TypeUtility.from(o);
        return $l(l);
    }

    public final String anonScript;

    transient Script script;

    transient JexlContext context;

    public ScalaInteract(AnonymousParam anon) {

        if (anon != null) {
            this.anonScript = Debugger.getText(anon.block );
        } else {
            this.anonScript = ";";
        }
    }


    protected Object safeCall(Object... args) {
        Object ret = Interpreter.NULL;
        Object o = args;
        if (args.length == 1) {
            o = args[0];
        }
        if (script == null) {
            //set up...
            context = Main.getContext();
            JexlEngine engine = Main.getJexl(context);
            context.set( ScalaInteract.MY_NAME , ScalaInteract.class );
            Map m = Main.getFunction(context);
            engine.setFunctions(m);
            script = engine.createScript(anonScript);
        }
        try {
            context.set(Script._ITEM_, o);
            ret = script.execute(context);
        } catch (Error e) {
            System.err.println("Error " + e.getMessage());
        }
        return ret;
    }

    public static Object f(Object... args) {
        if (args.length == 0) {
            return Function1.IDENTITY;
        }

        if (!(args[0] instanceof AnonymousParam)) {
            return Function1.IDENTITY;
        }
        AnonymousParam anon = (AnonymousParam) args[0];
        int numArgs = TypeUtility.castInteger(args[1], 0);
        switch (numArgs) {
            case 0:
                return new ScalaInteract.Function0(anon);
            case 1:
                return new ScalaInteract.Function1(anon);
            case 2:
                return new ScalaInteract.Function2(anon);
        }
        return Function1.IDENTITY;
    }

    public static class Function0 extends
            scala.runtime.AbstractFunction0 implements Serializable {

        public final ScalaInteract f;

        public Function0(AnonymousParam a) {
            f = new ScalaInteract(a);
        }

        @Override
        public Object apply() {
            Object safe = f.safeCall();
            if ( Interpreter.NULL == safe ) {
                throw new Error("Error applying Function0");
            }
            return safe;
        }
    }


    public static class Function1 extends
            scala.runtime.AbstractFunction1 implements Serializable {

        public static final Identity IDENTITY = new Identity();

        public final ScalaInteract f;

        public Function1(AnonymousParam a) {
            f = new ScalaInteract(a);
        }

        @Override
        public Object apply(Object o) {
            Object safe = f.safeCall(o);
            if ( Interpreter.NULL == safe ) {
                throw new Error("Error applying Function1");
            }
            return safe ;
        }

        public static final class Identity extends Function1 {
            private Identity() {
                super(null);
            }

            @Override
            public Object apply(Object o) {
                return o;
            }
        }
    }

    public static class Function2 extends
            scala.runtime.AbstractFunction2 implements Serializable {

        public static final Identity IDENTITY = new Identity();

        public final ScalaInteract f;

        public Function2(AnonymousParam a) {
            f = new ScalaInteract(a);
        }

        @Override
        public Object apply(Object o, Object o2) {
            Object safe = f.safeCall(o, o2);
            if ( Interpreter.NULL == safe ) {
                throw new Error("Error applying Function2");
            }
            return safe;
        }

        public static final class Identity extends Function2 {
            private Identity() {
                super(null);
            }

            @Override
            public Object apply(Object o, Object o2) {
                return new Object[]{o, o2};
            }
        }
    }
}
