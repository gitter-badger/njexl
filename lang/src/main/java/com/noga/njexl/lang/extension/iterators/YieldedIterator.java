package com.noga.njexl.lang.extension.iterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by noga on 01/05/15.
 */
public abstract class YieldedIterator implements Iterator, Cloneable{

    protected List list;

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
     * generates a list out of the iterator - if need be
     * @return the list
     */
    public List list(){
        if ( list != null ){
            return list;
        }
        YieldedIterator iterator = (YieldedIterator)this.clone() ;
        if ( iterator == null ){
            list = Collections.emptyList();
            return list;
        }
        list = new ArrayList<>();
        while ( iterator.hasNext() ){
            list.add( iterator.next());
        }
        return list;
    }
}
