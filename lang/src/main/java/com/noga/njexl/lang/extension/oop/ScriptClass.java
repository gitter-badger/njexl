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
import com.noga.njexl.lang.Debugger;
import com.noga.njexl.lang.parser.ASTClassDef;
import com.noga.njexl.lang.parser.ASTMethodDef;
import com.noga.njexl.lang.parser.JexlNode;
import com.noga.njexl.lang.Interpreter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.TypeAware;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Executable;


/**
 * Created by noga on 08/04/15.
 */
public class ScriptClass  implements TypeAware, Executable {

    /**
     * A wrapper for instance method instances
     */
    public static final class MethodInstance implements Executable {

        /**
         * The instance which to pass as *me* pointer
         */
        public final Executable executable ;

        /**
         * The name of the method
         */
        public final String methodName ;

        /**
         * Creates such a wrapper
         * @param exec executable object
         * @param method method name
         */
        public MethodInstance(Executable exec, String method){
            executable = exec ;
            methodName = method ;
        }

        /**
         * Calls the methodName
         * @param method name of the method - ignored
         * @param args to be passed as argument
         * @return result object
         */

        @Override
        public Object execMethod(String method, Object[] args) {
            return executable.execMethod(methodName,args );
        }
    }



    public static final String _INIT_ = "__new__";

    public static final String _CL_INIT_ = "__class__";

    public static final String SELF = "me";

    Interpreter interpreter;

    final Map<String,Object> statics;

    public Map<String,Object> statics(){
        return statics;
    }

    public Object get(String prop){
        if ( statics.containsKey(prop )) {
            return statics.get(prop);
        }
        try {
            ScriptMethod sm = getMethod(prop);
            if ( sm != null ){
                if ( sm.instance ){
                    statics.put(prop,null); // can not return instance method...
                    return null;
                }
                Object o = new MethodInstance(this,prop);
                statics.put(prop,o);
                return o;
            }
        }catch (Exception e){
        }
        return null;
    }

    public Object set(String prop, Object value){
        return statics.put(prop, value);
    }

    final Map<String, ScriptMethod> methods;

    public Map getMethods() {
        return methods;
    }

    ScriptMethod classInit;

    protected void classInit(){
        try{
            classInit.invoke(this,this.interpreter,new Object[]{});
        }catch (Exception e){
            throw new Error(e);
        }
    }

    ScriptMethod constructor;

    final Map<String, ScriptClass> supers;

    public Map getSupers() {
        return supers;
    }

    protected void findMethods(JexlNode node) {
        if (node instanceof ASTMethodDef) {
            ScriptMethod methodDef = new ScriptMethod((ASTMethodDef) node);
            methods.put(methodDef.name, methodDef);
        } else {
            int numChild = node.jjtGetNumChildren();
            for (int i = 0; i < numChild; i++) {
                findMethods(node.jjtGetChild(i));
            }
        }
    }

    public ScriptMethod getMethod(String method) throws Exception {
        if (methods.containsKey(method)) {
            return methods.get(method);
        } else {
            ScriptMethod m1;
            ScriptMethod m2 = null;
            for (String sup : supers.keySet()) {
                m1 = supers.get(sup).getMethod(method);
                if (m1 != null) {
                    if (m2 == null) {
                        m2 = m1;
                    } else {
                        // m2 exists, we have diamond issue
                        throw new Exception("Ambiguous Method : '" + method + "' !");
                    }
                }
            }
            return m2;
        }
    }

    public final String ns;

    public final String name;

    public final String hash;

    public String getName() {
        return name;
    }

