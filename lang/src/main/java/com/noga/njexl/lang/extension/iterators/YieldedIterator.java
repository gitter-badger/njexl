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
