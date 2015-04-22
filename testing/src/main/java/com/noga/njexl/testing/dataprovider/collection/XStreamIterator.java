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

import com.noga.njexl.testing.dataprovider.DataSourceTable;

import java.util.ListIterator;

/**
 * Created by noga on 15/04/15.
 */

public class XStreamIterator implements ListIterator {

    protected boolean stop = true ;

    public void setMode(boolean stop){
        this.stop = stop;
    }

    protected int index;

    public int index() {
        return index ;
    }

    public int size() {
        return dataSourceTable.length() ;
    }

    @Override
    public boolean hasNext() {
        return !stop;
    }

    @Override
    public boolean hasPrevious() {
        return true ;
    }

    @Override
    public int nextIndex() {
        if ( hasNext() ){
            int x = (index + 1) % size() ;
            return (x == 0)? 1 : x ;
        }
        return -1;
    }

    @Override
    public int previousIndex() {
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
    public Object next() {

        if (hasNext()) {
            index = nextIndex();
            return dataSourceTable.row(index);
        }
        return null;
    }

    @Override
    public Object previous() {

        if (hasPrevious()) {
            index = previousIndex();
            return dataSourceTable.row(index);
        }
        return null;
    }

    @Override
    public void set(Object o) {
        // We don't do it.
    }

    @Override
    public void add(Object o) {
        // We don't do it.
    }

    @Override
    public void remove() {
        // We don't do it.
    }

    protected DataSourceTable dataSourceTable ;

    public XStreamIterator(DataSourceTable table){
        dataSourceTable = table;
    }

}
