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

package com.noga.njexl.lang.extension.dataaccess;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by noga on 19/02/16.
 * This class wraps an enum to give it a hash like look.
 */
public final class EnumWrapper {

    public static final ConcurrentHashMap<Class,EnumWrapper> wrappers = new ConcurrentHashMap<>();

    public final Map values;

    public final Set set;

    public final Class clazz;

    private EnumWrapper(Class cls) throws Exception {
        HashMap v = new HashMap<>();
        Object[] constants = cls.getEnumConstants();
        for ( int i = 0 ; i < constants.length;i++ ){
            v.put(i,constants[i]);
            v.put(String.valueOf(constants[i]), constants[i]);
        }
        values = Collections.unmodifiableMap(v);
        set = Collections.unmodifiableSet(EnumSet.allOf( cls));
        clazz = cls ;
    }

    public Object get(Object key){
        return values.get(key);
    }

    public static EnumWrapper enumWrapper(Object o) throws Exception{
        Class cls = o.getClass();
        if ( o instanceof String ){
            cls = Class.forName((String)o);
        }
        if ( o instanceof Class ){
            cls = (Class)o;
        }
        if ( ! cls.isEnum() ) throw new Exception( "Input Class is not Enum type!" );
        EnumWrapper w;
        if ( !wrappers.containsKey( cls ) ){
            w = new EnumWrapper(cls);
            wrappers.put(cls,w);
            return w;
        }
        w = wrappers.get(cls);
        return w;
    }

    @Override
    public String toString() {
        return  "E" + String.valueOf( set );
    }
}
