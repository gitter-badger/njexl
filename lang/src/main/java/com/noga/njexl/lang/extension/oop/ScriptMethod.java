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

import com.noga.njexl.lang.*;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.parser.ASTBlock;
import com.noga.njexl.lang.parser.ASTIdentifier;
import com.noga.njexl.lang.parser.ASTMethodDef;
import com.noga.njexl.lang.parser.JexlNode;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Eventing.Event;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Eventing;

import java.util.HashMap;
import java.util.Set;

/**
 * Also supports closure...
 * Created by noga on 08/04/15.
 */
public class ScriptMethod {

    public final String name;

    public final ASTBlock astBlock;

    public final boolean instance;

    public final HashMap<String, Object> defaultValues;

    public final ListSet<String> params;

    public final boolean nested ;

    public final JexlContext parentContext;


    public static boolean isNested(JexlNode current){
        while ( current != null ){
            current = current.jjtGetParent();
            if ( current instanceof ASTMethodDef) return true ;
        }
        return false ;
    }

    public ScriptMethod(ASTMethodDef def){
        this(def,null);
    }

    public ScriptMethod(ASTMethodDef def, JexlContext parent) {

        JexlNode n = def.jjtGetChild(0);
        int start ;
        if ( n instanceof ASTIdentifier ){
            start = 1 ;
            name = n.image ;
        }else{
            name = "" ; // anonymous, so no name
            start = 0 ;
        }
        nested = isNested(def); // is it a closure definition  ?
        parentContext = parent ; // set it up for closure

        defaultValues = new HashMap<>();
        params = new ListSet<>();
        int numChild = def.jjtGetNumChildren();
        for (int i = start ; i < numChild - 1; i++) {
            // process params
            String argName = def.jjtGetChild(i).jjtGetChild(0).image;
            params.add(argName);
            Object value = null;
            if (def.jjtGetChild(i).jjtGetNumChildren() == 2) {
                value = def.jjtGetChild(i).jjtGetChild(1);
            }
            defaultValues.put(argName, value);
        }
        astBlock = (ASTBlock) def.jjtGetChild(numChild - 1);
        instance = ( !params.isEmpty() && ScriptClass.SELF.equals(params.get(0)));
        if ( instance ) {
            // This was always dummy...
            params.remove(0);
        }
        before = new ListSet<>();
        after = new ListSet<>();
    }

    public final ListSet before;

    public final ListSet after;

    private void dispatch(String p, Interpreter interpreter, Object[] args, ListSet listeners){
        Event event = new Event( p, this, args );
        Object[] argv = new Object[]{ event } ;
        for ( Object l : listeners ){
            if ( l instanceof ScriptClassInstance ){
                ScriptClassInstance i = (ScriptClassInstance)l;
                try {
                    i.execMethod( p , argv );
                }catch (Throwable t){
                }
            }
            else if ( l instanceof ScriptMethod ){
                ScriptMethod m = (ScriptMethod)l;
                try {
                    m.invoke( null , interpreter, argv );
                }catch (Throwable t){
                }
            }
        }
    }

    private Object getDefault(String paramName,Interpreter interpreter) {
        if (defaultValues.containsKey(paramName)) {
            Object defValue =  defaultValues.get(paramName);
            if ( defValue instanceof JexlNode) {
                defValue = ((JexlNode)defValue).jjtAccept(interpreter, null);
            }
            return defValue;
        }
        return null;
    }

    private HashMap<String, Object> getParamValues(Interpreter interpreter, Object[] args) throws Exception {
        HashMap<String, Object> map = new HashMap();
        boolean namedArgs = false ;
        for (int i = 0; i < args.length; i++) {
            if ( args[i] instanceof Interpreter.AnonymousParam ){
                continue;
            }
            if (args[i] instanceof Interpreter.NamedArgs) {
                Interpreter.NamedArgs na = (Interpreter.NamedArgs) args[i];
                map.put(na.name, na.value);
                namedArgs = true ;
                continue;
            }
            if ( namedArgs ) {
                // should never reach here....!
                throw new Exception("All Parameters are not using '=' ");
            }
        }
        boolean __args__ = map.containsKey( Script.ARGS) ;

        if ( namedArgs && ! __args__ ) {
            // now put the back stuff
            Set<String> remaining = SetOperations.set_d(defaultValues.keySet(), map.keySet());
            for (String name : remaining) {
                Object defValue = getDefault(name,interpreter);
                map.put(name, defValue);
            }
        }
        else{
            if ( __args__ ){
                // I specified an array to overwrite, so :
                args = TypeUtility.array(map.get(Script.ARGS)); // unwrap!
            }
            for ( int i = 0 ; i < params.size();i++ ){
                String name = params.get(i);
                if ( i < args.length ){
                    map.put(name,args[i]);
                }else{
                    Object defValue = getDefault(name,interpreter);
                    map.put(name, defValue);
                }
            }
        }
        map.put(Script.ARGS,args);
        return map;
    }

    private void addToContext(JexlContext context, HashMap<String, Object> map) {
        for (String key : map.keySet()) {
            context.set(key, map.get(key));
        }
    }

    public Object invoke(Object me, Interpreter interpreter, Object[] args) throws Exception {
        JexlContext oldContext = interpreter.getContext();
        JexlContext toBeCopiedContext = oldContext ;
        if ( nested ){
            // closure and nested functions
            //TODO : how do we integrate interpreter context changes here?
            toBeCopiedContext = parentContext ;
        }
        //process params : copy context
        JexlContext context = toBeCopiedContext.copy();

        HashMap map = getParamValues(interpreter, args);
        if ( me != null ){
            map.put(ScriptClass.SELF, me);
        }
        else if(instance){
            throw new Exception("Instance Method called with null instance!");
        }
        addToContext(context, map);
        try {
            interpreter.setContext(context);
            dispatch(Eventing.BEFORE, interpreter,args,before);
            Object ret = astBlock.jjtAccept(interpreter,null);
            return ret;
        }catch (Exception e){
            if ( e instanceof JexlException.Return){
                return ((JexlException.Return) e).getValue();
            }
            e.printStackTrace(); // ensure that we have an issue which is to be noted down...
        }
        finally {
            dispatch(Eventing.AFTER, interpreter,args,after);
            //finally remove
            interpreter.setContext(oldContext);
        }
        return null;
    }

    @Override
    public String toString() {
        return "ScriptMethod{" +
                " name='" + name + '\'' +
                ", instance=" + instance +
                '}';
    }
}
