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

package com.noga.njexl.lang.extension.oop;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.introspection.JexlMethod;
import com.noga.njexl.lang.introspection.JexlPropertyGet;
import com.noga.njexl.lang.introspection.JexlPropertySet;
import com.noga.njexl.lang.introspection.UberspectImpl;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.*;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * An instance of nJexl class
 * Created by noga on 09/04/15.
 */
public class ScriptClassInstance implements Executable, Comparable,Arithmetic, Logic, Eventing  {


    final HashMap<String, Object> fields;

    /**
     * gets declared fields
     * @return declared fields of the object
     */
    public HashMap<String,Object> getFields(){return fields ;}

    final HashMap<String,Object> supers;

    protected void addSuper(ScriptClassInstance value){
        if ( $.ns.equals(value.$.ns)) {
            //make direct calling possible ONLY if the namespace of child and parent match
            supers.put(value.$.name, value);
        }
        supers.put( value.$.ns +":" + value.$.name, value);
    }

    /**
     * Gets the immediate super classes
     * @return immediate super class instances
     */
    public HashMap<String,Object> getSupers(){ return supers ; }

    Interpreter interpreter;

    final ScriptClass $;

    public ScriptClass getNClass(){ return $;}

    public ScriptClassInstance(ScriptClass scriptClass, Interpreter interpreter){
        this.interpreter = interpreter ;
        this.$ = scriptClass ;
        this.fields = new HashMap<>();
        this.supers = new HashMap<>();
    }

    public JexlMethod getJSuperMethod(String name, Object[] sups, Object[] args) throws Exception {

        for ( String s : supers.keySet() ){
            if ( s.contains(":") ){
                continue;
            }
            Object sup = supers.get(s);
            if ( !(sup instanceof ScriptClassInstance) ){
                JexlMethod method = interpreter.uberspect.getMethod(sup, name, args, null);
                if ( method != null ){
                    sups[0] = sup;
                    return method ;
                }
            }
        }
        return null;
    }


    public void addJSuper(String name, Object obj, Object[] args) throws Exception {
        JexlMethod ctor = interpreter.uberspect.getConstructorMethod(obj, args, null);
        // DG: If we can't find an exact match, narrow the parameters and try again
        if (ctor == null) {
            if (interpreter.arithmetic.narrowArguments(args)) {
                ctor = interpreter.uberspect.getConstructorMethod(obj, args, null);
                if (ctor == null) {
                    throw new Exception("Constructor not found!");
                }
            }
        }
        Object instance = ctor.invoke(null, args);
        supers.put(name,instance);
        supers.put($.ns + ":" + name,instance);
    }


    public void ancestor(Object sName,Object[]args) throws Exception {
        Object old = interpreter.resolveJexlClassName(sName.toString());
        if ( old != null && ((ScriptClass)old).clazz == null ) {
            ScriptClassInstance _new_ = ((ScriptClass)old).instance(interpreter, args);
            addSuper( _new_ ); // this should replace the old
            return;
        }

        String name;
        if ( sName instanceof  String ){
            sName = interpreter.getContext().get((String)sName);
        }
        if ( sName instanceof Class ){
            old = ((Class) sName).getName();
            name = ((Class) sName).getSimpleName();
        }else{
            old = sName.getClass().getName();
            name = sName.getClass().getSimpleName();
        }
        // java guy... call constructor
        addJSuper(name,old,args);
    }

    @Override
    public Object execMethod(String method, Interpreter i, Object[] args)  {
        try {
            if ( method.equals(ScriptClass._ANCESTOR_)){
                Object arg0 = args[0];
                args = TypeUtility.shiftArrayLeft(args, 1);
                ancestor(arg0,args);
                return null;
            }
            ScriptMethod methodDef = $.getMethod(method);
            if (methodDef == null ) {

                Object[] sups = new Object[1];
                JexlMethod jexlMethod = getJSuperMethod(method, sups, args);
                if (jexlMethod != null) {
                    // call jexl method
                    return jexlMethod.invoke(sups[0], args);
                }

                throw new NoSuchMethodException("Method : '" + method + "' is not found in class : " + this.$.name);
            }
            if (methodDef.instance) {
                return methodDef.invoke(this, interpreter, args);
            }
            return methodDef.invoke(null, interpreter, args);
        }catch (Throwable e){
            throw new Error(e);
        }
    }

