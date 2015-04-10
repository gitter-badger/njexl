package noga.commons.njexl.extension.iterators;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by noga on 31/03/15.
 */
public class StringIterator implements Iterator {

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

    public StringIterator(String text){
        this(text,text.length(),0,1);
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
