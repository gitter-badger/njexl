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

package com.noga.njexl.lang.extension.iterators;

import org.joda.time.*;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by noga on 28/04/15.
 */
public class DateIterator extends YieldedIterator{

    public static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000 ;

    public static final Duration DAY = new Duration( DAY_IN_MILLIS );

    public static Duration parseDuration(String text){
        Period p = ISOPeriodFormat.standard().parsePeriod(text);
        return p.toStandardDuration();
    }

    /**
     * http://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java
     * @param start starting date
     * @param end  ending date
     * @return number of week days in between
     */
    static long weekDays(Date start, Date end){
        //Ignore argument check

        Calendar c1 = Calendar.getInstance();
        c1.setTime(start);
        int w1 = c1.get(Calendar.DAY_OF_WEEK);
        c1.add(Calendar.DAY_OF_WEEK, -w1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(end);
        int w2 = c2.get(Calendar.DAY_OF_WEEK);
        c2.add(Calendar.DAY_OF_WEEK, -w2);

        //end Saturday to start Saturday
        long days = (c2.getTimeInMillis()-c1.getTimeInMillis())/(1000*60*60*24);
        long daysWithoutWeekendDays = days-(days*2/7);

        // Adjust w1 or w2 to 0 since we only want a count of *weekdays*
        // to add onto our daysWithoutWeekendDays
        if (w1 == Calendar.SUNDAY) {
            w1 = Calendar.MONDAY;
        }

        if (w2 == Calendar.SUNDAY) {
            w2 = Calendar.MONDAY;
        }

        return daysWithoutWeekendDays-w1+w2;
    }

    public final DateTime b;

    public final DateTime e;

    public final Duration s ;

    public final Duration duration ;

    protected DateTime cur;

    public final int seconds;

    public final int minutes;

    public final int hours ;

    public final int days ;

    public final int weeks ;

    public final int months ;

    public final int years ;

    public final long weekDays;

    public final String stringRep;

    public final int hashCode;

    public DateIterator(DateTime end){
        this(end, new DateTime());
    }

    public DateIterator(DateTime end, DateTime start){
        this(end,start, DAY );
    }

    public DateIterator(DateTime end, DateTime start, Duration interval){
        this.e  = end ;
        this.b = start ;
        this.s = interval ;
        decreasing = this.b.compareTo(this.e) > 0 ;
        // inclusive
        if ( decreasing ){
            this.cur = this.b.plus(this.s) ;
        }else{
            this.cur = this.b.minus(this.s) ;
        }
        this.duration = new Duration(this.b, this.e);
        years = Years.yearsBetween(start,end).getYears();
        months = Months.monthsBetween(start, end).getMonths();
        weeks = Weeks.weeksBetween(start, end).getWeeks();
        days = Days.daysBetween(start, end).getDays();
        hours = Hours.hoursBetween(start, end).getHours();
        minutes = Minutes.minutesBetween(start, end).getMinutes();
        seconds = Seconds.secondsBetween(start, end).getSeconds();
        weekDays = weekDays( start.toDate(), end.toDate());
        stringRep = String.format("%s : %s : %s", start.toDate(), end.toDate(), interval);
        hashCode =  ( 31* (( 31 * end.hashCode() ) + start.hashCode()) + interval.hashCode());
    }


    @Override
    public void reset() {
        // inclusive
        if ( decreasing ){
            this.cur = this.b.plus(this.s) ;
        }else{
            this.cur = this.b.minus(this.s) ;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof DateIterator) ) return false ;
        DateIterator o = (DateIterator)obj;
        if ( e.equals(o.e) && b.equals(o.b) && s.equals(o.s) ) return true ;
        return false ;
    }

    @Override
    public int hashCode() {
        return hashCode ;
    }

    @Override
    public boolean hasNext() {
        if ( decreasing ){
            return cur.minus(s).compareTo( e ) >=0 ;
        }
        return cur.plus(s).compareTo( e ) <= 0 ;
    }

    @Override
    public Object next() {
        if ( decreasing ){
            cur = cur.minus(s);
        }else {
            cur = cur.plus(s);
        }
        return cur;
    }

    @Override
    public String toString(){
        return stringRep ;
    }

    @Override
    public YieldedIterator inverse() {
        return new DateIterator(this.b, this.e, this.s );
    }
}
