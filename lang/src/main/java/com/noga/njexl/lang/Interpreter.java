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

package com.noga.njexl.lang;

import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.iterators.RangeIterator;
import com.noga.njexl.lang.extension.oop.ScriptClass;
import com.noga.njexl.lang.extension.oop.ScriptClassInstance;
import com.noga.njexl.lang.extension.oop.ScriptMethod;
import com.noga.njexl.lang.internal.logging.Log;
import com.noga.njexl.lang.introspection.*;
import com.noga.njexl.lang.parser.*;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Executable;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Eventing;
import com.noga.njexl.lang.introspection.JexlPropertySet;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An interpreter of JEXL syntax.
 *
 * @since 2.0
 */
public class Interpreter implements ParserVisitor {

    HashMap<String, Script> imports;

    public HashMap<String, Script> imports() {
        return imports;
    }

    public ScriptClass resolveJexlClassName(String name) {

        String[] arr = name.split(":");
        if (arr.length > 1) {
            // namespaced
            String s = arr[0];
            name = arr[1];
            Script script = imports.get(s);
            if (script == null) {
                return null;
            }
            Map<String, ScriptClass> map = script.classes();
            if (map.containsKey(name)) {
                return map.get(name);
            }
            // you wanted specific, hence, I can not load you
            return null;
        }
        // Try, is that a Java object loaded?
        if (context.has(name)) {
            Object o = context.get(name);
            if ( !(o instanceof ScriptClass) ){
                return new ScriptClass(this, name, o , current.name());
            }
        }
        for (String s : imports.keySet()) {
            Script script = imports.get(s);
            Map<String, ScriptClass> map = script.classes();
            if (map.containsKey(name)) {
                return map.get(name);
            }
        }
        return null;
    }

    protected Script resolveScriptForFunction(String prefix, String name) {
        Script script = imports.get(prefix);
        if (script != null) {
            if (script.methods().containsKey(name)) {
                return script;
            }
        }
        // else do some more
        for (String key : imports.keySet()) {
            script = imports.get(key);
            Map methods = script.methods();
            if (methods.containsKey(name)) {
                return script;
            }
        }
        // now, is this name of a method passed as an arg?
        if (context.has(name)) {
            Object fo = context.get(name);
            if (fo != null) {
                String m = fo.toString();
                String[] arr = m.split(":");
                if (arr.length > 1) {
                    prefix = arr[0];
                    name = arr[1];
                } else {
                    name = m;
                }
                return resolveScriptForFunction(prefix, name);
            }
        }
        return null;
    }

    protected JexlEngine jexlEngine;
    /**
     * The logger.
     */
    protected final Log logger;
    /**
     * The uberspect.
     */
    public final Uberspect uberspect;
    /**
     * The arithmetic handler.
     */
    public final JexlArithmetic arithmetic;
    /**
     * The map of registered functions.
     */
    protected final Map<String, Object> functions;

    public void addFunctionNamespace(String ns, Object o){ functions.put(ns,o) ; }

    public void removeFunctionNamespace(String ns){ functions.remove(ns) ; }

    /**
     * The map of registered functions.
     */
    protected Map<String, Object> functors;
    /**
     * The context to store/retrieve variables.
     */
    protected JexlContext context;
    /**
     * Strict interpreter flag. Do not modify; will be made final/private in a later version.
     */
    protected boolean strict;
    /**
     * Silent intepreter flag.  Do not modify; will be made final/private in a later version.
     */
    protected boolean silent;
    /**
     * Cache executors.
     */
    protected final boolean cache;
    /**
     * Registers or arguments.
     */
    protected Object[] registers = null;
    /**
     * Parameter names if any.
     * Intended for use in debugging; not currently used externally.
     *
     * @since 2.1
     */
    @SuppressWarnings("unused")
    private String[] parameters = null;

    /**
     * Cancellation support.
     *
     * @see #isCancelled()
     * @since 2.1
     */
    private volatile boolean cancelled = false;

    /**
     * Empty parameters for method matching.
     */
    protected static final Object[] EMPTY_PARAMS = new Object[0];

    /**
     * Creates an interpreter.
     *
     * @param jexl       the engine creating this interpreter
     * @param aContext   the context to evaluate expression
     * @param strictFlag whether this interpreter runs in strict mode
     * @param silentFlag whether this interpreter runs in silent mode
     * @since 2.1
     */
    public Interpreter(JexlEngine jexl, JexlContext aContext, boolean strictFlag, boolean silentFlag) {

        this.jexlEngine = jexl;
        this.logger = jexl.logger;
        this.uberspect = jexl.uberspect;
        this.arithmetic = jexl.arithmetic;
        if (jexl.shareImports) {
            this.functions = jexl.functions;
        } else {
            // why this? Because use *clone* not the same object
            this.functions = new HashMap<>(jexl.functions);
        }
        this.strict = strictFlag;
        this.silent = silentFlag;
        this.cache = jexl.cache != null;
        this.context = aContext != null ? aContext : JexlEngine.EMPTY_CONTEXT;
        this.functors = null;
        imports = new HashMap<>();
        imports.putAll(jexlEngine.imports);
    }

    /**
     * Copy constructor.
     *
     * @param base the base to copy
     * @since 2.1
     */
    protected Interpreter(Interpreter base) {
        this.jexlEngine = base.jexlEngine;
        this.logger = base.logger;
        this.uberspect = base.uberspect;
        this.arithmetic = base.arithmetic;
        // use *clone* not the actual
        this.functions = new HashMap<>(base.functions);
        this.strict = base.strict;
        this.silent = base.silent;
        this.cache = base.cache;
        this.context = base.context;
        this.functors = base.functors;
        this.imports = base.imports;
    }

    /**
     * Sets whether this interpreter considers unknown variables, methods and constructors as errors.
     *
     * @param flag true for strict, false for lenient
     * @since 2.1
     * @deprecated Do not use; will be removed in a later version
     */
    // TODO why add a method and then deprecate it?
    @Deprecated
    public void setStrict(boolean flag) {
        this.strict = flag;
    }

    /**
     * Sets whether this interpreter throws JexlException when encountering errors.
     *
     * @param flag true for silent, false for verbose
     * @deprecated Do not use; will be removed in a later version
     */
    @Deprecated
    public void setSilent(boolean flag) {
        this.silent = flag;
    }

    /**
     * Checks whether this interpreter considers unknown variables, methods and constructors as errors.
     *
     * @return true if strict, false otherwise
     * @since 2.1
     */
    public boolean isStrict() {
        return this.strict;
    }

    /**
     * Checks whether this interpreter throws JexlException when encountering errors.
     *
     * @return true if silent, false otherwise
     */
    public boolean isSilent() {
        return this.silent;
    }

    /**
     * Interpret the given script/expression.
     * <p>
     * If the underlying JEXL engine is silent, errors will be logged through its logger as info.
     * </p>
     *
     * @param node the script or expression to interpret.
     * @return the result of the interpretation.
     * @throws JexlException if any error occurs during interpretation.
     */
    public Object interpret(JexlNode node) {
        try {
            return node.jjtAccept(this, null);
        } catch (JexlException.Return xreturn) {
            Object value = xreturn.getValue();
            return value;
        } catch (JexlException xjexl) {
            if (silent) {
                logger.warn(xjexl.getMessage(), xjexl.getCause());
                return null;
            }
            throw xjexl;
        } finally {
            functors = null;
            parameters = null;
            registers = null;
        }
    }

    /**
     * Gets the context.
     *
     * @return the {@link JexlContext} used for evaluation.
     * @since 2.1
     */
    public JexlContext getContext() {
        return context;
    }

    /**
     * Sets the context : needed but dangerous idea
     *
     * @param context the context in which it would wrok
     */
    public void setContext(JexlContext context) {
        this.context = context;
    }

    /**
     * Gets the uberspect.
     *
     * @return an {@link Uberspect}
     */
    protected Uberspect getUberspect() {
        return uberspect;
    }

    /**
     * Sets this interpreter registers for bean access/assign expressions.
     * <p>Use setFrame(...) instead.</p>
     *
     * @param theRegisters the array of registers
     */
    @Deprecated
    protected void setRegisters(Object... theRegisters) {
        if (theRegisters != null) {
            String[] regStrs = new String[theRegisters.length];
            for (int r = 0; r < regStrs.length; ++r) {
                regStrs[r] = "#" + r;
            }
            this.parameters = regStrs;
        }
        this.registers = theRegisters;
    }

    /**
     * Sets this interpreter parameters and arguments.
     *
     * @param frame the calling frame
     * @since 2.1
     */
    protected void setFrame(JexlEngine.Frame frame) {
        if (frame != null) {
            this.parameters = frame.getParameters();
            this.registers = frame.getRegisters();
        } else {
            this.parameters = null;
            this.registers = null;
        }
    }

