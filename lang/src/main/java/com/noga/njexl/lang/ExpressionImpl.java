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

package com.noga.njexl.lang;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.noga.njexl.lang.extension.oop.ScriptClass;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour;
import com.noga.njexl.lang.extension.oop.ScriptMethod;
import com.noga.njexl.lang.parser.*;

/**
 * Instances of ExpressionImpl are created by the {@link JexlEngine},
 * and this is the default implementation of the {@link Expression} and
 * {@link Script} interface.
 * @since 1.0
 */
public class ExpressionImpl implements Expression, Script , ScriptClassBehaviour.Executable {

    String location;

    String importName;

    final HashMap<String,ScriptClass> classes;

    final HashMap<String,ScriptMethod> methods;

    final HashMap<String,ASTImportStatement> imports;

    final HashMap<String,Integer> jumps;

    /**
     * This is to ensure that we can return values w/o
     * defaulting to the exact need
     * @param prop name of the class or method
     * @return the class or method, else null
     */
    @Override
    public Object get(String prop){
        if ( classes.containsKey(prop)){
            return classes.get(prop);
        }
        if ( methods.containsKey(prop)){
            return methods.get(prop);
        }
        return null;
    }


    /** The engine for this expression. */
    protected final JexlEngine jexl;
    /**
     * Original expression stripped from leading and trailing spaces.
     */
    protected final String expression;
    /**
     * The resulting AST we can interpret.
     */
    protected final ASTJexlScript script;

    protected void findInnerObjects(JexlNode node){
        if ( node instanceof ASTImportStatement ){
            ASTImportStatement importDef = (ASTImportStatement)node;
            String as = importDef.jjtGetChild(1).image ;
            imports.put( as , importDef );
        }
        else if ( node instanceof ASTClassDef){
            ScriptClass classDef = new ScriptClass(interpreter, (ASTClassDef)node,importName);
            classes.put( classDef.name , classDef );
        }
        else if ( node instanceof ASTMethodDef){
            if ( node.jjtGetParent() instanceof ASTAssignment ){
                // should be taken care later, don't worry
            }else {
                ScriptMethod methodDef = new ScriptMethod((ASTMethodDef) node);
                methods.put(methodDef.name, methodDef);
            }
        }else {
            int numChild = node.jjtGetNumChildren();
            for ( int i =0; i < numChild; i++ ){
                findInnerObjects(node.jjtGetChild(i));
            }
        }
    }

    protected void findLabels(JexlNode node){
        int n = node.jjtGetNumChildren();
        for ( int i = 0 ; i < n ; i++ ){
            JexlNode child = node.jjtGetChild(i);
            if ( child instanceof ASTLabelledStatement ){
                jumps.put( child.jjtGetChild(0).image , i );
            }
        }
    }

    protected ExpressionImpl(String from, String as, JexlEngine engine, String expr, ASTJexlScript ref) {
        jexl = engine;
        expression = expr;
        script = ref;
        methods = new HashMap<>();
        imports = new HashMap<>();
        classes = new HashMap<>();
        jumps = new HashMap<>();
        location = from ;
        if ( location != null && !location.isEmpty() ){
            location = location.replace("\\","/"); // windows?
            location = location.substring(0,location.lastIndexOf("/"));
        }
        importName = as;

        findLabels(script);
    }

    /**
     * Do not let this be generally instantiated with a 'new'.
     *
     * @param engine the interpreter to evaluate the expression
     * @param expr the expression.
     * @param ref the parsed expression.
     */
    protected ExpressionImpl(JexlEngine engine, String expr, ASTJexlScript ref) {
        this("", Script.DEFAULT_IMPORT_NAME, engine, expr, ref);
    }

    /**
     * {@inheritDoc}
     */
    public Object evaluate(JexlContext context) {
        if (script.jjtGetNumChildren() < 1) {
            return null;
        }
        Interpreter interpreter = jexl.createInterpreter(context);
        interpreter.setFrame(script.createFrame((Object[]) null));
        interpreter.current = this;
        this.interpreter = interpreter ;
        return interpreter.interpret(script.jjtGetChild(0));
    }

