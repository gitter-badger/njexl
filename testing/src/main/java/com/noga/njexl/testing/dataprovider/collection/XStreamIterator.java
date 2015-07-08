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

package com.noga.njexl.testing.dataprovider.collection;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * A class that takes a finite list and makes
 * an infinite stream out of it
 * Created by noga on 15/04/15.
 */

public class XStreamIterator<T> implements ListIterator<T> {

    protected boolean stop = true ;

    /**
     * Sets the infinite streaming
     * @param stop if true it stops, if false it never does stop
     */
    public void setMode(boolean stop){
        this.stop = stop;
    }

    protected int index;

    /**
     * gets the index
     * @return the index at which the stream is
     */
    public int index() {
        return index ;
    }

    /**
     * Size of the stream
     * @return stream size
     */
    public int size() {
        return dataSourceTable.size() ;
    }

    @Override
    public boolean hasNext() {
        // ensure that it flows in
        if ( !stop ) return true ;
        // now this is finite set
        return index < size() ;
    }

    @Override
    public boolean hasPrevious() {
        if ( !stop ) return true ;
        return index > 0 ;
    }

    @Override
    public int nextIndex() {
        if ( hasNext() ){
            int x = (index + 1) % size() ;
            return (x == 0)? 0 : x ;
        }
        return -1;
    }

    @Override
    public int  previousIndex() {
        if ( index == 1 ){
            return  size() - 1 ;
        }
        return (index - 1) ;
    }


    /**
     * Reads data, creates APIUNITs RunInputObject and puts into the object
     * array for JUNIT consumption.
     *
     * @return The Next Object, if exists, null if it does not, and the list is
     *         exhausted
     */
    @Override
    public synchronized T next() {

        if (hasNext()) {
            index = nextIndex();
            return dataSourceTable.get(index);
        }
        return null;
    }

    @Override
    public synchronized T previous() {

        if (hasPrevious()) {
            index = previousIndex();
            return dataSourceTable.get(index);
        }
        return null;
    }

    @Override
    public void set(T o) {
       // we do not do it
    }

    @Override
    public synchronized void add(T o) {
        dataSourceTable.add(o);
    }

    @Override
    public void remove() {
        // We don't do it.
    }

    protected List<T> dataSourceTable ;


    public T get(int index){
        if ( index >= 0 && index < dataSourceTable.size() ){
            return dataSourceTable.get(index);
        }
        if ( !stop ){
            if ( index < 0 ) return dataSourceTable.get( dataSourceTable.size() - 1);
            if ( index >  dataSourceTable.size() -1  ) return dataSourceTable.get(0);
        }
        return null;
    }

    /**
     * Creates one
     * @param table a  List using which would be used as data source table
     */
    public XStreamIterator(List<T> table){
        dataSourceTable = table;
        index = -1;
    }

    /**
     * Creates one
     * @param table an array using which would be used as data source table
     */
    public XStreamIterator(T[] table){
        this(Arrays.asList(table));
    }
}
