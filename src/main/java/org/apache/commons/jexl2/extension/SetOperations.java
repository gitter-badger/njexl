package org.apache.commons.jexl2.extension;

import org.apache.commons.jexl2.Interpreter;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by noga on 10/03/15.
 */
public final class SetOperations {

    public static enum SetRelation {
        INDEPENDENT,
        SUBSET,
        SUPERSET,
        EQUAL,
        OVERLAP;

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

    public static ListSet set_i(Set s1, Set s2) {
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

    public static ListSet set_u(Set s1, Set s2) {
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

    public static ListSet set_d(Set s1, Set s2) {
        ListSet d = new ListSet(s1);
        for (Object o : s2) {
            if (s1.contains(o)) {
                d.remove(o);
            }
        }
        return d;
    }

    public static ListSet set_sym_d(Set s1, Set s2) {
        ListSet d12 = set_d(s1, s2);
        ListSet d21 = set_d(s2, s1);
        ListSet ssd = set_u(d12, d21);
        return ssd;
    }

    public static SetRelation set_relation(Set s1, Set s2) {

        ListSet ssd = set_sym_d(s1, s2);
        if (ssd.isEmpty()) {
            return SetRelation.EQUAL;
        }

        if (s1.size() == 0) {
            return SetRelation.SUBSET;
        }
        if (s2.size() == 0) {
            return SetRelation.SUPERSET;
        }

        ListSet i = set_i(s1, s2);
        if (i.isEmpty()) {
            return SetRelation.INDEPENDENT;
        }

        ListSet ssd_1_i = set_sym_d(i, s1);
        if (ssd_1_i.isEmpty()) {
            return SetRelation.SUBSET;
        }
        ListSet ssd_2_i = set_sym_d(i, s2);
        if (ssd_2_i.isEmpty()) {
            return SetRelation.SUPERSET;
        }
        return SetRelation.OVERLAP;
    }

    public static boolean is_set_relation(Set s1, Set s2, String relation) {
        SetRelation actual = set_relation(s1, s2);
        return SetRelation.is(relation, actual);
    }

    public static HashMap<Object, ArrayList> multiset(Object... args) {
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
        HashMap<Object, ArrayList> m = new HashMap();
        if (anon != null) {
            int i = 0;
            for (Object o : list) {
                anon.setIterationContext(list, o, i);
                Object ret = anon.execute();
                if (!m.containsKey(ret)) {
                    m.put(ret, new ArrayList());
                }
                ArrayList values = m.get(ret);
                values.add(o);
                i++;
            }
            anon.removeIterationContext();
        } else {
            for (Object o : list) {
                if (!m.containsKey(o)) {
                    m.put(o, new ArrayList());
                }
                ArrayList values = m.get(o);
                values.add(o);
            }
        }
        return m;
    }

    public static HashMap<Object, int[]> list_diff(Object l, Object r) {
        Map<Object, ArrayList> mset1 = multiset(l);
        Map<Object, ArrayList> mset2 = multiset(r);
        return mset_diff(mset1,mset2);
    }

    public static HashMap<Object, int[]> mset_diff(Map<Object, ArrayList> mset1, Map<Object, ArrayList> mset2) {
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

    public static SetRelation mset_relation(Map<Object, ArrayList> mset1, Map<Object, ArrayList> mset2) {
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

    public static boolean is_mset_relation(Map<Object, ArrayList> mset1, Map<Object, ArrayList> mset2, String relation) {
        SetRelation actual = mset_relation(mset1, mset2);
        return SetRelation.is(relation, actual);
    }

    public static List list_i(Object l1, Object l2) {
        HashMap m1 = multiset(l1);
        HashMap m2 = multiset(l2);
        HashMap<Object, int[]> d = mset_diff(m1, m2);
        ArrayList l = new ArrayList();
        for (Object o : d.keySet()) {
            int[] var = d.get(o);
            int t = Math.min(var[0], var[1]);
            for (int i = 0; i < t; i++) {
                l.add(o);
            }
        }
        return l;
    }

    public static List list_u(Object l1, Object l2) {
        HashMap m1 = multiset(l1);
        HashMap m2 = multiset(l2);
        HashMap<Object, int[]> d = mset_diff(m1, m2);
        ArrayList l = new ArrayList();
        for (Object o : d.keySet()) {
            int[] var = d.get(o);
            int t = Math.max(var[0], var[1]);
            for (int i = 0; i < t; i++) {
                l.add(o);
            }
        }
        return l;
    }

    public static List list_d(Object l1, Object l2) {
        HashMap m1 = multiset(l1);
        HashMap m2 = multiset(l2);
        HashMap<Object, int[]> d = mset_diff(m1, m2);
        ArrayList l = new ArrayList();
        for (Object o : d.keySet()) {
            int[] var = d.get(o);
            int t = var[0] - var[1];
            for (int i = 0; i < t; i++) {
                l.add(o);
            }
        }
        return l;
    }

    public static List list_sym_d(Object l1, Object l2) {
        List i = list_i(l1, l2);
        List u = list_u(l1, l2);
        return list_d(u, i);
    }

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
        ArrayList r = new ArrayList();
        for (int i = 0; i < ls; i++) {
            ArrayList item = TypeUtility.from(left.get(i));
            for (int j = 0; j < rs; j++) {
                ArrayList l = new ArrayList(item);
                l.add(right.get(j));
                r.add(l);
            }
        }
        return r;
    }

    public static List join_c(Object... args) {
        Interpreter.AnonymousParam anon = null;
        ArrayList join = new ArrayList();
        if (args.length > 1) {
            if (args[0] instanceof Interpreter.AnonymousParam) {
                anon = (Interpreter.AnonymousParam) args[0];
                args = TypeUtility.shiftArrayLeft(args, 1);
            }
        }
        // the list of lists over which join would happen
        ArrayList[] argsList = new ArrayList[args.length];

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
        if (anon != null) {
            anon.setIterationContext(argsList, tuple, c++);
            Object r = anon.execute();
            if (TypeUtility.castBoolean(r, false)) {
                ArrayList t = new ArrayList();
                for (int i = 0; i < tuple.length; i++) {
                    t.add(tuple[i]);
                }
                join.add(t);
            }
        } else {
            ArrayList t = new ArrayList();
            for (int i = 0; i < tuple.length; i++) {
                t.add(tuple[i]);
            }
            join.add(t);
        }

        boolean emptied = false;

        while (true) {

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
                    ArrayList t = new ArrayList();
                    for (int i = 0; i < tuple.length; i++) {
                        t.add(tuple[i]);
                    }
                    join.add(t);
                }
            } else {
                ArrayList t = new ArrayList();
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
        if (c2 instanceof Collection || c2.getClass().isArray()) {
            if (c1 != null && (c1 instanceof Collection || c1.getClass().isArray())) {
                Map m1 = multiset(c1);
                Map m2 = multiset(c2);
                Map diff = mset_diff(m2, m1);
                for (Object key : diff.keySet()) {
                    int[] val = (int[]) diff.get(key);
                    if (val[0] == 0) {
                        return false;
                    }
                }
                return true;
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
