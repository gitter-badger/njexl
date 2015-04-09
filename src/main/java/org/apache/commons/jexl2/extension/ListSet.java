package org.apache.commons.jexl2.extension;

import java.util.*;

/**
 * Create a awesome Set that is also having capability of indexers like a list
 * Created by noga on 15/03/15.
 */
public class ListSet<T> extends HashSet<T> implements List<T> {

    ArrayList<T> behind;

    public ListSet() {
        this.behind = new ArrayList();
    }

    public ListSet(Collection<? extends T> c) {
        this();
        this.addAll(c);
    }

    public ListSet(int initialCapacity) {
        super(initialCapacity);
        this.behind = new ArrayList(initialCapacity);
    }

    public ListSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.behind = new ArrayList(initialCapacity);
    }

    @Override
    public boolean add(T t) {
        if ( super.add(t)){
            behind.add(t);
            return true ;
        }
        return false ;
    }

    @Override
    public void clear() {
        behind.clear();
        super.clear();
    }

    @Override
    public boolean remove(Object o) {
        if ( super.remove(o) ){
            behind.remove(o);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if( super.removeAll(c) ){
            behind.clear();
            behind.addAll(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int size = size() ;
        for(T t : c){
            this.add(t);
        }
        return size != size();
    }
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        int size = size() ;
        int i = index ;
        for(T t : c){
            this.add(i++, t);
        }
        return size != size();
    }

    /***
     * This is so that I can still index x['xxx'] and get a true false as if in contains
     * @param o
     * @return
     */
    public boolean get(Object o){
        return super.contains(o);
    }

    @Override
    public T get(int index) {
        return behind.get(index);
    }

    @Override
    public T set(int index, T element) {
        if ( super.contains(element)){
            behind.remove(index);
        }
        super.add(element);
        behind.set(index,element);
        return element;
    }

    @Override
    public void add(int index, T element) {
        if ( super.contains(element)){
            behind.remove(element);
        }
        behind.add(index,element);
    }

    @Override
    public T remove(int index) {
        T t = behind.remove(index);
        super.remove(t);
        return t;
    }

    @Override
    public int indexOf(Object o) {
        if ( !super.contains(o)){
            return -1;
        }
        return behind.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return behind.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return behind.subList(fromIndex,toIndex);
    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("S{ ");
        for(Object o : behind){
            buffer.append(o).append(",");
        }
        String s = buffer.toString();
        s = s.substring(0,s.length()-1);
        s += " }";
        return s;
    }
}
