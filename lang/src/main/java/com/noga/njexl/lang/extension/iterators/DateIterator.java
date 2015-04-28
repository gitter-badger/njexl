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

package com.noga.njexl.lang.extension.iterators;

import org.joda.time.*;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.time.*;
import java.util.Iterator;

/**
 * Created by noga on 28/04/15.
 */
public class DateIterator implements Iterator{

    public static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000 ;

    public static final Duration DAY = new Duration( DAY_IN_MILLIS );

    public static Duration parseDuration(String text){
        Period p = ISOPeriodFormat.standard().parsePeriod(text);
        return p.toStandardDuration();
    }

    public final DateTime start;

    public final DateTime end;

    public final Duration interval ;

    public final Duration duration ;

    protected DateTime cur;

    /**
     *
     * @return elapsed second between end and start
     */
    public int seconds(){
        return Seconds.secondsBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getSeconds();
    }

    /**
     *
     * @return elapsed minutes between end and start
     */
    public int minutes(){
        return Minutes.minutesBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getMinutes();
    }

    /**
     *
     * @return elapsed hours between end and start
     */
    public int hours(){
        return Hours.hoursBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getHours();
    }

    /**
     *
     * @return  elapsed days between end and start
     */
    public int days(){
        return Days.daysBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getDays() ;
    }

    /**
     *
     * @return  elapsed weeks between end and start
     */
    public int weeks(){
        return Weeks.weeksBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getWeeks();
    }

    /**
     *
     * @return  elapsed months between end and start
     */
    public int months(){
        return Months.monthsBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getMonths();
    }

    /**
     *
     * @return  elapsed years between end and start
     */
    public int years(){
        return Years.yearsBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getYears();
    }

    public DateIterator(DateTime end){
        this(end, new DateTime());
    }

    public DateIterator(DateTime end, DateTime start){
        this(end,start, DAY );
    }

    public DateIterator(DateTime end, DateTime start, Duration interval){
        this.end  = end ;
        this.start = start ;
        this.interval = interval ;
        // inclusive
        this.cur = this.start  ;
        this.duration = new Duration(this.start, this.end);
    }

    @Override
    public boolean hasNext() {
        return cur.plus(interval).compareTo( end ) <= 0 ;
    }

    @Override
    public Object next() {
        cur = cur.plus(interval);
        return cur;
    }
}