    /**
     * Finds the node causing a NPE for diadic operators.
     *
     * @param xrt   the RuntimeException
     * @param node  the parent node
     * @param left  the left argument
     * @param right the right argument
     * @return the left, right or parent node
     */
    protected JexlNode findNullOperand(RuntimeException xrt, JexlNode node, Object left, Object right) {
        if (xrt instanceof ArithmeticException
                && JexlException.NULL_OPERAND == xrt.getMessage()) {
            if (left == null) {
                return node.jjtGetChild(0);
            }
            if (right == null) {
                return node.jjtGetChild(1);
            }
        }
        return node;
    }

    public boolean errorOnUndefinedVariable = true;

    /**
     * Triggered when variable can not be resolved.
     *
     * @param xjexl the JexlException ("undefined variable " + variable)
     * @return throws JexlException if strict, null otherwise
     */
    protected Object unknownVariable(JexlException xjexl) {
        if (errorOnUndefinedVariable) {
            throw xjexl;
        }
        if (!silent) {
            logger.warn(xjexl.getMessage());
        }
        return null;
    }

    /**
     * Triggered when method, function or constructor invocation fails.
     *
     * @param xjexl the JexlException wrapping the original error
     * @return throws JexlException if strict, null otherwise
     */
    protected Object invocationFailed(JexlException xjexl) {
        if (strict || xjexl instanceof JexlException.Return) {
            throw xjexl;
        }
        if (!silent) {
            logger.warn(xjexl.getMessage(), xjexl.getCause());
        }
        return null;
    }

    /**
     * Checks whether this interpreter execution was cancelled due to thread interruption.
     *
     * @return true if cancelled, false otherwise
     * @since 2.1
     */
    protected boolean isCancelled() {
        if (cancelled | Thread.interrupted()) {
            cancelled = true;
        }
        return cancelled;
    }

    boolean isEventing;
    String eventingPattern;
    Eventing eventing;

    /**
     * Resolves a namespace, eventually allocating an instance using context as constructor argument.
     * The lifetime of such instances span the current expression or script evaluation.
     *
     * @param prefix the prefix name (may be null for global namespace)
     * @param node   the AST node
     * @return the namespace instance
     */
    protected Object resolveNamespace(String prefix, JexlNode node) {
        // just resolve the prefix and all
        eventingPattern = "";
        isEventing = false;
        if (prefix != null) {
            isEventing = Eventing.EVENTS.matcher(prefix).matches();
            if (isEventing) {
                eventingPattern = prefix.substring(0, 2);
                prefix = prefix.substring(2);
            }
        }

        Object namespace = null;
        // check whether this namespace is a functor
        if (functors != null) {
            namespace = functors.get(prefix);
            if (namespace != null) {
                return namespace;
            }
        }
        // check if namespace if a resolver
        if (context instanceof NamespaceResolver) {
            namespace = ((NamespaceResolver) context).resolveNamespace(prefix);
        }
        if (namespace == null) {
            String methodName = "";
            if (prefix == null) {
                methodName = node.jjtGetChild(0).image;
            } else {
                methodName = node.jjtGetChild(1).image;
            }
            namespace = resolveScriptForFunction(prefix, methodName);
            if (namespace != null) {
                return namespace;
            }
            namespace = functions.get(prefix);
            if (prefix != null && namespace == null) {
                namespace = resolveJexlClassName(prefix);
                if ( namespace != null ) return namespace ;
                throw new JexlException(node, "no such function namespace " + prefix);
            }

            if (context.has(methodName)) {
                Object r = context.get(methodName);
                if (r instanceof ScriptMethod) {
                    return current;
                }
            }
        }
        // allow namespace to be instantiated as functor with context if possible, not an error otherwise
        if (namespace instanceof Class<?>) {
            Object[] args = new Object[]{context};
            JexlMethod ctor = uberspect.getConstructorMethod(namespace, args, node);
            if (ctor != null) {
                try {
                    namespace = ctor.invoke(namespace, args);
                    if (functors == null) {
                        functors = new HashMap<String, Object>();
                    }
                    functors.put(prefix, namespace);
                } catch (Exception xinst) {
                    throw new JexlException(node, "unable to instantiate namespace " + prefix, xinst);
                }
            }
        }
        return namespace;
    }

    @Override
    public Object visit(ASTImportStatement node, Object data) {
        String from = node.jjtGetChild(0).image;
        String as = node.jjtGetChild(1).image;
        try {
            if (functions.containsKey(as) || imports.containsKey(as)) {
                throw new Exception(String.format("[%s] is already taken as import name!", as));
            }
            try {
                Class<?> c = Class.forName(from);
                functions.put(as, c);
                context.set(as, c);
                return c;
            } catch (Exception e) {
                try {
                    //perhaps a field ?
                    String actClass = from.substring(0, from.lastIndexOf("."));
                    Object c = null;
                    if ( context.has(actClass)){
                        c = context.get(actClass);
                    }else{
                        c = Class.forName(actClass);
                    }

                    String fieldName = from.substring(from.lastIndexOf(".") + 1);
                    Object o = null;
                    if ( c instanceof Executable ){
                        o = ((Executable)c).get(fieldName);
                    }else {
                        JexlPropertyGet pg = uberspect.getPropertyGet(c, fieldName, null);
                        o = pg.invoke(null);
                    }
                    try {
                        functions.put(as, o);
                        context.set(as, o);
                        return o;
                    } catch (Exception set) {
                        throw new Exception("Exception setting up functions and context -->" +
                                " am I passed unmodifiable stupidity?");
                    }

                } catch (Exception ee) {
                    // safely ignore this...
                }
            }

            Script base;
            if (data == null) {
                // I am the starting interpreter,
                base = current;
            } else {
                base = (Script) data;
            }
            Script freshlyImported = jexlEngine.importScript(from, as, base);
            freshlyImported.setup(context);
            imports.put(as, freshlyImported);
            functions.put(as, freshlyImported);
            context.set(as, freshlyImported);
            return freshlyImported;

        } catch (Exception e) {
            JexlException jexlException = new JexlException(node, "Import Failed!", e);
            return invocationFailed(jexlException);
        }
    }

    @Override
    public Object visit(ASTParamDef node, Object data) {
        return null;
    }

    public static class NamedArgs {
        public final String name;
        public final Object value;

        public NamedArgs(String n, Object v) {
            name = n;
            value = v;
        }

    }

