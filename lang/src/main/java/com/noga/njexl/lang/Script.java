/**
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

import com.noga.njexl.lang.extension.oop.ScriptClass;
import com.noga.njexl.lang.extension.oop.ScriptMethod;
import com.noga.njexl.lang.parser.ASTImportStatement;
import com.noga.njexl.lang.parser.ASTJexlScript;
import com.noga.njexl.lang.parser.ASTLabelledStatement;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * <p>A JEXL Script.</p>
 * <p>A script is some valid JEXL syntax to be executed with
 * a given set of {@link JexlContext} variables.</p>
 * <p>A script is a group of statements, separated by semicolons.</p>
 * <p>The statements can be <code>blocks</code> (curly braces containing code),
 * Control statements such as <code>if</code> and <code>while</code>
 * as well as expressions and assignment statements.</p>
 *
 * @since 1.1
 */
public interface Script {

    String SELF = "my";

    String DEFAULT_IMPORT_NAME = "JexlMain";

    String MY_MAIN_I = "__me__" ;

    Pattern DEFAULT_NAME_MATCH = Pattern.compile(".*\\.j(e)?xl$", Pattern.CASE_INSENSITIVE);

    String RELATIVE = "_";

    String ARGS = "__args__";
    String _ITEM_ = "$";
    String _CONTEXT_ = "$$";
    String _INDEX_ = "_";
    String _PARTIAL_ = "_$_";


    /**
     * Executes the script with the variables contained in the
     * supplied {@link JexlContext}.
     *
     * @param context A JexlContext containing variables.
     * @return The result of this script, usually the result of
     * the last statement.
     */
    Object execute(JexlContext context);

    /**
     * Executes the script with the variables contained in the
     * supplied {@link JexlContext} and a set of arguments corresponding to the
     * parameters used during parsing.
     *
     * @param context A JexlContext containing variables.
     * @param args    the arguments
     * @return The result of this script, usually the result of
     * the last statement.
     * @since 2.1
     */
    Object execute(JexlContext context, Object... args);

    /**
     * Returns the text of this Script.
     *
     * @return The script to be executed.
     */
    String getText();

    /**
     * Gets this script parameters.
     *
     * @return the parameters or null
     * @since 2.1
     */
    String[] getParameters();

    /**
     * Gets this script local variables.
     *
     * @return the local variables or null
     * @since 2.1
     */
    String[] getLocalVariables();

    /**
     * Gets this script variables.
     * <p>Note that since variables can be in an ant-ish form (ie foo.bar.quux), each variable is returned as
     * a list of strings where each entry is a fragment of the variable ({"foo", "bar", "quux"} in the example.</p>
     *
     * @return the variables or null
     * @since 2.1
     */
    Set<List<String>> getVariables();

    /**
     * Creates a Callable from this script.
     * <p>This allows to submit it to an executor pool and provides support for asynchronous calls.</p>
     * <p>The interpreter will handle interruption/cancellation gracefully if needed.</p>
     *
     * @param context the context
     * @return the callable
     * @since 2.1
     */
    Callable<Object> callable(JexlContext context);

    /**
     * Creates a Callable from this script.
     * <p>This allows to submit it to an executor pool and provides support for asynchronous calls.</p>
     * <p>The interpreter will handle interruption/cancellation gracefully if needed.</p>
     *
     * @param context the context
     * @param args    the script arguments
     * @return the callable
     * @since 2.1
     */
    Callable<Object> callable(JexlContext context, Object... args);

    /**
     * Necessary to import the script,
     * The script needs to be pre-processed, e.g. the imports and/or initializer
     * @param context the context that needs to be passed
     */
    void setup(JexlContext context);

    /**
     * The defined methods of the script
     *
     * @return a map of method name with parsed method object
     */
    Map<String, ScriptMethod> methods();

    /**
     * The defined classes of the script
     *
     * @return a map of class name with parsed class object
     */
    Map<String, ScriptClass> classes();

    /**
     * The defined methods of the script
     *
     * @return a map of import name with parsed import object
     */
    Map<String, ASTImportStatement> imports();


    /**
     * The defined methods of the script
     *
     * @return a map of label name with which no of child it points to
     */
    Map<String, Integer> jumps();


    /**
     *
     * @return returns the parsed script object
     */
    ASTJexlScript script();

    /**
     * The name under which it was imported
     *
     * @return name
     */
    String name();

    /**
     * From where it was imported
     *
     * @return location from where it was imported
     */
    String location();

}
