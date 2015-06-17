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

package com.noga.njexl.lang.extension;

import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.JexlException;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.datastructures.XList;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Class to handle Logical Collection Operations
 * Created by noga on 10/03/15.
 */
public final class SetOperations {

    public  enum SetRelation {
        /**
         * Independent
         */
        INDEPENDENT,
        /**
         * Subset
         */
        SUBSET,
        /**
         * Superset
         */
        SUPERSET,
        /**
         * Equal
         */
        EQUAL,
        /**
         * Overlapping Sets
         */
        OVERLAP;

        /**
         * Finds if String "s" is a representation of relation "r" or not
         * @param s the string :
         *          <pre>
         *
         *          "&gt;&lt;" , "I" : independent
         *          "&lt;&gt;" , "O" : overlap
         *          "=","==" , "E" : equal
         *          "&lt;" , "SUB" : subset
         *          "&lt;=" , "SUBEQ" : subset equals
         *          "&gt;" , "SUP" : superset
         *          "&gt;=" , "SUPEQ" : superset equals
         *
         *          </pre>
         * @param r set relation
         * @return true if it is, false if it is not
         */
        public static boolean is(String s, SetRelation r) {
            switch (s.trim().toUpperCase()) {
                case "><":
                case "I":
                    return (INDEPENDENT == r);
                case "<>":
                case "O":
                    return (OVERLAP == r);
                case "=":
                case "==":
                case "E":
                    return (EQUAL == r);
                case "<":
                case "SUB":
                    return (SUBSET == r);
                case "<=":
                case "SUBEQ":
                    return (SUBSET == r || EQUAL == r);
                case ">":
                case "SUP":
                    return (SUPERSET == r);
                case ">=":
                case "SUPEQ":
                    return (SUPERSET == r || EQUAL == r);

            }
            return false;
        }
    }

    /**
     * Set Intersection
     * @param s1 set 1
     * @param s2 set 2
     * @return Intersection of the sets
     */
    public static Set set_i(Set s1, Set s2) {
        ListSet i = new ListSet();
        Set b = s1;
        Set s = s2;
        if (s2.size() > s1.size()) {
            b = s2;
            s = s1;
        }
        for (Object o : s) {
            if (b.contains(o)) {
                i.add(o);
            }
        }
        return i;
    }

    /**
     * Set Union
     * @param s1 set 1
     * @param s2 set 2
     * @return Union of the sets
     */
    public static Set set_u(Set s1, Set s2) {
        Set b = s1;
        Set s = s2;
        if (s2.size() > s1.size()) {
            b = s2;
            s = s1;
        }

        ListSet u = new ListSet(b);

        for (Object o : s) {
            u.add(o);
        }
        return u;
    }

    /**
     * Set Difference
     * @param s1 set 1
     * @param s2 set 2
     * @return difference of the sets : s1 - s2
     */
    public static Set set_d(Set s1, Set s2) {
        ListSet d = new ListSet(s1);
        for (Object o : s2) {
            if (s1.contains(o)) {
                d.remove(o);
            }
        }
        return d;
    }

    /**
     * Set Symmetric Difference : s1-s2 Union s2-s1
     * @param s1 set 1
     * @param s2 set 2
     * @return Symmetric Difference of the sets
     */
    public static Set set_sym_d(Set s1, Set s2) {
        Set d12 = set_d(s1, s2);
        Set d21 = set_d(s2, s1);
        Set ssd = set_u(d12, d21);
        return ssd;
    }

    /**
     * Finds set relation between two objects,
     * after casting them into set
     * @param o1 object 1
     * @param o2 object 2
     * @return the relation between them
     */
    public static SetRelation set_relation(Object o1, Object o2) {
        Set s1 = TypeUtility.set(o1);
        Set s2 = TypeUtility.set(o2);
        return set_relation(s1,s2);
    }

    /**
     * Finds relation between two sets
     * @param s1 set 1
     * @param s2 set 2
     * @return the relation between them
     */
    public static SetRelation set_relation(Set s1, Set s2) {

        Set ssd = set_sym_d(s1, s2);
        if (ssd.isEmpty()) {
            return SetRelation.EQUAL;
        }

        if (s1.size() == 0) {
            return SetRelation.SUBSET;
        }
        if (s2.size() == 0) {
            return SetRelation.SUPERSET;
        }

        Set i = set_i(s1, s2);
        if (i.isEmpty()) {
            return SetRelation.INDEPENDENT;
        }

        Set ssd_1_i = set_sym_d(i, s1);
        if (ssd_1_i.isEmpty()) {
            return SetRelation.SUBSET;
        }
        Set ssd_2_i = set_sym_d(i, s2);
        if (ssd_2_i.isEmpty()) {
            return SetRelation.SUPERSET;
        }
        return SetRelation.OVERLAP;
    }

