/**
 *Copyright 2015 Nabarun Mondal
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

package com.noga.njexl.lang.extension.datastructures;

import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;

import java.util.*;

/**
 * Created by noga on 08/05/15.
 */
public class XList<T> extends ArrayList<T> {


    public static class Pair{
        public final Object l;
        public Object r;

        public Pair(Object l, Object r){
            this.l = l;
            this.r = r;
        }

        public Object get(int index){
            if ( index == 0 ) return l ;
            if ( index == 1 ) return r ;
            return this;
        }

        @Override
        public String toString(){
            return String.format("(%s,%s)",l,r);
        }
    }

    public XList() {
    }

    public XList(Collection c) {
        super(c);
    }

    public XList(Map m){
        super();
        for ( Object k : m.keySet() ){
            Object v = m.get(k);
            Pair p = new Pair(k,v);
            this.add((T) p);
        }
    }

    public XList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public int indexOf(Object o){

        if ( !( o instanceof Interpreter.AnonymousParam) ){
            return super.indexOf(o);
        }
        Interpreter.AnonymousParam anon = (Interpreter.AnonymousParam)o;

        Iterator iterator = iterator();
        int i = 0;
        while(iterator.hasNext()){
            Object item = iterator.next();
            anon.setIterationContext( this, item ,i);
            Object ret = anon.execute();
            if (TypeUtility.castBoolean(ret,false)){
                anon.removeIterationContext();
                return i ;
            }
            i++;
        }
        anon.removeIterationContext();
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        int i = indexOf(o);
        if ( i < 0 ) return false ;
        return true ;
    }

    public XList select(){
        return select(null);
    }

    public XList select(Interpreter.AnonymousParam anon){
        if ( anon == null ) {
            return new XList(this);
        }
        XList xList = new XList();
        Iterator iterator = iterator();
        int i = 0;
        while(iterator.hasNext()){
            Object item = iterator.next();
            anon.setIterationContextWithPartial( this, item ,i, xList);
            Object ret = anon.execute();
            if (TypeUtility.castBoolean(ret,false)){
                // ensure update to the variable is considered
                item = anon.getVar( TypeUtility._ITEM_);
                xList.add(item);
            }
            i++;
        }
        anon.removeIterationContext();
        return xList;
    }

    public ListSet set(){
        return set(null);
    }

    public ListSet set(Interpreter.AnonymousParam anon){
        if ( anon == null ){
            return new ListSet(this);
        }
        ListSet listSet = new ListSet();
        int i = 0;
        Iterator iterator = iterator();
        while(iterator.hasNext()){
            Object item = iterator.next();
            anon.setIterationContextWithPartial( this, item ,i, listSet);
            Object ret = anon.execute();
            listSet.add(ret);
            i++;
        }
        anon.removeIterationContext();
        return listSet;
    }

    public HashMap<Object,List> mset(){
        return mset(null);
    }

    public HashMap<Object,List> mset(Interpreter.AnonymousParam anon){
        if ( anon == null ){
            return SetOperations.multiset(this);
        }
        HashMap<Object,List> map = new HashMap<>();
        Iterator iterator = iterator();
        int i = 0;
        while(iterator.hasNext()){
            Object item = iterator.next();
            anon.setIterationContextWithPartial(this, item, i, map);
            Object key = anon.execute();
            if ( map.containsKey(key)){
                map.get(key).add(item);
            }else{
                XList list = new XList();
                list.add(item);
                map.put(key,list);
            }
            i++;
        }
        anon.removeIterationContext();
        return map;
    }

}
