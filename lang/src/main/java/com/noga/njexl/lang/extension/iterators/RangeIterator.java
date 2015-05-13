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

import java.util.function.Consumer;

/**
 * Created by noga on 31/03/15.
 */
public  class RangeIterator extends YieldedIterator {
    protected long e;
    protected long b;
    protected long s;

    private long cur ;

    public RangeIterator(long end, long begin, long step){
        e = end;
        b = begin;
        s = step;
        cur = b - s ;
    }

    public RangeIterator(long end, long begin){
        this(end,begin,1);
    }

    public RangeIterator(long end){
        this(end,0);
    }

    public RangeIterator(){
        this(42);
    }

    @Override
    public String toString(){
        return String.format("[%d:%d:%d]", b,e,s);
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof RangeIterator) ) return false ;
        RangeIterator o = (RangeIterator)obj;
        if ( e == o.e && b == o.b && s == o.s ) return true ;
        return false ;
    }

    @Override
    public int hashCode() {
        return (int) ( 31* (( 31 * e ) + b) + s);
    }

    @Override
    public void forEachRemaining(Consumer action) {

    }

    @Override
    public boolean hasNext() {
        return cur < e - s ;
    }

    @Override
    public Object next() {
        return (cur += s) ;
    }

    @Override
    public void remove() {

    }
}
