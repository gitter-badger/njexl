package org.apache.commons.jexl2.extension;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by noga on 21/03/15.
 */
public class PairHashMap extends HashMap {

    public static class Pair{
        public Object key;
        public Object value;
        public Pair(Object key, Object value){
            this.key = key ;
            this.value = value ;
        }
    }

    public PairHashMap() {
    }

    public PairHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public PairHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public PairHashMap(Map m) {
        super(m);
    }

    public PairHashMap(Pair[] pairs){
        super();
        if ( pairs == null){
            return;
        }
        for ( int i = 0 ; i < pairs.length;i++ ){
            super.put(pairs[i].key,pairs[i].value);
        }
    }
}