    @Override
    public Object visit(ASTArgDef node, Object data) {
        int numChild = node.jjtGetNumChildren();
        if (numChild == 1) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }
        NamedArgs na = new NamedArgs(node.jjtGetChild(0).image,
                node.jjtGetChild(1).jjtAccept(this, data));
        return na;
    }

    @Override
    public Object visit(ASTMethodDef node, Object data) {
        JexlNode n = node.jjtGetChild(0);
        if ( n instanceof ASTIdentifier ){
            ScriptMethod method = current.methods().get(n.image);
            if ( method == null ){
                //named inner method?
                ScriptMethod inner = new ScriptMethod(node,context);
                context.set(inner.name,inner);
                return inner;
            }
            return method ;
        }
        return null; // anonymous functions should not be there inside a main script?
    }

    @Override
    public Object visit(ASTExtendsDef node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTClassDef node, Object data) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTAdditiveNode node, Object data) {
        /**
         * The pattern for exception mgmt is to let the child*.jjtAccept
         * out of the try/catch loop so that if one fails, the ex will
         * traverse up to the interpreter.
         * In cases where this is not convenient/possible, JexlException must
         * be caught explicitly and rethrown.
         */
        JexlNode leftNode = node.jjtGetChild(0);
        Object left = leftNode.jjtAccept(this, data);
        for (int c = 2, size = node.jjtGetNumChildren(); c < size; c += 2) {
            Object right = node.jjtGetChild(c).jjtAccept(this, data);
            try {
                JexlNode op = node.jjtGetChild(c - 1);
                if (op instanceof ASTAdditiveOperator) {
                    String which = op.image;
                    if ("+".equals(which)) {
                        left = arithmetic.add(left, right);
                        continue;
                    }
                    if ("-".equals(which)) {
                        left = arithmetic.subtract(left, right);
                        continue;
                    }
                    if ("+=".equals(which)) {
                        left = arithmetic.add(left, right);
                        //assign
                        assignToNode(-1,node,leftNode,left);
                        continue;
                    }
                    if ("-=".equals(which)) {
                        left = arithmetic.subtract(left, right);
                        //assign
                        assignToNode(-1,node,leftNode,left);
                        continue;
                    }
                    throw new UnsupportedOperationException("unknown additive operator " + which);
                }
                throw new IllegalArgumentException("unknown non-additive operator " + op);
            } catch (ArithmeticException xrt) {
                JexlNode xnode = findNullOperand(xrt, node, left, right);
                throw new JexlException(xnode, "+/- error", xrt);
            }
        }
        return left;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTAdditiveOperator node, Object data) {
        throw new UnsupportedOperationException("Should not be called.");
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTAndNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            boolean leftValue = arithmetic.toBoolean(left);
            if (!leftValue) {
                return Boolean.FALSE;
            }
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt);
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            boolean rightValue = arithmetic.toBoolean(right);
            if (!rightValue) {
                return Boolean.FALSE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTArrayAccess node, Object data) {
        // first objectNode is the identifier
        Object object = node.jjtGetChild(0).jjtAccept(this, data);

        // can have multiple nodes - either an expression, integer literal or reference
        int numChildren = node.jjtGetNumChildren();
        for (int i = 1; i < numChildren; i++) {
            JexlNode nindex = node.jjtGetChild(i);
            if (nindex instanceof JexlNode.Literal<?>) {
                /*
                    BUG fix -- x[0] with x=null would not throw exception.
                    Now, it does.
                */
                if (object == null) {
                    throw new JexlException(node, "object is null");
                }
                object = nindex.jjtAccept(this, object);
            } else {
                Object index = nindex.jjtAccept(this, null);
                object = getAttribute(object, index, nindex);
            }
        }

        return object;
    }

    public Object visit(ASTArrayRange node, Object data) {
        // There should be a single map entry hence :
        Object start = node.jjtGetChild(0).jjtAccept(this, data);
        Object end = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            if (node.jjtGetNumChildren() == 3) {
                Object step = node.jjtGetChild(2).jjtAccept(this, data);
                return TypeUtility.range(end, start, step);
            }
            return TypeUtility.range(end, start);
        } catch (Exception e) {
            throw new JexlException(node, "Invalid Range!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTArrayLiteral node, Object data) {
        Object literal = node.getLiteral();
        if (literal == null) {
            int childCount = node.jjtGetNumChildren();
            Object[] array = new Object[childCount];
            for (int i = 0; i < childCount; i++) {
                Object entry = node.jjtGetChild(i).jjtAccept(this, data);
                array[i] = entry;
            }
            literal = arithmetic.narrowArrayType(array);
            node.setLiteral(literal);
        }
        return literal;
    }

    public Object assignToNode(int register, JexlNode node, JexlNode left, Object right) {
        // determine initial object & property:
        JexlNode objectNode = null;
        Object object = register >= 0 ? registers[register] : null;
        JexlNode propertyNode = null;
        Object property = null;
        boolean isVariable = true;
        int v = 0;
        StringBuilder variableName = null;
        // 1: follow children till penultimate, resolve dot/array
        int last = left.jjtGetNumChildren() - 1;
        // check we are not assigning a register itself
        boolean isRegister = last < 0 && register >= 0;
        // start at 1 if register
        for (int c = register >= 0 ? 1 : 0; c < last; ++c) {
            objectNode = left.jjtGetChild(c);
            // evaluate the property within the object
            object = objectNode.jjtAccept(this, object);
            if (object != null) {
                continue;
            }
            isVariable &= objectNode instanceof ASTIdentifier
                    || (objectNode instanceof ASTNumberLiteral && ((ASTNumberLiteral) objectNode).isInteger());
            // if we get null back as a result, check for an ant variable
            if (isVariable) {
                if (v == 0) {
                    variableName = new StringBuilder(left.jjtGetChild(0).image);
                    v = 1;
                }
                for (; v <= c; ++v) {
                    variableName.append('.');
                    variableName.append(left.jjtGetChild(v).image);
                }
                object = context.get(variableName.toString());
                // disallow mixing ant & bean with same root; avoid ambiguity
                if (object != null) {
                    isVariable = false;
                }
            } else {
                throw new JexlException(objectNode, "illegal assignment form");
            }
        }
        // 2: last objectNode will perform assignement in all cases
        propertyNode = isRegister ? null : left.jjtGetChild(last);
        boolean antVar = false;
        if (propertyNode instanceof ASTIdentifier) {
            ASTIdentifier identifier = (ASTIdentifier) propertyNode;
            register = identifier.getRegister();
            if (register >= 0) {
                isRegister = true;
            } else {
                property = identifier.image;
                antVar = true;
            }
        } else if (propertyNode instanceof ASTNumberLiteral && ((ASTNumberLiteral) propertyNode).isInteger()) {
            property = ((ASTNumberLiteral) propertyNode).getLiteral();
            antVar = true;
        } else if (propertyNode instanceof ASTArrayAccess) {
            // first objectNode is the identifier
            objectNode = propertyNode;
            ASTArrayAccess narray = (ASTArrayAccess) objectNode;
            Object nobject = narray.jjtGetChild(0).jjtAccept(this, object);
            if (nobject == null) {
                throw new JexlException(objectNode, "array element is null");
            } else {
                object = nobject;
            }
            // can have multiple nodes - either an expression, integer literal or
            // reference
            last = narray.jjtGetNumChildren() - 1;
            for (int i = 1; i < last; i++) {
                objectNode = narray.jjtGetChild(i);
                if (objectNode instanceof JexlNode.Literal<?>) {
                    object = objectNode.jjtAccept(this, object);
                } else {
                    Object index = objectNode.jjtAccept(this, null);
                    object = getAttribute(object, index, objectNode);
                }
            }
            property = narray.jjtGetChild(last).jjtAccept(this, null);
        } else if (!isRegister) {
            throw new JexlException(
                    objectNode != null ? objectNode : node,  // ensure we have non null markdown!
                    "illegal assignment form");
        }
        // deal with ant variable; set context
        if (isRegister) {
            registers[register] = right;
            return right;
        } else if (antVar) {
            if (isVariable && object == null) {
                if (variableName != null) {
                    if (last > 0) {
                        variableName.append('.');
                    }
                    variableName.append(property);
                    property = variableName.toString();
                }
                try {
                    context.set(String.valueOf(property), right);
                } catch (UnsupportedOperationException xsupport) {
                    throw new JexlException(node, "context is readonly", xsupport);
                }
                return right;
            }
        }
        if (property == null) {
            // no property, we fail
            throw new JexlException(propertyNode, "property is null");
        }
        if (object == null) {
            // no object, we fail
            throw new JexlException(objectNode, "bean is null");
        }
        // one before last, assign
        setAttribute(object, property, right, propertyNode);
        return right;
    }

    @Override
    public Object visit(ASTTagContainer node, Object data) {
        Object o = node.jjtGetChild(0).jjtAccept(this, data);
        return o;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTAssignment node, Object data) {
        // left contains the reference to assign to
        JexlNode left = node.jjtGetChild(0);
        JexlNode value = node.jjtGetChild(1);
        if (left instanceof ASTTuple) {
            return tupleAssignment((ASTTuple) left, value, data);
        }
        int register = -1;
        if (left instanceof ASTIdentifier) {
            ASTIdentifier var = (ASTIdentifier) left;
            register = var.getRegister();
            if (register < 0) {
                throw new JexlException(left, "unknown variable " + left.image);
            }
        } else if (!(left instanceof ASTReference)) {
            throw new JexlException(left, "illegal assignment form 0");
        }
        Object right = null;
        if (value instanceof ASTMethodDef) {
            // handle anonymous function and closures ...(?)
            right = new ScriptMethod((ASTMethodDef) value, context );
        } else {
            // right is the value expression to assign
            right = value.jjtAccept(this, data);
        }
        return assignToNode(register, node, left, right);
    }

    private Object[] tupleAssignment(ASTTuple astTuple, JexlNode value, Object data) {
        final int c = astTuple.jjtGetNumChildren();
        JexlNode errorNode = astTuple.jjtGetChild(c - 1);
        final JexlNode startNode = astTuple.jjtGetChild(0);
        final boolean catchError = errorNode instanceof ASTTagContainer;
        final boolean fromRight = startNode instanceof ASTTagContainer ;

        JexlNode left = null;
        int upto = c;
        if (catchError) {
            left = astTuple.jjtGetChild(c - 1).jjtGetChild(0);
            if (!(left instanceof ASTReference)) {
                throw new JexlException(left, "illegal assignment form 0");
            }
            errorNode = left;// re-assign
            upto = c - 1;
        }
        int start = 0;
        if ( fromRight ){
            start = 1 ;
            left = startNode.jjtGetChild(0);
        }

        for (int i = start ; i < upto; i++) {
            left = astTuple.jjtGetChild(i);
            if (!(left instanceof ASTReference)) {
                throw new JexlException(left, "illegal assignment form 0");
            }
        }
        Object result = null;
        Throwable error = null;
        synchronized (this) {
            boolean oldStrict = strict;
            try {
                strict = true;
                result = value.jjtAccept(this, data);
            } catch (Throwable t) {
                error = t;
                if (t.getCause() != null) {
                    error = t.getCause();
                }
            } finally {
                strict = oldStrict;
            }
        }
        JexlNode node = astTuple.jjtGetParent();
        ArrayList l = new ArrayList();
        if (error != null) {
            if ( fromRight ){
                left = startNode.jjtGetChild(0);
                assignToNode(-1, node, left, null);
                l.add(null);
            }
            for (int i = start; i < upto; i++) {
                left = astTuple.jjtGetChild(i);
                assignToNode(-1, node, left, null);
                l.add(null);
            }
        } else {
            if (catchError && upto == 1) {
                // (o,:e) error check only - no project
                assignToNode(-1, node, left, result);
                l.add(result);
            } else {
                // project
                List t = TypeUtility.combine(result);
                int s = t.size();
                if ( fromRight ){
                    int i = s - 1  ;
                    int ni = upto - 1;
                    while ( ni > 0 && i >= 0 ){
                        Object o = t.get(i);
                        left = astTuple.jjtGetChild(ni);
                        assignToNode(-1, node, left, o);
                        l.add(0,o);
                        i--;
                        ni--;
                    }
                    if ( i >= 0  ){
                        left = startNode.jjtGetChild(0);
                        Object o = t.get(i);
                        assignToNode(-1, node, left, o);
                        l.add(0,o);
                    }else{
                        left = startNode.jjtGetChild(0);
                        assignToNode(-1, node, left, null);
                        l.add(0,null);
                        for ( i = ni; i >= 1; i-- ){
                            left = astTuple.jjtGetChild(i);
                            assignToNode(-1, node, left, null);
                            l.add(0,null);
                        }
                    }

                }else {
                    for (int i = 0; i < upto; i++) {
                        left = astTuple.jjtGetChild(i);
                        Object o = null;
                        if (i < s) {
                            o = t.get(i);
                        }
                        assignToNode(-1, node, left, o);
                        l.add(o);
                    }
                }
            }
        }
        if (catchError) {
            assignToNode(-1, node, errorNode, error);
            l.add(error);
        }
        return l.toArray();
    }

    @Override
    public Object visit(ASTTuple node, Object data) {
        List l = TypeUtility.combine();
        int c = node.jjtGetNumChildren();
        for (int i = 0; i < c; i++) {
            Object r = node.jjtGetChild(i).jjtAccept(this, data);
            l.add(r);
        }
        return l.toArray();
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTBitwiseAndNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.bitwiseAnd(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "& error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTBitwiseComplNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            return arithmetic.bitwiseComplement(left);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "~ error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTBitwiseOrNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.bitwiseOr(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "| error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTBitwiseXorNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.bitwiseXor(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "^ error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTBlock node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            result = node.jjtGetChild(i).jjtAccept(this, data);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTBreakStatement node, Object data) {
        int c = node.jjtGetNumChildren();
        if (c == 0) {
            //raise BreakException at parent -- not it's own condition
            throw new JexlException.Break(node.jjtGetParent());
        }
        // conditional break
        Object value = node.jjtGetChild(0).jjtAccept(this, data);
        boolean b = TypeUtility.castBoolean(value, false);
        if (b) {
            if (c > 1) {
                value = node.jjtGetChild(1).jjtAccept(this, data);
                throw new JexlException.Break(node.jjtGetParent(), value);
            }
            // signifies breaking on condition, not returning value
            throw new JexlException.Break(node.jjtGetParent());
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTContinueStatement node, Object data) {
        int c = node.jjtGetNumChildren();
        if (c == 0) {
            //raise ContinueException at parent -- not it's own condition
            throw new JexlException.Continue(node.jjtGetParent());
        }
        // conditional continue
        Object value = node.jjtGetChild(0).jjtAccept(this, data);
        boolean b = TypeUtility.castBoolean(value, false);
        if (b) {
            if (c > 1) {
                value = node.jjtGetChild(1).jjtAccept(this, data);
                throw new JexlException.Continue(node.jjtGetParent(), value);
            }
            // signifies continue on condition, not returning value
            throw new JexlException.Continue(node.jjtGetParent());
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTDivNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.divide(left, right);
        } catch (ArithmeticException xrt) {
            if (!strict) {
                return new Double(0.0);
            }
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "divide error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTEmptyFunction node, Object data) {
        Object o = node.jjtGetChild(0).jjtAccept(this, data);
        if (o == null) {
            return Boolean.TRUE;
        }
        if (o instanceof String && "".equals(o)) {
            return Boolean.TRUE;
        }
        if (o.getClass().isArray() && ((Object[]) o).length == 0) {
            return Boolean.TRUE;
        }
        if (o instanceof Collection<?>) {
            return ((Collection<?>) o).isEmpty() ? Boolean.TRUE : Boolean.FALSE;
        }
        // Map isn't a collection
        if (o instanceof Map<?, ?>) {
            return ((Map<?, ?>) o).isEmpty() ? Boolean.TRUE : Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    protected Object findScriptObject(String name) {
        String[] arr = name.split(":");
        Object so = null;
        if (arr.length == 1) {
            name = arr[0].trim();
            so = current.classes().get(name);
            if (so != null) {
                return so;
            }
            so = current.methods().get(name);
            return so;
        }
        String ns = arr[0].trim();
        name = arr[1].trim();
        Script script = imports.get(ns);
        if (script == null) {
            return null;
        }
        so = script.classes().get(name);
        if (so != null) {
            return so;
        }
        so = script.methods().get(name);
        return so;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTDefinedFunction node, Object data) {
        try {

            JexlNode n = node.jjtGetChild(0).jjtGetChild(0);
            int num = n.jjtGetNumChildren();
            if (n instanceof ASTIdentifier && num == 1 ) {
                String varName = n.image;
                return context.has(varName);
            } else {
                // null is defined
                if ( num > 0 && n.jjtGetChild(0) instanceof ASTNullLiteral) {
                    return true;
                }
                // now here try to do slightly better
                Object o =  node.jjtGetChild(0).jjtAccept(this, data);
                if (o != null) return true ;
                o = n.jjtAccept(this, data);
                if (o instanceof String) {
                    // used to get the Function or class
                    String name = (String) o;
                    return findScriptObject(name);
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTISANode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            if (left == null) {
                return false;
            }
            if (left instanceof ScriptClassInstance) {
                return ((ScriptClassInstance) left).getNClass().isa(right);
            }
            Class r;
            if (right instanceof Class) {
                r = (Class) right;
            } else {
                r = right.getClass();
            }
            Class l;
            if (left instanceof Class) {
                l = (Class) left;
            } else {
                l = left.getClass();
            }

            return r.isAssignableFrom(l);

        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "isa error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTINNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return SetOperations.in(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "in error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTAEQNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {

            if (left == right) {
                return true;
            }
            if (left == null || right == null) {
                return false;
            }
            if (left instanceof ScriptClassInstance && right instanceof ScriptClassInstance) {
                if (((ScriptClassInstance) left).getNClass().equals(((ScriptClassInstance) right).getNClass())) {
                    return left.equals(right);
                }
                return false;
            }
            if (left.getClass().equals(right.getClass())) {
                return arithmetic.equals(left, right) ? Boolean.TRUE : Boolean.FALSE;
            }
            return false;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "=== error", xrt);
        }
    }


    /**
     * {@inheritDoc}
     */
    public Object visit(ASTEQNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.equals(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "== error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTFalseNode node, Object data) {
        return Boolean.FALSE;
    }


    /**
     * {@inheritDoc}
     */
    public Object visit(ASTForeachStatement node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTExpressionFor node, Object data) {
        if (node.jjtGetNumChildren() > 0) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }
        // in lieu of anything is...
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTForWithCondition node, Object data) {
        Object result = null;
        int numChild = node.jjtGetNumChildren();
        /* last node is the statement to execute */
        JexlNode statement = node.jjtGetChild(numChild - 1);
        // initialize the stuff
        node.jjtGetChild(0).jjtAccept(this, data);
        while (true) {
            // now the condition
            Object cond = node.jjtGetChild(1).jjtAccept(this, data);
            boolean condition = TypeUtility.castBoolean(cond, false);
            if (!condition) break;
            if (isCancelled()) {
                throw new JexlException.Cancel(node);
            }

            try {
                // execute statement
                result = statement.jjtAccept(this, data);
            } catch (JexlException.Break b) {
                result = b;
                break;
            } catch (JexlException.Continue c) {
                result = c;
                // even in here too ..
                node.jjtGetChild(2).jjtAccept(this, data);
                continue;
            }
            // last one
            node.jjtGetChild(2).jjtAccept(this, data);

        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTForWithIterator node, Object data) {
        Object result = null;
        /* first objectNode is the loop variable */
        ASTReference loopReference = (ASTReference) node.jjtGetChild(0);
        ASTIdentifier loopVariable = (ASTIdentifier) loopReference.jjtGetChild(0);
        int register = loopVariable.getRegister();
        /* second objectNode is the variable to iterate */
        Object iterableValue = node.jjtGetChild(1).jjtAccept(this, data);
        // make sure there is a value to iterate on and a statement to execute
        if (iterableValue != null && node.jjtGetNumChildren() >= 3) {
            /* third objectNode is the statement to execute */
            JexlNode statement = node.jjtGetChild(2);
            // get an iterator for the collection/array etc via the
            // introspector.
            Iterator<?> itemsIterator = uberspect.getIterator(iterableValue, node);
            if (itemsIterator != null) {
                while (itemsIterator.hasNext()) {
                    if (isCancelled()) {
                        throw new JexlException.Cancel(node);
                    }
                    // set loopVariable to value of iterator
                    Object value = itemsIterator.next();
                    if (register < 0) {
                        context.set(loopVariable.image, value);
                    } else {
                        registers[register] = value;
                    }
                    try {
                        // execute statement
                        result = statement.jjtAccept(this, data);
                    } catch (JexlException.Break b) {
                        result = b;
                        break;
                    } catch (JexlException.Continue c) {
                        result = c;
                        continue;
                    }
                }
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public Object visit(ASTGENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.greaterThanOrEqual(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, ">= error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTGTNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.greaterThan(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "> error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTERNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        if ( right == null ){
            if ( left == null ) return true ;
            return false ;
        }
        try {
            // use arithmetic / pattern matching ?
            if (right instanceof java.util.regex.Pattern || right instanceof String) {
                return arithmetic.matches(left, right) ? Boolean.TRUE : Boolean.FALSE;
            }
            // left in right ? <=> right.contains(left) ?
            // try contains on collection
            if (right instanceof Set<?>) {
                return ((Set<?>) right).contains(left) ? Boolean.TRUE : Boolean.FALSE;
            }
            // try contains on map key
            if (right instanceof Map<?, ?>) {
                return ((Map<?, ?>) right).containsKey(left) ? Boolean.TRUE : Boolean.FALSE;
            }
            if ( JexlArithmetic.areListOrArray(left,right )){
                return (TypeUtility.index(left, right) >= 0) ;
            }
            // try contains on collection
            if (right instanceof Collection<?>) {
                return ((Collection<?>) right).contains(left) ? Boolean.TRUE : Boolean.FALSE;
            }
            // try a contains method (duck type set)
            try {
                Object[] argv = {left};
                JexlMethod vm = uberspect.getMethod(right, "contains", argv, node);
                if (vm != null) {
                    return arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.TRUE : Boolean.FALSE;
                } else if (arithmetic.narrowArguments(argv)) {
                    vm = uberspect.getMethod(right, "contains", argv, node);
                    if (vm != null) {
                        return arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            } catch (InvocationTargetException e) {
                throw new JexlException(node, "=~ invocation error", e.getCause());
            } catch (Exception e) {
                throw new JexlException(node, "=~ error", e);
            }
            // try iterative comparison
            Iterator<?> it = uberspect.getIterator(right, node);
            if (it != null) {
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next == left || (next != null && next.equals(left))) {
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            }
            // defaults to equal
            return arithmetic.equals(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "=~ error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTIdentifier node, Object data) {
        if (isCancelled()) {
            throw new JexlException.Cancel(node);
        }
        String name = node.image;
        if (data == null) {
            int register = node.getRegister();
            if (register >= 0) {
                return registers[register];
            }
            Object value = context.get(name);
            if (value == null
                    && !(node.jjtGetParent() instanceof ASTReference)
                    && !context.has(name)
                    && !isTernaryProtected(node)) {
                JexlException xjexl = new JexlException.Variable(node, name);
                return unknownVariable(xjexl);
            }
            if (current != null) {
                // last try, is that a method name?
                if (current.methods().containsKey(name)) {
                    return current.methods().get(name);
                }
            }
            return value;
        } else {
            return getAttribute(data, name, node);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTVar node, Object data) {
        return visit((ASTIdentifier) node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTIfStatement node, Object data) {
        int n = 0;
        try {
            Object result = null;
            /* first objectNode is the condition */
            Object expression = node.jjtGetChild(0).jjtAccept(this, data);
            if (arithmetic.toBoolean(expression)) {
                // first objectNode is true statement
                n = 1;
                result = node.jjtGetChild(1).jjtAccept(this, data);
            } else {
                // if there is a false, execute it. false statement is the second
                // objectNode
                if (node.jjtGetNumChildren() == 3) {
                    n = 2;
                    result = node.jjtGetChild(2).jjtAccept(this, data);
                }
            }
            return result;
        } catch (JexlException error) {
            throw error;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(n), "if error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTNumberLiteral node, Object data) {
        if (data != null && node.isInteger()) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.getLiteral();
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTJexlScript node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            JexlNode child = node.jjtGetChild(i);
            try {
                result = child.jjtAccept(this, data);
            } catch (JexlException.Jump jump) {
                i = jump.location - 1;
                result = jump;
                continue;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTLENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.lessThanOrEqual(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "<= error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTLTNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.lessThan(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "< error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTMapEntry node, Object data) {
        Object key = node.jjtGetChild(0).jjtAccept(this, data);
        Object value = node.jjtGetChild(1).jjtAccept(this, data);
        return new Object[]{key, value};
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTMapLiteral node, Object data) {
        int childCount = node.jjtGetNumChildren();
        Map<Object, Object> map = new HashMap<Object, Object>();

        for (int i = 0; i < childCount; i++) {
            Object[] entry = (Object[]) (node.jjtGetChild(i)).jjtAccept(this, data);
            map.put(entry[0], entry[1]);
        }

        return map;
    }

    /**
     * The class to handle anonymous method calls
     */
    public static class AnonymousParam implements Runnable {

        // the underlying interpreter
        public Interpreter interpreter;

        // the method block
        public ASTBlock block;

        /**
         * Constructs a anonymous parameter
         *
         * @param i     the interpreter
         * @param block the block
         */
        public AnonymousParam(Interpreter i, ASTBlock block) {
            this.interpreter = i;
            this.block = block;
        }

        /**
         * Sets the iteration context
         *
         * @param con context of iteration, the whole object, in case of a collection the collection
         * @param o   the individual object, elements of the collection
         * @param i   the index - of the element in the collection
         */
        public void setIterationContext(Object con, Object o, Object i) {
            interpreter.context.set(Script._CONTEXT_, con);
            interpreter.context.set(Script._ITEM_, o);
            interpreter.context.set(Script._INDEX_, i);
        }

        /**
         * Sets the iteration context with partial updated result
         *
         * @param con context of iteration, the whole object, in case of a collection the collection
         * @param o   the individual object, elements of the collection
         * @param i   the index - of the element in the collection
         * @param p   the partial result as of current iteration
         */
        public void setIterationContextWithPartial(Object con, Object o, Object i, Object p) {
            setIterationContext(con, o, i);
            interpreter.context.set(Script._PARTIAL_, p);
        }

        /**
         * Cleans up the context by removing all context variables
         */
        public void removeIterationContext() {
            interpreter.context.remove(Script._CONTEXT_);
            interpreter.context.remove(Script._ITEM_);
            interpreter.context.remove(Script._INDEX_);
            interpreter.context.remove(Script._PARTIAL_);
        }

        /**
         * Executes the anonymous function
         *
         * @return the result of the call
         */
        public Object execute() {
            try {
                Object ret = block.jjtAccept(interpreter, null);
                return ret;
            } catch (JexlException.Return r) {
                return r.getValue();
            } catch (JexlException.Break b) {
                // important to return itself...
                return b;
            } catch (JexlException.Continue c) {
                // important to return itself...
                return c;
            }
        }

        /**
         * Gets a variable
         *
         * @param name of the variable
         * @return variable value on the running context
         */
        public Object getVar(String name) {
            return interpreter.context.get(name);
        }

        @Override
        public void run() {
            try {
                execute();
            } finally {
                removeIterationContext();
            }
        }
    }

    /**
     * Calls a method (or function).
     * <p>
     * Method resolution is a follows:
     * 1 - attempt to find a method in the bean passed as parameter;
     * 2 - if this fails, narrow the arguments and try again
     * 3 - if this still fails, seeks a Script or JexlMethod as a property of that bean.
     * </p>
     *
     * @param node       the method node
     * @param bean       the bean this method should be invoked upon
     * @param methodNode the node carrying the method name
     * @param argb       the first argument index, child of the method node
     * @return the result of the method invocation
     */
    private Object call(JexlNode node, Object bean, ASTIdentifier methodNode, int argb) {
        if (isCancelled()) {
            throw new JexlException.Cancel(node);
        }
        String methodName = methodNode.image;

        // evaluate the arguments
        int argc = node.jjtGetNumChildren() - argb;
        Object[] argv = new Object[argc];

        if (argc > 0) {
            SimpleNode n = node.jjtGetChild(argb);
            if (n instanceof ASTBlock) {
            /* the anonymous function is passed here.
                Thus, we should pass this as argument
            */
                argv[0] = new AnonymousParam(this, (ASTBlock) n);
            } else {
                argv[0] = n.jjtAccept(this, null);
            }
            for (int i = 1; i < argc; i++) {
                argv[i] = node.jjtGetChild(i + argb).jjtAccept(this, null);
            }
            // if __args__ expansion was used?
            if (n.jjtGetNumChildren() > 0 && n.jjtGetChild(0).jjtGetNumChildren() > 0) {
                if (Script.ARGS.equals(n.jjtGetChild(0).jjtGetChild(0).image)) {
                    argv = (Object[]) argv[0];
                }
            }
        }
        JexlException xjexl = null;
        // save eventing states, when we do not have a stack
        boolean wasEventing = isEventing;
        Eventing curEventing = null;
        Eventing.Event event = null;

        if (isEventing) {
            eventing = getEventing(bean);
            curEventing = eventing; // set it here
            event = new Eventing.Event(eventingPattern, methodName, argv);
            eventing.before(event);
        }
        try {
            // attempt to reuse last executor cached in volatile JexlNode.value
            if (cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlMethod) {
                    JexlMethod me = (JexlMethod) cached;
                    Object eval = me.tryInvoke(methodName, bean, argv);
                    if (!me.tryFailed(eval)) {
                        return eval;
                    }
                }
            }
            boolean cacheable = cache;
            if (bean instanceof Executable) {
                try {
                    return ((Executable) bean).execMethod(methodName, argv);
                }catch (Throwable e){
                    if ( e.getCause() instanceof NoSuchMethodException ){
                        // continue
                    }else{
                        throw e;
                    }
                }
            }

            JexlMethod vm = uberspect.getMethod(bean, methodName, argv, node);
            // DG: If we can't find an exact match, narrow the parameters and try again
            if (vm == null) {
                /* Intercept it here,
                 if possible
                 */
                Boolean[] success = new Boolean[1];
                Object ret = TypeUtility.interceptCastingCall(methodName, argv, success);
                if (success[0]) {
                    return ret;
                }
                if (arithmetic.narrowArguments(argv)) {
                    vm = uberspect.getMethod(bean, methodName, argv, node);
                }
                if (vm == null) {
                    Object functor = null;
                    // could not find a method, try as a var
                    if (bean == context) {
                        int register = methodNode.getRegister();
                        if (register >= 0) {
                            functor = registers[register];
                        } else {
                            functor = context.get(methodName);
                        }
                    } else {
                        JexlPropertyGet gfunctor = uberspect.getPropertyGet(bean, methodName, node);
                        if (gfunctor != null) {
                            functor = gfunctor.tryInvoke(bean, methodName);
                        }
                    }
                    // script of jexl method will do
                    if (functor instanceof Script) {
                        return ((Script) functor).execute(context, argv.length > 0 ? argv : null);
                    } else if (functor instanceof JexlMethod) {
                        vm = (JexlMethod) functor;
                        cacheable = false;
                    } else if ( functor instanceof ScriptMethod ){
                        return ((ScriptMethod)functor).invoke(bean,this,argv);
                    }else if ( functor instanceof Executable ){
                        return ((Executable)functor).execMethod(methodName,argv);
                    } else {
                        xjexl = new JexlException.Method(node, methodName);
                    }
                }
            }
            if (xjexl == null) {
                // vm cannot be null if xjexl is null
                Object eval = vm.invoke(bean, argv);
                // cache executor in volatile JexlNode.value
                if (cacheable && vm.isCacheable()) {
                    node.jjtSetValue(vm);
                }
                return eval;
            }
        } catch (InvocationTargetException e) {
            xjexl = new JexlException(node, "method invocation error : '" + methodName + "'", e.getCause());
        } catch (JexlException.Return | JexlException.Cancel e) {
            throw e;
        } catch (Exception e) {
            xjexl = new JexlException(node, "method '" + methodName + "' in error", e);
        } finally {
            if (wasEventing) {
                curEventing.after(event);
            }
        }
        return invocationFailed(xjexl);
    }

    Eventing getEventing(Object object) {
        if (object instanceof Eventing) {
            return ((Eventing) object);
        }
        return Eventing.Timer.TIMER;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTMethodNode node, Object data) {
        isEventing = false;
        // the object to invoke the method on should be in the data argument
        if (data == null) {
            // if the method node is the first child of the (ASTReference) parent,
            // it is considered as calling a 'top level' function
            JexlNode firstChild = node.jjtGetParent().jjtGetChild(0);
            if (firstChild == node) {
                data = resolveNamespace(null, node);
                if (data == null) {
                    data = context;
                }
            } else {
                // OK, we may have @@|$$ etc ... so?
                if (firstChild.image != null) {
                    isEventing = Eventing.EVENTS.matcher(firstChild.image).matches();
                    if (isEventing) {
                        eventingPattern = firstChild.image.substring(0, 2);
                        String objName = firstChild.image.substring(2);
                        data = context.get(objName);
                    }
                } else {
                    throw new JexlException(node, "attempting to call method on null");
                }
            }
        }
        // objectNode 0 is the identifier (method name), the others are parameters.
        ASTIdentifier methodNode = (ASTIdentifier) node.jjtGetChild(0);
        return call(node, data, methodNode, 1);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTFunctionNode node, Object data) {
        // objectNode 0 is the prefix
        String prefix = node.jjtGetChild(0).image;
        Object namespace = resolveNamespace(prefix, node);
        // objectNode 1 is the identifier , the others are parameters.
        ASTIdentifier functionNode = (ASTIdentifier) node.jjtGetChild(1);
        return call(node, namespace, functionNode, 2);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTConstructorNode node, Object data) {
        if (isCancelled()) {
            throw new JexlException.Cancel(node);
        }
        // first child is class or class name
        Object cobject = node.jjtGetChild(0).jjtAccept(this, data);
        // get the ctor args
        int argc = node.jjtGetNumChildren() - 1;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + 1).jjtAccept(this, null);
        }

        JexlException xjexl = null;
        try {
            // attempt to reuse last constructor cached in volatile JexlNode.value
            if (cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlMethod) {
                    JexlMethod mctor = (JexlMethod) cached;
                    Object eval = mctor.tryInvoke(null, cobject, argv);
                    if (!mctor.tryFailed(eval)) {
                        return eval;
                    }
                }
            }
            if (cobject instanceof String) {
                ScriptClass scriptClass = resolveJexlClassName((String) cobject);
                if (scriptClass != null) {
                    return scriptClass.instance(this, argv);
                }
            }
            if (cobject instanceof ScriptClass) {
                ScriptClass scriptClass = ((ScriptClass) cobject);
                return scriptClass.instance(this, argv);
            }
            JexlMethod ctor = uberspect.getConstructorMethod(cobject, argv, node);
            // DG: If we can't find an exact match, narrow the parameters and try again
            if (ctor == null) {
                if (arithmetic.narrowArguments(argv)) {
                    ctor = uberspect.getConstructorMethod(cobject, argv, node);
                }
                if (ctor == null) {
                    xjexl = new JexlException.Method(node, cobject.toString());
                }
            }
            if (xjexl == null) {
                Object instance = ctor.invoke(cobject, argv);
                // cache executor in volatile JexlNode.value
                if (cache && ctor.isCacheable()) {
                    node.jjtSetValue(ctor);
                }
                return instance;
            }
        } catch (InvocationTargetException e) {
            xjexl = new JexlException(node, "constructor invocation error", e.getCause());
        } catch (Exception e) {
            xjexl = new JexlException(node, "constructor error", e);
        }
        return invocationFailed(xjexl);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTModNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.mod(left, right);
        } catch (ArithmeticException xrt) {
            if (!strict) {
                return new Double(0.0);
            }
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "% error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTPowNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.power(left, right);
        } catch (ArithmeticException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "** error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTMulNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.multiply(left, right);
        } catch (ArithmeticException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "* error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTNENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.equals(left, right) ? Boolean.FALSE : Boolean.TRUE;
        } catch (ArithmeticException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "!= error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTNRNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            if (right instanceof java.util.regex.Pattern || right instanceof String) {
                // use arithmetic / pattern matching
                return arithmetic.matches(left, right) ? Boolean.FALSE : Boolean.TRUE;
            }
            // try contains on collection
            if (right instanceof Set<?>) {
                return ((Set<?>) right).contains(left) ? Boolean.FALSE : Boolean.TRUE;
            }
            // try contains on map key
            if (right instanceof Map<?, ?>) {
                return ((Map<?, ?>) right).containsKey(left) ? Boolean.FALSE : Boolean.TRUE;
            }
            // try contains on collection
            if (right instanceof Collection<?>) {
                return ((Collection<?>) right).contains(left) ? Boolean.FALSE : Boolean.TRUE;
            }
            // try a contains method (duck type set)
            try {
                Object[] argv = {left};
                JexlMethod vm = uberspect.getMethod(right, "contains", argv, node);
                if (vm != null) {
                    return arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.FALSE : Boolean.TRUE;
                } else if (arithmetic.narrowArguments(argv)) {
                    vm = uberspect.getMethod(right, "contains", argv, node);
                    if (vm != null) {
                        return arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.FALSE : Boolean.TRUE;
                    }
                }
            } catch (InvocationTargetException e) {
                throw new JexlException(node, "!~ invocation error", e.getCause());
            } catch (Exception e) {
                throw new JexlException(node, "!~ error", e);
            }
            // try iterative comparison
            Iterator<?> it = uberspect.getIterator(right, node.jjtGetChild(1));
            if (it != null) {
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next == left || (next != null && next.equals(left))) {
                        return Boolean.FALSE;
                    }
                }
                return Boolean.TRUE;
            }
            // defaults to not equal
            return arithmetic.equals(left, right) ? Boolean.FALSE : Boolean.TRUE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "!~ error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTNotNode node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);
        return arithmetic.toBoolean(val) ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTNullLiteral node, Object data) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTOrNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            boolean leftValue = arithmetic.toBoolean(left);
            if (leftValue) {
                return Boolean.TRUE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt);
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            boolean rightValue = arithmetic.toBoolean(right);
            if (rightValue) {
                return Boolean.TRUE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
        }
        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTReference node, Object data) {
        // could be array access, identifier or map literal
        // followed by zero or more ("." and array access, method, size,
        // identifier or integer literal)
        int numChildren = node.jjtGetNumChildren();
        // pass first piece of data in and loop through children
        Object result = null;
        StringBuilder variableName = null;
        String propertyName = null;
        boolean isVariable = true;
        int v = 0;
        for (int c = 0; c < numChildren; c++) {
            if (isCancelled()) {
                throw new JexlException.Cancel(node);
            }
            JexlNode theNode = node.jjtGetChild(c);
            // integer literals may be part of an antish var name only if no bean was found so far
            if (result == null && theNode instanceof ASTNumberLiteral && ((ASTNumberLiteral) theNode).isInteger()) {
                isVariable &= v > 0;
            } else {
                isVariable &= (theNode instanceof ASTIdentifier);
                result = theNode.jjtAccept(this, result);
            }
            // if we get null back a result, check for an ant variable
            if (result == null && isVariable) {
                if (v == 0) {
                    variableName = new StringBuilder(node.jjtGetChild(0).image);
                    v = 1;
                }
                for (; v <= c; ++v) {
                    variableName.append('.');
                    variableName.append(node.jjtGetChild(v).image);
                }
                result = context.get(variableName.toString());
            } else {
                propertyName = theNode.image;
            }
        }
        if (result == null) {
            if (isVariable && !isTernaryProtected(node)
                    // variable unknow in context and not (from) a register
                    && !(context.has(variableName.toString())
                    || (numChildren == 1
                    && node.jjtGetChild(0) instanceof ASTIdentifier
                    && ((ASTIdentifier) node.jjtGetChild(0)).getRegister() >= 0))) {
                JexlException xjexl = propertyName != null
                        ? new JexlException.Property(node, propertyName)
                        : new JexlException.Variable(node, variableName.toString());
                return unknownVariable(xjexl);
            }
        }
        if (result instanceof Null) {
            return null;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.1
     */
    public Object visit(ASTReferenceExpression node, Object data) {
        ASTArrayAccess upper = node;
        return visit(upper, data);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.1
     */
    public Object visit(ASTReturnStatement node, Object data) {
        Object val = NULL;
        if (node.jjtGetNumChildren() > 0) {
            val = node.jjtGetChild(0).jjtAccept(this, data);
        }
        throw new JexlException.Return(node, null, val);
    }

    /**
     * Check if a null evaluated expression is protected by a ternary expression.
     * The rationale is that the ternary / elvis expressions are meant for the user to explictly take
     * control over the error generation; ie, ternaries can return null even if the engine in strict mode
     * would normally throw an exception.
     *
     * @param node the expression node
     * @return true if nullable variable, false otherwise
     */
    private boolean isTernaryProtected(JexlNode node) {
        for (JexlNode walk = node.jjtGetParent(); walk != null; walk = walk.jjtGetParent()) {
            if (walk instanceof ASTTernaryNode) {
                return true;
            } else if (!(walk instanceof ASTReference || walk instanceof ASTArrayAccess)) {
                break;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTSizeFunction node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);
        if (val == null) {
            return -1; // should be fine?
        }
        return Integer.valueOf(sizeOf(node, val));
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTSizeMethod node, Object data) {
        return Integer.valueOf(sizeOf(node, data));
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTStringLiteral node, Object data) {
        if (data != null) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.image;
    }

    public static final Pattern CURRY_PATTERN = Pattern.compile("\\#\\{(?<expr>[^\\{^\\}]*)\\}",
            Pattern.MULTILINE);

    Script current;

    protected Object curry(String toBeCurried) {
        String ret = toBeCurried;
        Matcher m = CURRY_PATTERN.matcher(ret);
        while (m.find()) {
            String expression = m.group("expr");
            try {
                Script curry = jexlEngine.createCopyScript(expression, current);
                Object o = curry.execute(context);
                //fancy to avoid null check -- 'null' comes
                String replaceWith = String.format("%s", o);
                String replaceThis = m.group(0);
                // because there can be strings using regex, so no regex replace
                ret = ret.replace(replaceThis, replaceWith);
                m = CURRY_PATTERN.matcher(ret);
            } catch (Exception e) {
                // return whatever we could...
                return ret;
            }
        }
        // now, in here, finally execute all ....
        try {
            Script curry = jexlEngine.createCopyScript(ret, current);
            Object c = curry.execute(context);
            return c;
        } catch (Exception e) {
            // return whatever got substituted as of now...
            return ret;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTCurryingLiteral node, Object data) {
        return curry(node.image);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTTernaryNode node, Object data) {
        Object condition = node.jjtGetChild(0).jjtAccept(this, data);
        if (node.jjtGetNumChildren() == 3) {
            if (condition != null && arithmetic.toBoolean(condition)) {
                return node.jjtGetChild(1).jjtAccept(this, data);
            } else {
                return node.jjtGetChild(2).jjtAccept(this, data);
            }
        }
        if (condition != null && arithmetic.toBoolean(condition)) {
            return condition;
        } else {
            return node.jjtGetChild(1).jjtAccept(this, data);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTNullCoalesce node, Object data) {
        Object condition = null ;
        try {
            condition = node.jjtGetChild(0).jjtAccept(this, data);
        }catch (Exception e){
            // do nothing...
        }
        if (condition != null) {
            return condition;
        } else {
            return node.jjtGetChild(1).jjtAccept(this, data);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTTrueNode node, Object data) {
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTUnarySizeNode node, Object data) {
        JexlNode valNode = node.jjtGetChild(0);
        Object val = valNode.jjtAccept(this, data);
        if (val == null) return 0; // null gets a modulus of 0
        try {
            try {
                return sizeOf(node, val);
            } catch (Exception e) {
                if (arithmetic.compare(val, 0, ">=") < 0) {
                    return arithmetic.negate(val);
                } else {
                    return val;
                }
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(valNode, "arithmetic modulus error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTUnaryMinusNode node, Object data) {
        JexlNode valNode = node.jjtGetChild(0);
        Object val = valNode.jjtAccept(this, data);
        try {
            Object number = arithmetic.negate(val);
            // attempt to recoerce to literal class
            if (valNode instanceof ASTNumberLiteral && arithmetic.isNumberable(number)) {
                number = arithmetic.narrowNumber((Number) number, ((ASTNumberLiteral) valNode).getLiteralClass());
            }
            return number;
        } catch (ArithmeticException xrt) {
            throw new JexlException(valNode, "arithmetic error", xrt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTWhileStatement node, Object data) {
        Object result = null;
        /* first objectNode is the expression */
        Node expressionNode = node.jjtGetChild(0);
        while (arithmetic.toBoolean(expressionNode.jjtAccept(this, data))) {
            if (isCancelled()) {
                throw new JexlException.Cancel(node);
            }
            // execute statement
            if (node.jjtGetNumChildren() > 1) {
                try {
                    result = node.jjtGetChild(1).jjtAccept(this, data);
                } catch (JexlException.Continue c) {
                    result = c;
                    continue;
                } catch (JexlException.Break b) {
                    result = b;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTWhereStatement node, Object data) {
        /* first objectNode is the expression */
        Node expressionNode = node.jjtGetChild(0);
        if (arithmetic.toBoolean(expressionNode.jjtAccept(this, data))) {
            if (isCancelled()) {
                throw new JexlException.Cancel(node);
            }
            // execute statement
            if (node.jjtGetNumChildren() > 1) {
                node.jjtGetChild(1).jjtAccept(this, data);
            }
            return true;
        }
        return false;
    }

    /**
     * Calculate the <code>size</code> of various types: Collection, Array,
     * Map, String, and anything that has a int size() method.
     *
     * @param node the node that gave the value to size
     * @param val  the object to get the size of.
     * @return the size of val
     */
    private int sizeOf(JexlNode node, Object val) {
        if (val instanceof Collection<?>) {
            return ((Collection<?>) val).size();
        } else if (val.getClass().isArray()) {
            return Array.getLength(val);
        } else if (val instanceof Map<?, ?>) {
            return ((Map<?, ?>) val).size();
        } else if (val instanceof String) {
            return ((String) val).length();
        } else {
            // check if there is a size method on the object that returns an
            // integer and if so, just use it
            Object[] params = new Object[0];
            JexlMethod vm = uberspect.getMethod(val, "size", EMPTY_PARAMS, node);
            if (vm != null && vm.getReturnType() == Integer.TYPE) {
                Integer result;
                try {
                    result = (Integer) vm.invoke(val, params);
                } catch (Exception e) {
                    throw new JexlException(node, "size() : error executing", e);
                }
                return result.intValue();
            }
            throw new JexlException(node, "size() : unsupported type : " + val.getClass(), null);
        }
    }

    /**
     * Gets an attribute of an object.
     *
     * @param object    to retrieve value from
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *                  key for a map
     * @return the attribute value
     */
    public Object getAttribute(Object object, Object attribute) {
        return getAttribute(object, attribute, null);
    }


    /**
     * Shows that result was a valid null
     */
    public static class Null {
    }

    public static final Null NULL = new Null();

    /**
     * Gets an attribute of an object.
     *
     * @param object    to retrieve value from
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *                  key for a map
     * @param node      the node that evaluated as the object
     * @return the attribute value
     */
    protected Object getAttribute(Object object, Object attribute, JexlNode node) {
        if (object == null) {
            throw new JexlException(node, "object is null");
        }
        if (isCancelled()) {
            throw new JexlException.Cancel(node);
        }
        // attempt to reuse last executor cached in volatile JexlNode.value
        if (node != null && cache) {
            Object cached = node.jjtGetValue();
            if (cached instanceof JexlPropertyGet) {
                JexlPropertyGet vg = (JexlPropertyGet) cached;
                Object value = vg.tryInvoke(object, attribute);
                if (!vg.tryFailed(value)) {
                    return value;
                }
            }
        }

        JexlPropertyGet vg = uberspect.getPropertyGet(object, attribute, node);
        if (vg != null) {
            try {
                Object value = null;
                Exception ex = null;
                try {
                    value = vg.invoke(object);
                } catch (Exception e) {
                    ex = e;
                }
                // before we do something check for once if the field value is that?
                if (value == null) {
                    Field field = ((UberspectImpl) uberspect).getField(object, attribute.toString(), null);
                    if (field != null) {
                        JexlPropertyGet fg = new UberspectImpl.FieldPropertyGet(field);
                        value = fg.invoke(object);
                        vg = fg;
                        if (value == null) {
                            // the actual value of the property is null !
                            return NULL;
                        }
                    } else {
                        if (ex == null) {
                            // the actual value of the property is null!
                            return NULL;
                        }
                        // null field, raise ex
                        throw ex;
                    }
                }
                // cache executor in volatile JexlNode.value
                if (node != null && cache && vg.isCacheable()) {
                    node.jjtSetValue(vg);
                }
                return value;
            } catch (Exception xany) {
                if (node == null) {
                    throw new RuntimeException(xany);
                } else {
                    JexlException xjexl = new JexlException.Property(node, attribute.toString());
                    if (strict) {
                        throw xjexl;
                    }
                    if (!silent) {
                        logger.warn(xjexl.getMessage());
                    }
                }
            }
        } else {
            // Special casing for 'length' for Array.
            if (object.getClass().isArray() && attribute.equals("length")) {
                return Array.getLength(object);
            }
            // ok, is this a method with void input?
            JexlMethod jexlMethod = uberspect.getMethod(object, attribute.toString(), null, null);
            if (jexlMethod != null) {
                try {
                    Object ret = jexlMethod.invoke(object, null);
                    return ret;
                } catch (Exception e) {

                }
            }
            // is this a range stuff?
            if ( attribute instanceof RangeIterator ){
                return ((RangeIterator)attribute).splice(object);
            }

        }
        return null;
    }

    /**
     * Sets an attribute of an object.
     *
     * @param object    to set the value to
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *                  key for a map
     * @param value     the value to assign to the object's attribute
     */
    public void setAttribute(Object object, Object attribute, Object value) {
        setAttribute(object, attribute, value, null);
    }

    /**
     * Sets an attribute of an object.
     *
     * @param object    to set the value to
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *                  key for a map
     * @param value     the value to assign to the object's attribute
     * @param node      the node that evaluated as the object
     */
    protected void setAttribute(Object object, Object attribute, Object value, JexlNode node) {
        if (isCancelled()) {
            throw new JexlException.Cancel(node);
        }
        // attempt to reuse last executor cached in volatile JexlNode.value
        if (node != null && cache) {
            Object cached = node.jjtGetValue();
            if (cached instanceof JexlPropertySet) {
                JexlPropertySet setter = (JexlPropertySet) cached;
                Object eval = setter.tryInvoke(object, attribute, value);
                if (!setter.tryFailed(eval)) {
                    return;
                }
            }
        }
        JexlException xjexl = null;
        JexlPropertySet vs = uberspect.getPropertySet(object, attribute, value, node);
        // if we can't find an exact match, narrow the value argument and try again
        if (vs == null) {
            // replace all numbers with the smallest type that will fit
            Object[] narrow = {value};
            if (arithmetic.narrowArguments(narrow)) {
                vs = uberspect.getPropertySet(object, attribute, narrow[0], node);
            }
        }
        if (vs != null) {
            try {
                // cache executor in volatile JexlNode.value
                vs.invoke(object, value);
                if (node != null && cache && vs.isCacheable()) {
                    node.jjtSetValue(vs);
                }
                return;
            } catch (RuntimeException xrt) {
                if (node == null) {
                    throw xrt;
                }
                xjexl = new JexlException(node, "set object property error", xrt);
            } catch (Exception xany) {
                if (node == null) {
                    throw new RuntimeException(xany);
                }
                xjexl = new JexlException(node, "set object property error", xany);
            }
        }
        if (xjexl == null) {
            if (node == null) {
                String error = "unable to set object property"
                        + ", class: " + object.getClass().getName()
                        + ", property: " + attribute
                        + ", argument: " + value.getClass().getSimpleName();
                throw new UnsupportedOperationException(error);
            }
            xjexl = new JexlException.Property(node, attribute.toString());
        }
        if (strict) {
            throw xjexl;
        }
        if (!silent) {
            logger.warn(xjexl.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTGoToStatement node, Object data) {
        boolean jump = true;
        if (node.jjtGetNumChildren() > 1) {
            Object o = node.jjtGetChild(1).jjtAccept(this, data);
            jump = TypeUtility.castBoolean(o, false);
        }
        if (!jump) return new JexlException.Jump(node);
        String label = node.jjtGetChild(0).image;
        // find ASTLabelledStatement --> and fire it...
        Map<String, Integer> jumps = current.jumps();
        Integer loc = jumps.get(label);

        if (loc == null) {
            throw new JexlException(node, "Invalid Jump Label : " + label);
        }
        throw new JexlException.Jump(node, loc);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTLabelledStatement node, Object data) {
        return node.jjtGetChild(1).jjtAccept(this, data);
    }

    /**
     * Unused, satisfy ParserVisitor interface.
     *
     * @param node a node
     * @param data the data
     * @return does not return
     */
    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unused, should throw in Parser.
     *
     * @param node a node
     * @param data the data
     * @return does not return
     */
    public Object visit(ASTAmbiguous node, Object data) {
        throw new UnsupportedOperationException("unexpected type of node");
    }
}
