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
import com.noga.njexl.lang.JexlException;
import com.noga.njexl.lang.Script;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;

import java.util.*;

/**
 * An EXtended List -- XList.
 * The purpose, doing some of the fantastic functional programming stuff
 * Created by noga on 08/05/15.
 */
public class XList<T> extends ArrayList<T> {


    public static class Pair implements Map.Entry{

        public Object l;

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

        @Override
        public Object getKey() {
            return l;
        }

        @Override
        public Object getValue() {
            return r;
        }

        @Override
        public Object setValue(Object value) {
            Object o = r;
            r = value;
            return o;
        }
    }

    public XList() {
        super();
    }

    public XList(Collection c) {
        super(c);
    }

    public XList(T[] c) {
        super();
        for ( int i = 0 ; i < c.length; i++ ){
            this.add(c[i]);
        }
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
        int i = -1;
        while(iterator.hasNext()){
            i++;
            Object item = iterator.next();
            anon.setIterationContext( this, item ,i);
            Object ret = anon.execute();
            if ( ret instanceof JexlException.Continue ){
                continue;
            }

            if (TypeUtility.castBoolean(ret,false)){
                return i ;
            }
        }
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
        int i = -1;
        while(iterator.hasNext()){
            i++;
            Object item = iterator.next();
            anon.setIterationContextWithPartial( this, item ,i, xList);
            Object ret = anon.execute();

            if ( ret instanceof JexlException.Break ){
                if ( ((JexlException.Break) ret).hasValue ){
                    ret = ((JexlException.Break) ret).value ;
                    if (TypeUtility.castBoolean(ret,false)){
                        // ensure update to the variable is considered
                        item = anon.getVar( Script._ITEM_);
                        xList.add(item);
                    }
                }
                break;
            }
            if (TypeUtility.castBoolean(ret,false)){
                // ensure update to the variable is considered
                item = anon.getVar( Script._ITEM_);
                xList.add(item);
            }
        }
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
        int i = -1;
        Iterator iterator = iterator();
        while(iterator.hasNext()){
            i++;
            Object item = iterator.next();
            anon.setIterationContextWithPartial( this, item ,i, listSet);
            Object ret = anon.execute();
            if ( ret instanceof JexlException.Continue ){
                if ( ((JexlException.Continue) ret).hasValue ){
                    ret = ((JexlException.Continue) ret).value ;
                    listSet.add(ret);
                }
                continue;
            }

            if ( ret instanceof JexlException.Break ){
                if ( ((JexlException.Break) ret).hasValue ){
                    ret = ((JexlException.Break) ret).value ;
                    listSet.add(ret);
                }
                break;
            }
            listSet.add(ret);
        }
        return listSet;
    }

    public XList map(Interpreter.AnonymousParam anon){
        if ( anon == null ){
            return new XList(this);
        }
        XList list = new XList();
        int i = -1;
        Iterator iterator = iterator();
        while(iterator.hasNext()){
            i++;
            Object item = iterator.next();
            anon.setIterationContextWithPartial( this, item ,i, list);
            Object ret = anon.execute();
            if ( ret instanceof JexlException.Continue ){
                if ( ((JexlException.Continue) ret).hasValue ){
                    ret = ((JexlException.Continue) ret).value ;
                    list.add(ret);
                }
                continue;
            }

            if ( ret instanceof JexlException.Break ){
                if ( ((JexlException.Break) ret).hasValue ){
                    ret = ((JexlException.Break) ret).value ;
                    list.add(ret);
                }
                break;
            }
            list.add(ret);
        }
        return list;
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
        int i = -1;
        while(iterator.hasNext()){
            i++;
            Object item = iterator.next();
            anon.setIterationContextWithPartial(this, item, i, map);
            Object key = anon.execute();
            if ( key instanceof JexlException.Continue ){
                if ( ((JexlException.Continue) key).hasValue ){
                    key = ((JexlException.Continue) key).value ;
                    if ( map.containsKey(key)){
                        map.get(key).add(item);
                    }else{
                        XList list = new XList();
                        list.add(item);
                        map.put(key,list);
                    }
                }
                continue;
            }

            if ( key instanceof JexlException.Break ){
                if ( ((JexlException.Break) key).hasValue ){
                    key = ((JexlException.Break) key).value ;
                    if ( map.containsKey(key)){
                        map.get(key).add(item);
                    }else{
                        XList list = new XList();
                        list.add(item);
                        map.put(key,list);
                    }
                }
                break;
            }

            if ( map.containsKey(key)){
                map.get(key).add(item);
            }else{
                XList list = new XList();
                list.add(item);
                map.put(key,list);
            }
        }
        return map;
    }

}