    /**
     * Given two sets, finds if the relation passed holds for them
     * @param s1 set 1
     * @param s2 set 2
     * @param relation the relation
     * @return true if s1 and s2 abide by relation, else false
     */
    public static boolean is_set_relation(Set s1, Set s2, String relation) {
        SetRelation actual = set_relation(s1, s2);
        return SetRelation.is(relation, actual);
    }

    /**
     * Converts a list into a multi-set
     * @param args the arguments
     * @return a multi-set
     */
    public static HashMap<Object, List> multiset(Object... args) {
        Interpreter.AnonymousParam anon = null;
        XList list = new XList();
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
        HashMap<Object, List> m = new HashMap();
        if (anon != null) {
            int i = 0;
            for (Object o : list) {
                anon.setIterationContext(list, o, i);
                Object ret = anon.execute();
                if (!m.containsKey(ret)) {
                    m.put(ret, new XList());
                }
                List values = m.get(ret);
                values.add(o);
                i++;
            }
            anon.removeIterationContext();
        } else {
            for (Object o : list) {
                if (!m.containsKey(o)) {
                    m.put(o, new XList());
                }
                List values = m.get(o);
                values.add(o);
            }
        }
        return m;
    }

    /**
     * Finds difference of two lists
     * @param l left side list
     * @param r right side list
     * @return the difference in multi-set-diff form, key : ( left_count, right_count)
     */
    public static HashMap<Object, int[]> list_diff(Object l, Object r) {
        Map<Object, List> mset1 = multiset(l);
        Map<Object, List> mset2 = multiset(r);
        return mset_diff(mset1,mset2);
    }

    /**
     * Finds list diff with anonymous parameter
     * @param anon the anonymous parameter
     * @param mset1 multi-set 1
     * @param mset2 multi-set 2
     * @return the difference
     */
    public static HashMap mset_diff(Interpreter.AnonymousParam anon,
                                                    Map<Object, List> mset1, Map<Object, List> mset2) {
        HashMap diff = mset_diff(mset1, mset2);
        if ( anon == null ){
            return diff;
        }
        Object context = new Object[]{ mset1, mset2 };
        HashMap result = new HashMap();
        for ( Object k : diff.keySet() ){
            Object[] values = new Object[]{ mset1.get(k) , mset2.get(k) } ;
            anon.setIterationContext(context, values, k);
            Object ret = anon.execute();
            boolean same = TypeUtility.castBoolean(ret,false) ;
            if ( !same ){
                result.put(k,values);
            }
        }
        anon.removeIterationContext();
        return result;
    }

    /**
     * Finds diff between two multi-sets
     * @param mset1 multi-set 1
     * @param mset2 mutli-set 2
     * @return the diff
     */
    public static HashMap<Object, int[]> mset_diff(Map<Object, List> mset1, Map<Object,List> mset2) {
        HashMap<Object, int[]> diff = new HashMap<>();
        for (Object k : mset1.keySet()) {
            int[] v = new int[2];
            v[0] = mset1.get(k).size();
            v[1] = 0;
            diff.put(k, v);
        }

        for (Object k : mset2.keySet()) {
            int[] v = diff.get(k);
            if (v == null) {
                v = new int[2];
                v[1] = mset2.get(k).size();
                v[0] = 0;
            }
            v[1] = mset2.get(k).size();
            diff.put(k, v);
        }
        return diff;
    }

    /**
     * Finds relation between two lists
     * @param o1 left object to be casted as list
     * @param o2 right object to be casted as list
     * @return the relation between them : left R right
     */
    public static SetRelation list_relation(Object o1, Object o2) {
        HashMap m1 = multiset(o1);
        HashMap m2 = multiset(o2);
        return mset_relation(m1,m2);
    }

    /**
     * Finds multi-set relation
     * @param mset1 multi-set 1
     * @param mset2 multi-set 2
     * @return the relation
     */
    public static SetRelation mset_relation(Map<Object, List> mset1, Map<Object, List> mset2) {
        HashMap<Object, int[]> d = mset_diff(mset1, mset2);
        if (d.isEmpty()) {
            return SetRelation.EQUAL;
        }
        if (mset1.isEmpty()) {
            return SetRelation.SUBSET;
        }
        if (mset2.isEmpty()) {
            return SetRelation.SUPERSET;
        }
        boolean eq = true;
        boolean sub = true;
        boolean sup = true;
        boolean overlap = false;
        for (Object o : d.keySet()) {
            int[] values = d.get(o);
            if (values[0] != 0 && values[1] != 0) {
                overlap = true;
            }
            if (values[0] != values[1]) {
                eq = false;
            }
            if (values[0] > values[1]) {
                sub = false;
            }
            if (values[0] < values[1]) {
                sup = false;
            }
        }
        if (!overlap) {
            return SetRelation.INDEPENDENT;
        }
        if (eq) {
            return SetRelation.EQUAL;
        }
        if (sub) {
            return SetRelation.SUBSET;
        }
        if (sup) {
            return SetRelation.SUPERSET;
        }
        return SetRelation.OVERLAP;
    }