    public Object get(String name) throws Exception {

        if (fields.containsKey(name)) {
            return fields.get(name);
        }
        for (String sup : supers.keySet()) {
            Object s = supers.get(sup);
            if (s instanceof  ScriptClassInstance ) {
                ScriptClassInstance supI = (ScriptClassInstance)s;
                if ( supI.fields.containsKey(name)){
                    return supI.fields.get(name);
                }
            }else{
                // try from this
                Field field = ((UberspectImpl)interpreter.uberspect).getField(s, name, null);
                if (field != null) {
                    JexlPropertyGet fg = new UberspectImpl.FieldPropertyGet(field);
                    return fg.invoke(s);
                }
            }
        }
        try {
            // try finding functions...?
            ScriptMethod fp = $.getMethod(name);
            Object o =  new ScriptClass.MethodInstance(this,fp.name);
            // cache it -- important
            fields.put(name,o);
            return o;

        }catch (Exception e){
            // nothing...
        }
        throw new Exception("Key : '" + name + "' is not found!");
    }

    public void set(String name, Object value) throws Exception {
        if (fields.containsKey(name)) {
            fields.put(name, value);
            return;
        }
        for (String sup : supers.keySet()) {
            Object s = supers.get(sup);
            if (s instanceof  ScriptClassInstance ) {
                ScriptClassInstance supI = (ScriptClassInstance)s;
                if ( supI.fields.containsKey(name)){
                    supI.fields.put(name,value);
                    return;
                }
            }else{
                Field field = ((UberspectImpl)interpreter.uberspect).getField(s, name, null);
                if (field != null) {
                    JexlPropertySet fs = new UberspectImpl.FieldPropertySet(field);
                    fs.invoke(s,value);
                    return;
                }
            }
        }
        fields.put(name, value);
    }

    @Override
    public String toString() {
        try {
            return execMethod(ScriptClassBehaviour.STR, interpreter ,new Object[]{}).toString();
        } catch (Throwable e) {
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        try {
            return TypeUtility.castBoolean(execMethod(ScriptClassBehaviour.EQ,interpreter , new Object[]{o}), false);
        } catch (Throwable e) {

        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        try {
            return TypeUtility.castInteger(execMethod(ScriptClassBehaviour.HC, interpreter , new Object[]{}));
        } catch (Throwable e) {
        }
        return super.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        return TypeUtility.castInteger(execMethod(ScriptClassBehaviour.CMP,interpreter, new Object[]{o}));
    }

    @Override
    public Object add(Object o) {
        return execMethod(Arithmetic.ADD,interpreter,new Object[]{o});
    }

    @Override
    public Object neg()  {
        return execMethod(Arithmetic.NEG,interpreter,new Object[]{});
    }

    @Override
    public Object sub(Object o)  {
        return execMethod(Arithmetic.SUB, interpreter , new Object[]{o});
    }

    @Override
    public Object mul(Object o)  {
        return execMethod(Arithmetic.MUL, interpreter , new Object[]{o});
    }

    @Override
    public Object div(Object o)  {
        return execMethod(Arithmetic.DIV, interpreter , new Object[]{o});
    }

    @Override
    public Object exp(Object o)  {
        return execMethod(Arithmetic.EXP, interpreter , new Object[]{o});
    }

    @Override
    public Object and(Object o)  {
        return execMethod(Logic.AND,interpreter, new Object[]{o});
    }

    @Override
    public Object complement()  {
        return execMethod(Logic.COMPLEMENT,interpreter ,new Object[]{});
    }

    @Override
    public Object or(Object o) {
        return execMethod(Logic.OR,interpreter ,new Object[]{o});
    }

    @Override
    public Object xor(Object o)  {
        return execMethod(Logic.XOR,interpreter , new Object[]{o});
    }


    @Override
    public void before(Event event) {
        try{
            Object[] params = new Object[]{ event  } ;
            execMethod(BEFORE,interpreter ,params );
        }catch (Throwable t){
            if ( t.getCause() instanceof NoSuchMethodException ) {
                Timer.TIMER.before(event);
            }
        }
    }

    @Override
    public void after(Event event) {
        try{
            Object[] params = new Object[]{ event } ;
            execMethod(AFTER,interpreter,params );
        }catch (Throwable t){
            if ( t.getCause() instanceof NoSuchMethodException ) {
                Timer.TIMER.after(event);
            }
        }
    }
}
