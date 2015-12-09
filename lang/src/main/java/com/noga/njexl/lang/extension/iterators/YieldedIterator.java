/**
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

package com.noga.njexl.lang.extension.iterators;

import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.datastructures.XList;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This sort of iterator is more fun,
 * Let's you do way more thing than standard ones
 * Created by noga on 01/05/15.
 */
public abstract class YieldedIterator implements Iterator, Cloneable,
        Arithmetic,Logic {

    public static final String ERROR =  "Sorry, can not make the loop infinite by range!" ;

    /**
     * Get's a list out of an iterator
     * @param iterator the input
     * @return a list
     */
    public static List list(Iterator iterator){
        List list ;
        if ( iterator == null ){
            list = Collections.emptyList();
            return list;
        }
        list = new XList<>();
        while ( iterator.hasNext() ){
            list.add( iterator.next());
        }
        return list;
    }

    public YieldedIterator(){
        decreasing = false ;
    }

    protected boolean decreasing;

    public boolean decreasing(){ return decreasing ; }

    protected List list;

    protected List reverseList;

    /**
     * Resets the iterator back to where it was in the beginning
     */
    public abstract void reset();

    /**
     * Gets a reverse iterator from this one
     * @return another iterator, in reverse
     */
    public abstract YieldedIterator inverse();

    @Override
    public Object clone(){
        try {
            return super.clone();
        }catch (CloneNotSupportedException e){
            System.err.println("Why I am being thrown?");
        }
        return null;
    }

    /**
     * Yields a list out of the iterator - if need be
     * @return the list
     */
    public List list(){
        if ( list != null ){
            return list;
        }
        YieldedIterator iterator = (YieldedIterator)this.clone() ;
        iterator.reset(); // reset it to the basic
        list = list(iterator);
        return list;
    }

    /**
     * Yields a reverse list out of the iterator - if need be
     * @return the list
     */
    public List reverse(){
        if ( reverseList != null ){
            return reverseList;
        }
        List l = list();
        if ( l.isEmpty() ){
            reverseList = Collections.emptyList();
        }else{
            reverseList = new XList<>();
            int size = l.size();
            for ( int i = 0 ; i <size; i++ ){
                Object o = l.get(size-i-1);
                reverseList.add(o);
            }
        }
        return reverseList ;
    }

    @Override
    public Object neg() {
        return Collections.emptyList();
    }

    @Override
    public Object sub(Object o) {
        if ( o instanceof YieldedIterator ){
            return SetOperations.list_diff(list(),((YieldedIterator) o).list);
        }
        return SetOperations.list_diff( list(), o);
    }

    @Override
    public Object mul(Object o) {
        if ( o instanceof YieldedIterator ){
            return SetOperations.join(list(),((YieldedIterator) o).list);
        }
        return SetOperations.join( list(),o);
    }

    @Override
    public Object div(Object o) {
        return SetOperations.division(list(),o);
    }

    @Override
    public Object add(Object o) {
        if ( o instanceof YieldedIterator ){
            return SetOperations.arithmatic.add(list(),((YieldedIterator) o).list);
        }
        return SetOperations.arithmatic.add(list(),o);
    }

    @Override
    public Object exp(Object o) {
        return SetOperations.arithmatic.power(list(),o);
    }

    @Override
    public Object and(Object o) {
        if ( o instanceof YieldedIterator ){
            return SetOperations.list_i(list(),((YieldedIterator) o).list);
        }
        return SetOperations.list_i(list(),o);
    }

    @Override
    public Object complement() {
        return Collections.emptyList();
    }

    @Override
    public Object or(Object o) {
        if ( o instanceof YieldedIterator ){
            return SetOperations.list_u(list(),((YieldedIterator) o).list);
        }
        return SetOperations.list_u(list(),o);
    }

    @Override
    public Object xor(Object o) {
        if ( o instanceof YieldedIterator ){
            return SetOperations.list_sym_d(list(),((YieldedIterator) o).list);
        }
        return SetOperations.list_sym_d(list(),o);
    }
}
