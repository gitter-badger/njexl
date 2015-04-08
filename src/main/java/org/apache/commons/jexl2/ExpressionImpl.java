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


import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.jexl2.extension.oop.Invokable;
import org.apache.commons.jexl2.extension.oop.ScriptClass;
import org.apache.commons.jexl2.extension.oop.ScriptMethod;
import org.apache.commons.jexl2.parser.*;

/**
 * Instances of ExpressionImpl are created by the {@link JexlEngine},
 * and this is the default implementation of the {@link Expression} and
 * {@link Script} interface.
 * @since 1.0
 */
public class ExpressionImpl implements Expression, Script , Invokable {

    String location;

    String importName;

    final HashMap<String,ScriptClass> classes;

    final HashMap<String,ScriptMethod> methods;

    final HashMap<String,ASTImportStatement> imports;

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

    protected void findInnerObjects(JexlNode node){
        if ( node instanceof ASTClassDef ){
            ScriptClass classDef = new ScriptClass((ASTClassDef)node);
            classes.put( classDef.getName() , classDef );
        }
        else if ( node instanceof ASTMethodDef ){
            ScriptMethod methodDef = new ScriptMethod((ASTMethodDef)node);
            methods.put( methodDef.name, methodDef );
        }else {
            int numChild = node.jjtGetNumChildren();
            for ( int i =0; i < numChild; i++ ){
                findInnerObjects(node.jjtGetChild(i));
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
        location = from;
        importName = as;
        findImports(script);
        findInnerObjects(script);
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
    public Object execMethod(String method,Interpreter interpreter ,Object[] args) throws Exception{
        ScriptMethod methodDef = methods.get(method) ;
        if ( methodDef == null){
            throw new Exception("Method : '" + method + "' is not found in : " + this.importName );
        }
        return methodDef.invoke(null, interpreter, args);
    }

    @Override
    public String type(){ return  ExpressionImpl.class.getName() ; }

    /**
     * {@inheritDoc}
     */
    public Object execute(JexlContext context) {
        return execute(context, (Object[]) null);
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
    public HashMap<String, ScriptMethod> methods() {
        return methods;
    }

    /**
     * The defined classes of the script
     *
     * @return
     */
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
    /**
     * The name under which it was imported
     *
     * @return
     */
    @Override
    public String name() {
        return importName;
    }

}