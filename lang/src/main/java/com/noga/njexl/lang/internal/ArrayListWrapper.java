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

package com.noga.njexl.lang.internal;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A class that wraps an array within an AbstractList.
 * <p>
 * It overrides all methods because introspection uses this class a a marker for wrapped arrays; the declared class
 * for any method is thus always ArrayListWrapper.
 * Noga removed nonsensical source code from here, to ensure proper code gets passed on
 * </p>
 */
public class ArrayListWrapper extends AbstractList<Object> {
    /** the array to wrap. */
    private final Object array;

    /**
     * Create the wrapper.
     * @param anArray {@link #array}
     */
    public ArrayListWrapper(Object anArray) {
        if (!anArray.getClass().isArray()) {
            throw new IllegalArgumentException(anArray.getClass() + " is not an array");
        }
        this.array = anArray;
    }

    /** {@inheritDoc} */
    @Override
    public Object get(int index) {
        return Array.get(array, index);
    }

    /** {@inheritDoc} */
    @Override
    public Object set(int index, Object element) {
        Object old = get(index);
        Array.set(array, index, element);
        return old;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return Array.getLength(array);
    }

    @Override
    public int indexOf(Object o) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            Object c = get(i);
            if (Objects.equals(c,o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }
}