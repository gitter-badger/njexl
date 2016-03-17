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

package com.noga.njexl.lang.extension.iterators;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.datastructures.XList;

import java.lang.reflect.Array;
import java.util.List;

/**
 * A range iterator, for longish stuff to be done
 * Created by noga on 31/03/15.
 */
public  class RangeIterator extends YieldedIterator {

    protected long e;

    protected long b;

    protected long s;

    private long cur ;

    private void init(long end, long begin, long step){
        e = end;
        b = begin;
        s = step;
        if ( b > e && s < 0 ){
            decreasing = true ;

        }
        else if ( b <= e && s >= 0  ){
            decreasing = false ;
        }
        else{
            throw new IllegalArgumentException(ERROR);
        }
        cur = b - s ;
    }

    public RangeIterator(long end, long begin, long step){
        init(end, begin,step);
    }

    public RangeIterator(long end, long begin){
        if ( end >= begin ) {
            init(end, begin, 1);
        }else{
            init(end,begin,-1);
        }
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
    public void reset() {
        cur = b - s ;
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
    public synchronized boolean hasNext() {
        long r = cur - e + s ;
        return ( !decreasing && r < 0 ) || ( decreasing && r > 0 );
    }

    @Override
    public synchronized Object next() {
        cur += s;
        if ( (int)cur == cur ) return (int)cur;
        return cur ;
    }


    @Override
    public YieldedIterator inverse() {
        return new RangeIterator(this.b, this.e,-this.s);
    }

    /**
     * Splices the target
     * @param target an array or a list or a String
     * @return spliced object
     */
    public Object splice(Object target) {
        if ( target instanceof String ){
            String ts = (String)target;
            StringBuffer buffer = new StringBuffer();
            int s = ts.length() ;
            while( hasNext() ){
                int i = TypeUtility.castInteger(next());
                i = ( s + i) % s ;
                buffer.append( ts.charAt(i));
            }
            return buffer.toString();
        }
        List l = new XList();
        if ( target instanceof List){
            List tl = (List)target;
            int s = tl.size();
            while( hasNext() ){
                int i = TypeUtility.castInteger(next());
                i = ( s + i) % s ;
                l.add( tl.get(i));
            }
            return l;

        }
        if ( target.getClass().isArray() ){
            int s = Array.getLength( target );
            while( hasNext() ){
                int i = TypeUtility.castInteger(next());
                i = ( s + i) % s ;
                l.add(Array.get( target, i));
            }
            return l.toArray();
        }
        return target;
    }

}
