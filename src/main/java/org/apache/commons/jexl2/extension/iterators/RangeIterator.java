package org.apache.commons.jexl2.extension.iterators;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by noga on 31/03/15.
 */
public  class RangeIterator implements Iterator {
    protected long e;
    protected long b;
    protected long s;

    private long cur ;

    public RangeIterator(long end, long begin, long step){
        e = end;
        b = begin;
        s = step;
        cur = b;
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
