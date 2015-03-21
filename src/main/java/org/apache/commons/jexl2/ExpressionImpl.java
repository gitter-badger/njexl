/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jexl2;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javassist.CtClass;
import org.apache.commons.jexl2.extension.TypeUtility;
import org.apache.commons.jexl2.jvm.ClassGen;
import org.apache.commons.jexl2.jvm.DynaCallable;
import org.apache.commons.jexl2.parser.*;

/**
 * Instances of ExpressionImpl are created by the {@link JexlEngine},
 * and this is the default implementation of the {@link Expression} and
 * {@link Script} interface.
 * @since 1.0
 */
public class ExpressionImpl implements Expression, Script {

    String location;

    String importName;

    ClassGen classGen ;

    Class myself;

    HashMap<String,ASTMethodDef> methods;

    HashMap<String,ASTImportStatement> imports;


    /** The engine for this expression. */
    protected final JexlEngine jexl;
    /**
     * Original expression stripped from leading & trailing spaces.
     */
    protected final String expression;
    /**
     * The resulting AST we can interpret.
     */
    protected final ASTJexlScript script;

    protected void findImports(JexlNode node){
        if ( node instanceof ASTImportStatement ){
            ASTImportStatement importDef = (ASTImportStatement)node;
            String as = importDef.jjtGetChild(1).image ;
            imports.put( as , importDef );
        }else {
            int numChild = node.jjtGetNumChildren();
            for ( int i =0; i < numChild; i++ ){
                findImports(node.jjtGetChild(i));
            }
        }
    }

    protected void findMethods(JexlNode node){
        if ( node instanceof ASTMethodDef ){
            ASTMethodDef methodDef = (ASTMethodDef)node;
            methods.put( methodDef.jjtGetChild(0).image, methodDef );
            int numChild = methodDef.jjtGetNumChildren();
            for ( int i = 1; i < numChild-1;i++ ){
                String paramName = methodDef.jjtGetChild(i).image;
            }
        }else {
            int numChild = node.jjtGetNumChildren();
            for ( int i =0; i < numChild; i++ ){
                findMethods( node.jjtGetChild(i) );
            }
        }
    }

    @Override
    public Class myClass(HashMap<String,Object> context){
        if ( myself == null ) {
            try {
                myself = classGen.createClass(context);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return myself;
    }

    protected ExpressionImpl(String from, String as, JexlEngine engine, String expr, ASTJexlScript ref) {
        jexl = engine;
        expression = expr;
        script = ref;
        methods = new HashMap<>();
        imports = new HashMap<>();
        location = from;
        importName = as;
        findImports(script);
        findMethods(script);
        classGen = new ClassGen(this);
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


    @Override
    public Object execMethod(String method,Interpreter interpreter ,Object[] args){
        ASTMethodDef methodDef = methods.get(method) ;
        if ( methodDef == null ){
            return null;
        }
        JexlContext context = interpreter.context;
        Object ret = null;
        int numChild = methodDef.jjtGetNumChildren();
        int numParams = numChild - 2 ; // function_name ...params... code_block

        try {
            ASTBlock codeBlock = (ASTBlock) methodDef.jjtGetChild(numChild - 1);

            // create the params
            for ( int i = 0 ; i< numParams;i++ ){
                String paramName = methodDef.jjtGetChild(i+1).image;
                if ( i< args.length ){
                    context.set(paramName,args[i]);
                }else{
                    context.set(paramName,null);
                }
            }
            ret = codeBlock.jjtAccept(interpreter,null);

        }catch (JexlException.Return er){
            ret = er.getValue();
        }
        finally {
            //clear the params
            for (int i = 0; i < numParams; i++) {
                String paramName = methodDef.jjtGetChild(i + 1).image;
                //remove context
                context.remove(paramName);
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Object execute(JexlContext context) {
        return execute( context,(Object[])null);
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    public Object execute(JexlContext context, Object... args) {
        Interpreter interpreter = jexl.createInterpreter(context);
        interpreter.setFrame(script.createFrame(args));
        // the following are key for calling methods ...
        interpreter.imports.put(importName, this);
        interpreter.imports.put(Script.SELF, this);
        interpreter.imports.put(null, this);
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

    /**
     * From where it was imported
     *
     * @return
     */
    @Override
    public String location() {
        return location;
    }

    /**
     * The defined methods of the script
     *
     * @return
     */
    @Override
    public HashMap<String, ASTMethodDef> methods() {
        return methods;
    }

    @Override
    public HashMap<String, ASTImportStatement> imports() {
        return imports;
    }

    @Override
    public ASTJexlScript script(){ return script; }
    /**
     * The name under which it was imported
     *
     * @return
     */
    @Override
    public String name() {
        return importName;
    }

    void initContext(Object o, HashMap<String,Object> context)throws Exception {
        if ( context == null ){
            return;
        }
        for ( String name : context.keySet()){
            Field f = o.getClass().getDeclaredField(name);
            f.set(o,context.get(name));
        }
    }

    @Override
    public Object executeJVM(HashMap<String,Object> context, Object... args){
        Class c = myClass(context);
        if ( c == null ){
            return null;
        }
        DynaCallable dynaCallable = null;
        try {
            dynaCallable = (DynaCallable)c.newInstance();
            initContext(dynaCallable,context);
            Object r = dynaCallable.__call__(args);
            return r;
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            if ( dynaCallable != null ) {
                HashMap map = dynaCallable.__context__() ;
                if ( map != null && context != null ) {
                    context.putAll(map);
                }
            }
        }
        return null;
    }

}