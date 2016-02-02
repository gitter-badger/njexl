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
 **/

package com.noga.njexl.lang.extension.iterators;

import com.noga.njexl.lang.extension.TypeUtility;

import java.util.function.Consumer;

/**
 * A symbol iterator, so that we can give ranges in char
 * Created by noga on 11/09/15.
 */
public class SymbolIterator extends YieldedIterator {

    protected Character e;
    protected Character b;
    protected short s;

    private Character cur ;

    public final String str;

    public final String rstr;

    private void init(Character end, Character begin, short step){
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
        cur = (char)(b - s);

    }

    public SymbolIterator(Character end, Character begin, short step){
        init(end,begin,step);
        str = TypeUtility.castString(list(), "");
        rstr = TypeUtility.castString(reverse(), "");
    }

    public SymbolIterator(Character end, Character begin){
        if ( end >= begin ){
            init(end,begin,(short)1);
        }else{
            init(end,begin,(short)-1);
        }
        str = TypeUtility.castString(list(), "");
        rstr = TypeUtility.castString(reverse(), "");
    }

    public SymbolIterator(Character end){
        this(end,'A');
    }

    public SymbolIterator(){
        this('Z');
    }

    @Override
    public void reset() {
        cur = (char)(b - s) ;
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
        if ( decreasing ){ return  cur + s >= e ; }
        return cur + s <= e ; // this is because in char range you want it included
    }

    @Override
    public Object next() {
        cur = (char)(cur + s);
        return cur;
    }

    @Override
    public YieldedIterator inverse() {
        return new SymbolIterator(this.b, this.e, (short)-this.s);
    }
}