    public ScriptClassInstance  instance(Interpreter interpreter, Object[] args) throws Exception {

        ScriptClassInstance instance = new ScriptClassInstance(this, interpreter);
        Set<String> superKeys = new HashSet<>(supers.keySet());
        for (String n : superKeys ) {
            ScriptClass superClass = supers.get(n);
            if (superClass == null) {
                superClass = interpreter.resolveJexlClassName(n);
                if ( superClass == null ) {
                    throw new Exception("Superclass : '" + n + "' not found!");
                }
                instance.hasJSuper = (superClass.clazz != null );
                // one time resolving of this
                addSuper(superClass.ns, superClass.name, superClass);
            }
            if ( superClass.clazz == null ) {
                ScriptClassInstance superClassInstance = superClass.instance(interpreter, args);
                instance.addSuper(superClassInstance);
            }else{
                instance.addJSuper( superClass.name, superClass.clazz, args);
            }
        }
        if (constructor != null) {
            instance.execMethod(_INIT_, args);
        }
        // return
        return instance ;
    }

    protected void addSuper(String ns, String superName, ScriptClass value){
        if ( this.ns.equals(ns)) {
            supers.put(superName, value);
        }
        supers.put( ns +":" + superName, value);
    }

    public ScriptClass(Interpreter interpreter, ASTClassDef ref,String ns) {
        this.interpreter = interpreter ;
        this.ns = ns ;
        name = ref.jjtGetChild(0).image;
        methods = new HashMap<>();
        supers = new HashMap<>();
        int numChild = ref.jjtGetNumChildren();
        for (int i = 1; i < numChild - 1; i++) {
            int n = ref.jjtGetChild(i).jjtGetNumChildren();
            String supNs = this.ns;
            String superName = ref.jjtGetChild(i).jjtGetChild(0).image;
            if (n > 1) {
                supNs = superName;
                superName = ref.jjtGetChild(i).jjtGetChild(1).image;
            }
            addSuper(supNs, superName, null);
        }
        findMethods(ref.jjtGetChild(numChild - 1));
        constructor = methods.get(_INIT_);
        hash = TypeUtility.hash(Debugger.getText(ref));
        clazz = null ;
        statics = new ConcurrentHashMap<>();
        this.interpreter.getContext().set(this.name,this);
        classInit = methods.get(_CL_INIT_);
        if ( classInit != null ){
            classInit();
        }
    }

    public final Class clazz;

    public ScriptClass(Interpreter interpreter, String name, Object o,String ns){
        this.interpreter = interpreter ;
        Class c ;
        if (o instanceof  Class){
            c = (Class)o ;
        }else{
            c = o.getClass();
        }
        clazz = c ;
        this.ns = ns ;
        this.name = name ;
        hash = ns;
        // we will do something about them later
        methods = new HashMap<>();
        supers = new HashMap<>();
        statics = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isa(Object o) {
        if ( o == null ){
            return false ;
        }
        ScriptClass that = null;
        if ( o instanceof ScriptClass){
            that = (ScriptClass)o;
        }
        else if ( o instanceof ScriptClassInstance){
            that = ((ScriptClassInstance)o).$;
        }
        if ( that != null){
            // match body hash
            if ( this.hash.equals(that.hash) ){
                return true ;
            }
        }
        else{
            // do we have Java Supers?
            Class clazz;
            if  ( o instanceof Class){
                clazz = (Class)o;
            }else{
                clazz = o.getClass();
            }
            if ( this.clazz != null && clazz.isAssignableFrom(this.clazz)){
                return true ;
            }
        }
        // nothing, continue upward : may end in loop
        for ( String n :  supers.keySet()){
            boolean s = supers.get(n).isa(o);
            if ( s ){
                return true ;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return String.format("nClass %s:%s", ns, name) ;
    }

    @Override
    public Object execMethod(String methodName, Object[] args) {
        try {
            ScriptMethod scriptMethod = getMethod(methodName);
            if ( scriptMethod == null ) {
                throw new Error(new NoSuchMethodException(methodName));
            }
            interpreter.addFunctionNamespace(SELF,this);
            return scriptMethod.invoke(this, interpreter, args);
        }catch (Exception e){
            throw new Error(e);
        }
        finally {
            // do some housekeeping
            interpreter.removeFunctionNamespace(SELF) ;
        }
    }
}
