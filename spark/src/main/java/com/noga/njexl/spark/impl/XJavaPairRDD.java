/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.spark.impl;

import org.apache.spark.Partitioner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.rdd.RDD;
import com.noga.njexl.spark.impl.AnonymousFunction.* ;

/**
 * Created by noga on 08/11/15.
 */
public class XJavaPairRDD extends JavaPairRDD {

    public XJavaPairRDD(RDD rdd){
        super(rdd,ScalaInteract.$TAG, ScalaInteract.$TAG);
    }

    public XJavaPairRDD reduceByKey(Object a) {
        JavaPairRDD jrdd = super.reduceByKey(new XFunction2(a));
        return new XJavaPairRDD(jrdd.rdd());
    }

    public XJavaPairRDD aggregateByKey(Object zeroValue, int numPartitions, Object seqFunc, Object combFunc) {
        JavaPairRDD jrdd = super.aggregateByKey(zeroValue, numPartitions,
                new XFunction2(seqFunc) , new XFunction2(combFunc));
        return new XJavaPairRDD(jrdd.rdd());
    }

    public XJavaPairRDD aggregateByKey(Object zeroValue, Partitioner partitioner, Object seqFunc, Object combFunc) {
        JavaPairRDD jrdd =  super.aggregateByKey(zeroValue, partitioner,
                new XFunction2(seqFunc), new XFunction2(combFunc) );
        return new XJavaPairRDD(jrdd.rdd());
    }

    public JavaPairRDD aggregateByKey(Object zeroValue, Object seqFunc, Object combFunc) {
        return aggregateByKey(zeroValue, super.context().defaultMinPartitions() , seqFunc, combFunc);
    }

    @Override
    public JavaPairRDD cache() {
        JavaPairRDD jrdd = super.cache();
        return new XJavaPairRDD(jrdd.rdd());
    }
}
