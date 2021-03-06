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

/**
 * Manages variables which can be referenced in a JEXL expression.
 * <p>Note that JEXL may use '$jexl' and '$ujexl' variables for internal purpose; setting or getting those
 * variables may lead to unexpected results unless specified otherwise.</p>
 *  @since 1.0
 *  @version $Id: JexlContext.java 1202769 2011-11-16 16:33:02Z henrib $
 */
public interface JexlContext {

    String PARENT_CONTEXT = "__parent__" ;

    /**
     * Gets the value of a variable.
     * @param name the variable's name
     * @return the value
     */
    Object get(String name);

    /**
     * Sets the value of a variable.
     * @param name the variable's name
     * @param value the variable's value
     */
    void set(String name, Object value);

    /**
     * Checks whether a variable is defined in this context.
     * <p>A variable may be defined with a null value; this method checks whether the
     * value is null or if the variable is undefined.</p>
     * @param name the variable's name
     * @return true if it exists, false otherwise
     */
    boolean has(String name);

    /**
     * Remove the variable if needed,
     * should never throw exception
     * @param name the name of the variable
     */
    void remove(String name);

    /**
     * Needed to call methods,
     * Copies the context
     * @return a copy of the original context, depends on the context
     */
    JexlContext copy();

    /**
     * Cleans the context to make the individual items
     * for garbage collection
     */
    void clear();
}