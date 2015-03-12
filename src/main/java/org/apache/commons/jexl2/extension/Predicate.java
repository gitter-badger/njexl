package org.apache.commons.jexl2.extension;

import org.apache.commons.jexl2.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by noga on 10/03/15.
 */
public class Predicate {

    public static enum SetRelation{
        INDEPENDENT,
        SUBSET,
        SUPERSET,
        EQUAL,
        OVERLAP
        ;
        public static boolean is(String s, SetRelation r){
            switch (s.trim().toUpperCase()){
                case "><":
                case "I":
                    return (INDEPENDENT == r );
                case "<>":
                case "O":
                    return (OVERLAP == r );
                case "=":
                case "==":
                case "E":
                    return (EQUAL == r );
                case "<":
                case "SUB":
                    return ( SUBSET == r );
                case "<=":
                case "SUBEQ":
                    return (SUBSET == r || EQUAL == r );
                case ">":
                case "SUP":
                    return ( SUPERSET == r );
                case ">=":
                case "SUPEQ":
                    return (SUPERSET == r || EQUAL == r );

            }
            return false ;
        }
    }

    public HashSet set_i(HashSet s1, HashSet s2){
        HashSet i = new HashSet();
        HashSet b = s1;
        HashSet s = s2;
        if ( s2.size() > s1.size() ){
            b = s2;
            s = s1 ;
        }
        for ( Object o : s ){
            if ( b.contains(o)){
                i.add(o);
            }
        }
        return i;
    }

    public HashSet set_u(HashSet s1, HashSet s2){
        HashSet b = s1;
        HashSet s = s2;
        if ( s2.size() > s1.size() ){
            b = s2;
            s = s1 ;
        }
        HashSet u = (HashSet)b.clone();

        for ( Object o : s ){
            u.add(o);
        }
        return u;
    }

    public HashSet set_d(HashSet s1, HashSet s2){
        HashSet d = (HashSet)s1.clone();
        for ( Object o : s2 ){
            if ( s1.contains(o)){
                d.remove(o);
            }
        }
        return d;
    }

    public HashSet set_sym_d(HashSet s1, HashSet s2){
        HashSet d12 = set_d(s1,s2);
        HashSet d21 = set_d(s2,s1);
        HashSet ssd = set_u(d12,d21);
        return ssd;
    }

    public SetRelation set_relation(HashSet s1, HashSet s2){

        HashSet ssd = set_sym_d(s1,s2);
        if ( ssd.isEmpty() ){
            return SetRelation.EQUAL ;
        }

        if ( s1.size() == 0 ){
            return SetRelation.SUBSET ;
        }
        if ( s2.size() == 0 ){
            return SetRelation.SUPERSET ;
        }

        HashSet i = set_i(s1,s2);
        if ( i.isEmpty() ){
            return SetRelation.INDEPENDENT ;
        }

        HashSet ssd_1_i = set_sym_d(i,s1);
        if ( ssd_1_i.isEmpty() ){
            return SetRelation.SUBSET ;
        }
        HashSet ssd_2_i = set_sym_d(i,s2);
        if ( ssd_2_i.isEmpty() ){
            return SetRelation.SUPERSET ;
        }
        return SetRelation.OVERLAP ;
    }

    public boolean is_set_relation(HashSet s1, HashSet s2, String relation){
        SetRelation actual = set_relation(s1, s2);
        return SetRelation.is(relation, actual);
    }

    public HashMap multiset(Object... args){
        Interpreter.AnonymousParam anon = null;
        ArrayList list = new ArrayList();
        if (args.length > 1) {
            if (args[0] instanceof Interpreter.AnonymousParam) {
                anon = (Interpreter.AnonymousParam) args[0];
                args = TypeUtility.shiftArrayLeft(args, 1);
            }
        }
        for (int i = 0; i < args.length; i++) {
            List l = TypeUtility.from(args[i]);
            list.addAll(l);
        }
        HashMap<Object,ArrayList> m = new HashMap();
        if (anon != null) {
            for (Object o : list) {
                anon.jexlContext.set(TypeUtility._ITEM_, o);
                Object ret = anon.block.jjtAccept(anon.pv, null);
                if ( !m.containsKey(ret) ){
                    m.put(ret,new ArrayList());
                }
                ArrayList values = m.get(ret);
                values.add(o);
            }
            anon.jexlContext.set(TypeUtility._ITEM_, null);
        }else{
            for (Object o : list) {
                if ( !m.containsKey(o) ){
                    m.put(o,new ArrayList());
                }
                ArrayList values = m.get(o);
                values.add(o);
            }
        }
        return m;
    }

    public HashMap mset_diff(HashMap<Object,ArrayList> mset1, HashMap<Object,ArrayList> mset2){
        HashMap<Object,int[]> diff = new HashMap<>();
        for ( Object k : mset1.keySet() ){
            int[] v = new int[2];
            v[0] = mset1.get(k).size();
            v[1] = 0 ;
            diff.put( k, v);
        }

        for ( Object k : mset2.keySet() ){
            int[] v = diff.get(k);
            if ( v == null ){
                v = new int[2];
                v[1] = mset2.get(k).size();
                v[0] = 0 ;
            }
            v[1] = mset2.get(k).size();
            diff.put( k, v);
        }
        return diff;
    }

    public boolean mset_equal(HashMap<Object,ArrayList> mset1, HashMap<Object,ArrayList> mset2){
        HashMap<Object,int[]> d = mset_diff(mset1,mset2);

        for ( Object k : d.keySet() ){
            int[] v = d.get(k);
            if ( v[0] != v[1] ){
                return false ;
            }
        }
        return true ;
    }

}
