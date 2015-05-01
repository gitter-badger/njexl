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
public class StringIterator extends YieldedIterator {

    int cur;
    int end ;
    int start;
    int step;

    final String text;

    public StringIterator(String text,int end, int start, int step){
        this.text = text;
        cur = start - step ;
        this.start = start ;
        this.end = end ;
        this.step = step;
    }

    public StringIterator(String text,int end,int start){
        this(text,end,start,1);
    }

    public StringIterator(String text,int end){
        this(text,end,0);
    }

    public StringIterator(String text){
        this(text,text.length());
    }

    @Override
    public boolean hasNext() {
        return cur < end - step  ;
    }

    @Override
    public Object next() {
        cur += step;
        return text.charAt(cur);
    }

    @Override
    public void remove() {}

    @Override
    public void forEachRemaining(Consumer action) {}
}
