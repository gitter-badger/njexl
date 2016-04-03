/*
* Copyright 2016 Nabarun Mondal
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
import com.noga.njexl.lang.extension.datastructures.XList;
import com.noga.njexl.lang.parser.ASTBlock;
import com.noga.njexl.lang.parser.ASTIdentifier;
import com.noga.njexl.lang.parser.ASTMethodDef;
import com.noga.njexl.lang.parser.JexlNode;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Eventing.Event;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Eventing;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Executable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Also supports closure...
 * Created by noga on 08/04/15.
 */
public class ScriptMethod implements Serializable {

    public static final String IDENTITY = "def(){ __args__ ; }" ;

    public static final String COMPOSE = "def(){ _L_ = def() %s ; _R_ = def() %s ; _a_ = __args__ ; " +
            " r = _R_( __args__ = _a_ ) ;  _L_( __args__ = array(r) ) ; }" ;

    public static final String POW = "def(){ _ME_ = def() %s ; _a_ = __args__ ; for( i : [0:%d] ) " +
            " { _a_ = _ME_( __args__ = _a_ ) } ; _a_ }" ;

    public final transient String name;

    public final transient ASTBlock astBlock;

    public final transient boolean instance;

    public final transient HashMap<String, Object> defaultValues;

    public final transient ListSet<String> params;

    public final transient boolean nested ;

    public final transient JexlContext parentContext;

    public final String definitionText ;

    /**
     * In case this is defined inside a class
     */
    Executable declaringObject = null;

    public static boolean isNested(JexlNode current){
        while ( current != null ){
            current = current.jjtGetParent();
            if ( current instanceof ASTMethodDef) return true ;
        }
        return false ;
    }

    /**
     * Creates a method back from definition text !
     * @param definitionText the text of the method
     * @param context a @{JexlContext} within which the method would be created
     * @return  a @{ScriptMethod}
     */
    public static ScriptMethod fromDefinitionText(String definitionText, JexlContext context){
        JexlEngine e = new JexlEngine();
        Script s = e.createScript( definitionText );
        s.execute(context);
        return s.methods().values().iterator().next() ;
    }

    /**
     * Compose functions f of g , f is myself
     * @param other other, the g
     * @return composition
     */
    public ScriptMethod compose( ScriptMethod other){
        String me = Debugger.getText( astBlock );
        String him = Debugger.getText( other.astBlock );
        String defText = String.format( COMPOSE , me, him );
        return fromDefinitionText( defText, parentContext );
    }

    /**
     * Compose f^l(x)
     * @param l the power in long
     * @return a function composition of power
     */
    public ScriptMethod pow( long l ){
        if ( l < 0 ) throw new IllegalArgumentException("Power of a function must be >= 0 !");
        if ( l == 0 ) return fromDefinitionText( IDENTITY , parentContext );
        if ( l == 1 ) return this ;
        String me = Debugger.getText( astBlock );
        String defText = String.format( POW , me, l );
        return fromDefinitionText( defText, parentContext );
    }

    public ScriptMethod(ASTMethodDef def){
        this(def,null);
    }

    public ScriptMethod(ASTMethodDef def, JexlContext parent) {

        definitionText = Debugger.getText(def);

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
        before = new XList<>();
        after = new XList<>();
    }

    public final transient List before;

    public final transient List after;

    private void dispatch(String p, Interpreter interpreter,
                          Object[] args, Object result, Throwable error, List listeners){
        Event event = new Event( p, this, args, result, error );
        Object[] argv = new Object[]{ event } ;
        for ( Object l : listeners ){

            if ( l instanceof ScriptClassBehaviour.Executable ){
                ScriptClassBehaviour.Executable e = (ScriptClassBehaviour.Executable)l;
                try {
                    e.execMethod(p, interpreter, args );
                }catch (Throwable t){
                }
            }
            if ( l instanceof ScriptMethod ){
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

    protected boolean checkIfReflectiveCall(Object[] args){
        try{
            ScriptClassInstance instance = (ScriptClassInstance)args[0];
            return instance.$.methods.containsKey(this.name);
        }catch (Exception e){
            return false ;
        }
    }

    public Object invoke(Object me, Interpreter interpreter, Object[] args) throws Exception {
        dispatch(Eventing.BEFORE, interpreter,args,null,null,before);
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
        else if(declaringObject != null ){
            if ( instance ) {
                boolean reflective = checkIfReflectiveCall(args);
                if (reflective) {
                    me = args[0];
                    map.put(ScriptClass.SELF, me);
                    args = TypeUtility.shiftArrayLeft(args, 1);
                } else {
                    throw new Exception("Instance Method called with null instance!");
                }
            }else{
                // static
                map.put(ScriptClass.SELF, declaringObject );
            }
        }
        addToContext(context, map);
        Object ret = null;
        Throwable error = null;
        try {
            interpreter.setContext(context);
            ret = astBlock.jjtAccept(interpreter,null);
            return ret;
        }catch (Exception e){
            if ( e instanceof JexlException.Return){
                return ((JexlException.Return) e).getValue();
            }
            error = e;
            e.printStackTrace(); // ensure that we have an issue which is to be noted down...
        }
        finally {
            dispatch(Eventing.AFTER, interpreter,args,ret, error , after);
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
                ", body=" + definitionText +
                '}';
    }

    @Override
    protected void finalize() throws Throwable {
        this.after.clear();
        this.before.clear();
        this.defaultValues.clear();
        this.params.clear();
        super.finalize();
    }
}
