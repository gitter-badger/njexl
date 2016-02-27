/**
*Copyright 2016 Nabarun Mondal
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

package com.noga.njexl.lang.extension.datastructures;

import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.JexlException;
import com.noga.njexl.lang.Script;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;

import java.util.*;

/**
 * Create a awesome Set that is also having capability of indexers like a list
 * Created by noga on 15/03/15.
 */
public class ListSet<T> extends HashSet<T> implements List<T> {

    /**
     * A list to shadow the hash map
     */
    protected XList<T> behind;

    public ListSet() {
        this.behind = new XList<>();
    }

    public ListSet(Collection<? extends T> c) {
        this();
        this.addAll(c);
    }

    public ListSet(int initialCapacity) {
        super(initialCapacity);
        this.behind = new XList(initialCapacity);
    }

    public ListSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.behind = new XList(initialCapacity);
    }

    @Override
    public boolean add(T t) {
        if ( super.add(t)){
            behind.add(t);
            return true ;
        }
        return false ;
    }

    @Override
    public void clear() {
        behind.clear();
        super.clear();
    }

    @Override
    public boolean remove(Object o) {
        if ( super.remove(o) ){
            behind.remove(o);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if( super.removeAll(c) ){
            behind.clear();
            behind.addAll(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int size = size() ;
        for(T t : c){
            this.add(t);
        }
        return size != size();
    }
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        int size = size() ;
        int i = index ;
        for(T t : c){
            this.add(i++, t);
        }
        return size != size();
    }

    /***
     * This is so that I can still index x['xxx'] and get a true false as if in contains
     * @param o the object as index
     * @return the item in the list
     */
    public boolean get(Object o){
        return super.contains(o);
    }

    @Override
    public T get(int index) {
        return behind.get(index);
    }

    @Override
    public T set(int index, T element) {
        if ( super.contains(element)){
            behind.remove(index);
        }
        super.add(element);
        behind.set(index,element);
        return element;
    }

    @Override
    public void add(int index, T element) {
        if ( super.contains(element)){
            behind.remove(element);
        }
        behind.add(index,element);
    }

    @Override
    public T remove(int index) {
        T t = behind.remove(index);
        super.remove(t);
        return t;
    }

    @Override
    public int indexOf(Object o) {
        if ( !super.contains(o)){
            return -1;
        }
        return behind.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<T> listIterator() {
        return behind.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return behind.subList(fromIndex,toIndex);
    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("S{ ");
        for(Object o : behind){
            buffer.append(o).append(",");
        }
        String s = buffer.toString();
        s = s.substring(0,s.length()-1);
        s += " }";
        return s;
    }

    @Override
    public boolean equals(Object o){
        if ( o == null ) return false ;
        if ( o instanceof Set ){
            return SetOperations.set_relation(this,(Set)o)
                    == SetOperations.SetRelation.EQUAL ;
        }
        if ( o instanceof List ){
            return SetOperations.list_relation(this, o)
                    == SetOperations.SetRelation.EQUAL ;
        }
        return false ;
    }

    @Override
    public int hashCode() {
       return super.hashCode();
    }

    public ListSet map(Interpreter.AnonymousParam anon){
        return behind.set(anon);
    }
    public ListSet set(Interpreter.AnonymousParam anon){
        return map(anon);
    }
    public ListSet select(Interpreter.AnonymousParam anon){
        if ( anon == null ) {
            return new ListSet(this);
        }
        ListSet xList = new ListSet();
        Iterator iterator = iterator();
        int i = -1;
        while(iterator.hasNext()){
            i++;
            Object item = iterator.next();
            anon.setIterationContextWithPartial( this, item ,i, xList);
            Object ret = anon.execute();
            if ( ret instanceof JexlException.Break ){
                if ( ((JexlException.Break) ret).hasValue ){
                    ret = ((JexlException.Break) ret).value ;
                    if (TypeUtility.castBoolean(ret,false)){
                        // ensure update to the variable is considered
                        item = anon.getVar( Script._ITEM_);
                        xList.add(item);
                    }
                }
                break;
            }
            if ( ret instanceof JexlException.Continue ){
                if ( ((JexlException.Continue) ret).hasValue ){
                    ret = ((JexlException.Continue) ret).value ;
                    if (TypeUtility.castBoolean(ret,false)){
                        // ensure update to the variable is considered
                        item = anon.getVar( Script._ITEM_);
                        xList.add(item);
                    }
                }
                continue;
            }
            if (TypeUtility.castBoolean(ret,false)){
                // ensure update to the variable is considered
                item = anon.getVar( Script._ITEM_);
                xList.add(item);
            }
        }
        return xList;
    }
}