    /**
     * Finds if the two multi-sets are related by relation
     * @param mset1 left multi-set
     * @param mset2 right multi-set
     * @param relation the relation R to be used to compare
     * @return true if left R right, else false
     */
    public static boolean is_mset_relation(Map<Object, List> mset1, Map<Object, List> mset2, String relation) {
        SetRelation actual = mset_relation(mset1, mset2);
        return SetRelation.is(relation, actual);
    }

    /**
     * Finds relationship between two dictionaries
     * @param m1 left dict
     * @param m2 right dict
     * @return relation
     */
    public static SetRelation dict_relation(Map m1, Map m2){
        SetRelation setRelation = set_relation(m1.keySet(), m2.keySet());
        if ( SetRelation.INDEPENDENT == setRelation || SetRelation.OVERLAP == setRelation ){
            return setRelation ;
        }
        Map s = m1;
        Map b = m2;
        if ( SetRelation.SUPERSET == setRelation ){
            s = m2;
            b = m1;
        }
        for ( Object k : s.keySet() ){
            Object l = s.get(k);
            Object r = b.get(k);
            if ( ! Objects.equals(l,r) ){
                return SetRelation.OVERLAP ;
            }
        }

        return setRelation ;
    }

    /**
     * Checks if two dicts are abiding relation
     * @param m1 left dict
     * @param m2 right dict
     * @param relation relation between them
     * @return true if m1 R m2, else false
     */
    public static boolean is_dict_relation(Map m1, Map m2,String relation){
        SetRelation actual = dict_relation(m1,m2);
        return SetRelation.is(relation,actual);
    }

    /**
     * Finds list intersection
     * @param l1 list 1
     * @param l2 list 2
     * @return intersection
     */
    public static List list_i(Object l1, Object l2) {
        HashMap m1 = multiset(l1);
        HashMap m2 = multiset(l2);
        HashMap<Object, int[]> d = mset_diff(m1, m2);
        XList l = new XList();
        for (Object o : d.keySet()) {
            int[] var = d.get(o);
            int t = Math.min(var[0], var[1]);
            for (int i = 0; i < t; i++) {
                l.add(o);
            }
        }
        return l;
    }

    /**
     * Finds list union
     * @param l1 list 1
     * @param l2 list 2
     * @return union
     */
    public static List list_u(Object l1, Object l2) {
        HashMap m1 = multiset(l1);
        HashMap m2 = multiset(l2);
        HashMap<Object, int[]> d = mset_diff(m1, m2);
        XList l = new XList();
        for (Object o : d.keySet()) {
            int[] var = d.get(o);
            int t = Math.max(var[0], var[1]);
            for (int i = 0; i < t; i++) {
                l.add(o);
            }
        }
        return l;
    }

    /**
     * Finds list difference
     * @param l1 list 1
     * @param l2 list 2
     * @return difference
     */
    public static List list_d(Object l1, Object l2) {
        HashMap m1 = multiset(l1);
        HashMap m2 = multiset(l2);
        HashMap<Object, int[]> d = mset_diff(m1, m2);
        XList l = new XList();
        for (Object o : d.keySet()) {
            int[] var = d.get(o);
            int t = var[0] - var[1];
            for (int i = 0; i < t; i++) {
                l.add(o);
            }
        }
        return l;
    }

    /**
     * Finds list symmetric difference
     * @param l1 list 1
     * @param l2 list 2
     * @return symmetric difference
     */
    public static List list_sym_d(Object l1, Object l2) {
        List i = list_i(l1, l2);
        List u = list_u(l1, l2);
        return list_d(u, i);
    }

    /**
     * Finds list join
     * @param a list 1
     * @param b list 2
     * @return join of the two lists
     */
    public static List join(Object a, Object b) {
        List left = TypeUtility.from(a);
        List right = TypeUtility.from(b);

        if (left == null || right == null) {
            return null;
        }

        int ls = left.size();
        int rs = right.size();
        if (ls == 0) {
            return right;
        }
        if (rs == 0) {
            return left;
        }
        XList r = new XList();
        for (int i = 0; i < ls; i++) {
            List item = TypeUtility.from(left.get(i));
            for (int j = 0; j < rs; j++) {
                XList l = new XList(item);
                l.add(right.get(j));
                r.add(l);
            }
        }
        return r;
    }

