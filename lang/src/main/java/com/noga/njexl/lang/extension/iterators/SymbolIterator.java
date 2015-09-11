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
 **/

package com.noga.njexl.lang.extension.iterators;

import com.noga.njexl.lang.extension.TypeUtility;

import java.util.function.Consumer;

/**
 * Created by noga on 11/09/15.
 */
public class SymbolIterator extends YieldedIterator {

    protected Character e;
    protected Character b;
    protected short s;

    private Character cur ;

    public final String str;

    public SymbolIterator(Character end, Character begin, short step){
        e = end;
        b = begin;
        s = step;
        cur = (char)(b - s);
        str = TypeUtility.castString(list(),"");
    }

    public SymbolIterator(Character end, Character begin){
        this(end,begin,(short)1);
    }
    public SymbolIterator(Character end){
        this(end,'A');
    }

    public SymbolIterator(){
        this('Z');
    }

    @Override
    public String toString(){
        return String.format("[%c:%c:%d]", b,e,s);
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof SymbolIterator) ) return false ;
        SymbolIterator o = (SymbolIterator)obj;
        if ( e == o.e && b == o.b && s == o.s ) return true ;
        return false ;
    }

    @Override
    public int hashCode() {
        return  ( 31* (( 31 * e ) + b) + s);
    }

    @Override
    public boolean hasNext() {
        return cur + s <= e ; // this is because in char range you want it included
    }

    @Override
    public Object next() {
        cur = (char)(cur + s);
        return cur;
    }

    @Override
    public void forEachRemaining(Consumer action) {
    }

    @Override
    public void remove() {
    }
}