    /**
     * {@inheritDoc}
     */
    public String dump() {
        Debugger debug = new Debugger();
        boolean d = debug.debug(script);
        return debug.data() + (d ? " /*" + debug.start() + ":" + debug.end() + "*/" : "/*?:?*/ ");
    }

    /**
     * {@inheritDoc}
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Provide a string representation of this expression.
     * @return the expression or blank if it's null.
     */
    @Override
    public String toString() {
        String expr = getExpression();
        return expr == null ? "" : expr;
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return toString();
    }

    Interpreter interpreter;

    public Interpreter interpreter(){ return interpreter ; }

    ScriptMethod getMethod(String method){
        ScriptMethod methodDef = methods.get(method);
        if ( methodDef != null ){
            return methodDef;
        }
        // by argument?
        if ( interpreter.context.has(method)){
            Object m = interpreter.context.get(method);
            if ( m!= null ){
                if ( m instanceof ScriptMethod ) return (ScriptMethod)m;

                String[] arr = m.toString().split(":");
                // ignore first, call by last.
                return methods.get(arr[arr.length-1]);
            }
        }
        return null;
    }

    @Override
    public Object execMethod(String method,Interpreter i, Object[] args){
        try {
            ScriptMethod methodDef = getMethod(method);
            if (methodDef == null) {
                if ( MY_MAIN_I.equals(method) ){
                    JexlContext copy = i.context.copy();
                    copy.set(ARGS, args);
                    try {
                        return execute(copy, args);
                    }finally {
                        copy.clear();
                        copy = null;
                    }
                }
                throw new NoSuchMethodException("Method : '" + method + "' is not found in : " + this.importName);
            }
            return methodDef.invoke(null,i, args);
        }catch (Throwable e){
            throw new Error(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object execute(JexlContext context) {
        return execute(context, (Object[]) null);
    }

    public void setup(JexlContext context){
        interpreter = jexl.createInterpreter(context);
        findInnerObjects(this.script);
        interpreter.current = this;
        interpreter.setFrame(script.createFrame(new Object[]{}));
        interpreter.imports.put(importName, this);
        interpreter.imports.put(null, this);
        //only execute the import statements
        for ( String i : imports.keySet() ){
            ASTImportStatement importStatement = imports.get(i);
            interpreter.visit( importStatement, this);
        }
        interpreter.functions.put(importName,this);
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public Object execute(JexlContext context, Object... args) {
        interpreter = jexl.createInterpreter(context);
        findInnerObjects(this.script);
        interpreter.current = this;
        interpreter.setFrame(script.createFrame(args));
        // the following are key for calling methods ...
        interpreter.imports.put(importName, this);
        interpreter.imports.put(Script.SELF, this);
        interpreter.imports.put(null, this);
        interpreter.functions.put(importName,this);
        return interpreter.interpret(script);
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public String[] getParameters() {
        return script.getParameters();
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public String[] getLocalVariables() {
        return script.getLocalVariables();
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public Set<List<String>> getVariables() {
        return jexl.getVariables(this);
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public Callable<Object> callable(JexlContext context) {
        return callable(context, (Object[]) null);
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public Callable<Object> callable(JexlContext context, Object... args) {
        final Interpreter interpreter = jexl.createInterpreter(context);
        interpreter.setFrame(script.createFrame(args));

        return new Callable<Object>() {
            /** Use interpreter as marker for not having run. */
            private Object result = interpreter;

            public Object call() throws Exception {
                if (result == interpreter) {
                    result = interpreter.interpret(script);
                }
                return result;
            }

        };
    }

    @Override
    public String location() {
        return location;
    }

    @Override
    public HashMap<String, ScriptMethod> methods() {
        return methods;
    }


    @Override
    public HashMap<String, ScriptClass> classes() {
        return classes;
    }

    @Override
    public HashMap<String, ASTImportStatement> imports() {
        return imports;
    }

    @Override
    public ASTJexlScript script(){ return script; }

    @Override
    public String name() {
        return importName;
    }

    @Override
    public Map<String, Integer> jumps() {
        return jumps;
    }
}