    /**
     * Arbitrary list join
     * @param args the lists
     * @return a joined list of lists
     */
    public static List join_c(Object... args) {
        Interpreter.AnonymousParam anon = null;
        XList join = new XList();
        if (args.length > 1) {
            if (args[0] instanceof Interpreter.AnonymousParam) {
                anon = (Interpreter.AnonymousParam) args[0];
                args = TypeUtility.shiftArrayLeft(args, 1);
            }
        }
        // the list of lists over which join would happen
        XList[] argsList = new XList[args.length];

        for (int i = 0; i < args.length; i++) {
            argsList[i] = TypeUtility.from(args[i]);
        }

        Iterator[] iterators = new Iterator[argsList.length];
        for (int i = 0; i < argsList.length; i++) {
            Iterator itr = argsList[i].iterator();
            iterators[i] = itr;
        }
        Object[] tuple = new Object[argsList.length];

        for (int i = iterators.length - 1; i >= 0; i--) {
            tuple[i] = iterators[i].next();
        }
        int c = 0;
        boolean broken = false ;
        if (anon != null) {
            anon.setIterationContext(argsList, tuple, c++);
            Object r = anon.execute();
            if ( r instanceof JexlException.Continue ){
                r = false ;
            }
            else if ( r instanceof JexlException.Break ){
                JexlException.Break br = (JexlException.Break)r;
                r = true ;
                if ( br.hasValue ){
                    r = br.value ;
                }
                broken = true ;
            }
            if (TypeUtility.castBoolean(r, false)) {
                XList t = new XList();
                for (int i = 0; i < tuple.length; i++) {
                    t.add(tuple[i]);
                }
                join.add(t);
            }
        } else {
            XList t = new XList();
            for (int i = 0; i < tuple.length; i++) {
                t.add(tuple[i]);
            }
            join.add(t);
        }

        boolean emptied = false;

        while (!broken) {

            emptied = true;
            for (int i = iterators.length - 1; i >= 0; i--) {
                Iterator itr = iterators[i];
                if (!itr.hasNext()) {
                    itr = argsList[i].iterator();
                    iterators[i] = itr;
                    tuple[i] = itr.next();
                    continue;
                }
                tuple[i] = itr.next();
                emptied = false;
                break;
            }
            if (emptied) {
                break;
            }

            if (anon != null) {
                anon.setIterationContext(argsList, tuple, c++);
                Object r = anon.execute();
                if (TypeUtility.castBoolean(r, false)) {
                    XList t = new XList();
                    for (int i = 0; i < tuple.length; i++) {
                        t.add(tuple[i]);
                    }
                    join.add(t);
                }
            } else {
                XList t = new XList();
                for (int i = 0; i < tuple.length; i++) {
                    t.add(tuple[i]);
                }
                join.add(t);
            }
        }
        if (anon != null) {
            anon.removeIterationContext();
        }
        return join;
    }

    /**
     * Given object c1 does it in some sense inside c2 or not
     * @param c1 the object which is to be inside
     * @param c2 the proposed container
     * @return true if it is, false otherwise
     */
    public static Boolean in(Object c1, Object c2) {
        if (c2 == null) {
            return false;
        }
        if (c2 instanceof String) {
            if (c1 == null) {
                return false;
            }
            return ((String) c2).contains(c1.toString());
        }
        if (c2 instanceof Set) {
            if (c1 instanceof Set) {
                return is_set_relation((Set) c1, (Set) c2, "<=");
            }
            return ((Set) c2).contains(c1);
        }
        if ( c2 instanceof Map ){
            if ( c1 instanceof Map ){
                return is_dict_relation((Map)c1,(Map)c2,"<=");
            }
            return ((Map) c2).containsKey(c1);
        }
        if (c2 instanceof Collection || c2.getClass().isArray()) {
            if (c1 != null && (c1 instanceof Collection || c1.getClass().isArray())) {
                Map m1 = multiset(c1);
                Map m2 = multiset(c2);
                return is_mset_relation(m1,m2,"<=");
            }
            if (c2 instanceof Collection) {
                return ((Collection) c2).contains(c1);
            } else {
                int len = Array.getLength(c2);
                for (int i = 0; i < len; i++) {
                    Object o = Array.get(c2, i);
                    if (Objects.equals(o, c1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
