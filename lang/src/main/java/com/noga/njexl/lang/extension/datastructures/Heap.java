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

import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.internal.ArrayIterator;
import java.util.*;

/**
 * A class for implementation for heap
 * Created by noga on 29/12/15.
 */
public class Heap implements Collection{

    public static class GenericComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            if ( o1 == o2 ) return  0;
            return SetOperations.arithmatic.compare(o1,o2,"<");
        }
    }

    public static final Comparator ASC = new GenericComparator();

    public static final Comparator DESC = ASC.reversed() ;

    public final Object[] heap;

    public final Comparator comparator ;

    public final boolean minHeap;

    public Object get(int index){
        return heap[index];
    }

    public Heap(int size, Comparator cmp){
        heap = new Object[size];
        comparator = cmp ;
        minHeap = false ;
    }

    public Heap(int size, boolean min){
        heap = new Object[size];
        if ( min ) {
            comparator = DESC ;
        }else{
            comparator = ASC ;
        }
        minHeap = min ;
    }

    public Heap(int size){
        this(size,false);
    }


    protected int find(Object o){
        if ( o == null ) return -1;
        return Arrays.binarySearch(heap,o);
    }

    protected boolean heapify(Object o){
        int c = comparator.compare(o,heap[0]);
        if ( c >  0  ){
            heap[0] = o ;
            // heapify - should I Nah, too much code?
            Arrays.sort(heap, comparator );
            return true ;
        }
        return false ;
    }

    @Override
    public boolean add(Object o) {
        if ( heap[0] == null ){
            for ( int i = 0 ; i < heap.length; i++ ){
                heap[i] = o;
            }
            return true ;
        }
        return heapify(o);
    }

    @Override
    public int size() {
        if ( heap[0] == null ) return 0;
        return heap.length ;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0 ;
    }

    @Override
    public boolean contains(Object o) {
        if ( isEmpty() ) return false ;
        int i = find(o);
        return (i >= 0) ;
    }

    @Override
    public Iterator iterator() {
        return new ArrayIterator(heap);
    }

    @Override
    public Object[] toArray() {
        return TypeUtility.array(heap);
    }

    @Override
    public Object[] toArray(Object[] a) {
        return toArray();
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        for ( Object o : c ){
            if ( !contains(o) ) { return false ;}
        }
        return true ;
    }

    @Override
    public boolean addAll(Collection c) {
        boolean added = true ;
        for ( Object o : c ){
            added = added && add(o);
        }
        return added;
    }

    @Override
    public boolean removeAll(Collection c) {
        return false ;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public void clear() {
        for ( int i = 0 ; i< heap.length ; i++ ){
            heap[i] =  null ;
        }
    }

    @Override
    public String toString() {
        return "|-|[" + TypeUtility.castString(heap,',')  + "]" ;
    }
}
