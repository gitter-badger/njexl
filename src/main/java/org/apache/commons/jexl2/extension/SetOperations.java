package org.apache.commons.jexl2.extension;

import org.apache.commons.jexl2.Interpreter;
import org.apache.commons.jexl2.jvm.CodeGen;

import java.util.*;

/**
 * Created by noga on 10/03/15.
 */
public final class SetOperations {

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

    public static ListSet set_i(Set s1, Set s2){
        ListSet i = new ListSet();
        Set b = s1;
        Set s = s2;
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

    public static ListSet set_u(Set s1, Set s2){
        Set b = s1;
        Set s = s2;
        if ( s2.size() > s1.size() ){
            b = s2;
            s = s1 ;
        }

        ListSet u = new ListSet(b);

        for ( Object o : s ){
            u.add(o);
        }
        return u;
    }

    public static ListSet set_d(Set s1, Set s2){
        ListSet d = new ListSet(s1);
        for ( Object o : s2 ){
            if ( s1.contains(o)){
                d.remove(o);
            }
        }
        return d;
    }

    public static ListSet set_sym_d(Set s1, Set s2){
        ListSet d12 = set_d(s1,s2);
        ListSet d21 = set_d(s2,s1);
        ListSet ssd = set_u(d12,d21);
        return ssd;
    }

    public static SetRelation set_relation(Set s1, Set s2){

        ListSet ssd = set_sym_d(s1,s2);
        if ( ssd.isEmpty() ){
            return SetRelation.EQUAL ;
        }

        if ( s1.size() == 0 ){
            return SetRelation.SUBSET ;
        }
        if ( s2.size() == 0 ){
            return SetRelation.SUPERSET ;
        }

        ListSet i = set_i(s1,s2);
        if ( i.isEmpty() ){
            return SetRelation.INDEPENDENT ;
        }

        ListSet ssd_1_i = set_sym_d(i,s1);
        if ( ssd_1_i.isEmpty() ){
            return SetRelation.SUBSET ;
        }
        ListSet ssd_2_i = set_sym_d(i,s2);
        if ( ssd_2_i.isEmpty() ){
            return SetRelation.SUPERSET ;
        }
        return SetRelation.OVERLAP ;
    }

    public static boolean is_set_relation(Set s1, Set s2, String relation){
        SetRelation actual = set_relation(s1, s2);
        return SetRelation.is(relation, actual);
    }

    public static HashMap multiset(Object... args){
        Interpreter.AnonymousParam anon = null;
        String  anonCall = null ;
        ArrayList list = new ArrayList();
        if (args.length > 1) {
            if (args[0] instanceof Interpreter.AnonymousParam) {
                anon = (Interpreter.AnonymousParam) args[0];
                args = TypeUtility.shiftArrayLeft(args, 1);
            }
            if (args[0] instanceof String) {
                String b = (String)args[0];
                if ( b.startsWith(CodeGen.ANON_MARKER )){
                    String[] arr = b.split(CodeGen.ANON_MARKER);
                    args = TypeUtility.shiftArrayLeft(args, 1);
                    anonCall = arr[1];
                }
            }
        }

        for (int i = 0; i < args.length; i++) {
            List l = TypeUtility.from(args[i]);
            list.addAll(l);
        }
        HashMap<Object,ArrayList> m = new HashMap();
        if ( anonCall != null ){
            for (Object o : list) {
                Object ret = TypeUtility.callAnonMethod(anonCall,o);
                if ( !m.containsKey(ret) ){
                    m.put(ret,new ArrayList());
                }
                ArrayList values = m.get(ret);
                values.add(o);
            }
        }
        else if (anon != null) {
            for (Object o : list) {
                anon.interpreter.getContext().set(TypeUtility._ITEM_, o);
                Object ret = anon.block.jjtAccept(anon.interpreter, null);
                if ( !m.containsKey(ret) ){
                    m.put(ret,new ArrayList());
                }
                ArrayList values = m.get(ret);
                values.add(o);
            }
            anon.interpreter.getContext().remove(TypeUtility._ITEM_);
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

    public static HashMap mset_diff(Map<Object,ArrayList> mset1, Map<Object,ArrayList> mset2){
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

    public boolean mset_equal(Map<Object,ArrayList> mset1, Map<Object,ArrayList> mset2){
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
