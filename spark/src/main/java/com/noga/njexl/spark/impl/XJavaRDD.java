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

import com.noga.njexl.lang.Interpreter.AnonymousParam;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaFutureAction;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import com.noga.njexl.spark.impl.AnonymousFunction.* ;
import org.apache.spark.rdd.RDD;

/**
 * Created by noga on 08/11/15.
 */
public class XJavaRDD extends JavaRDD {

    public XJavaRDD(RDD rdd){
        super(rdd, ScalaInteract.$TAG );
    }

    public XJavaRDD(JavaRDD rdd){
        super(rdd.rdd(), ScalaInteract.$TAG );
    }

    public XJavaRDD filter(AnonymousParam a) {
        JavaRDD jdd =  super.filter(new XFunction(a));
        return new XJavaRDD(jdd);
    }

    public XJavaRDD sortBy(AnonymousParam a, boolean ascending, int numPartitions) {
        JavaRDD jdd = super.sortBy(new XFunction(a), ascending, numPartitions);
        return new XJavaRDD(jdd);
    }

    public XJavaRDD sortBy(AnonymousParam a, boolean ascending) {
        return sortBy(a,ascending, context().defaultMinPartitions() );
    }

    public XJavaRDD sortBy(AnonymousParam a) {
        return sortBy(a,false);
    }

    public XJavaRDD map(AnonymousParam a) {
        JavaRDD jdd = super.map(new XFunction(a));
        return new XJavaRDD(jdd);
    }

    public XJavaRDD mapPartitionsWithIndex(AnonymousParam a, boolean preservesPartitioning) {
        JavaRDD jdd = super.mapPartitionsWithIndex(new XFunction2(a), preservesPartitioning);
        return new XJavaRDD(jdd);
    }

    public JavaDoubleRDD mapToDouble(AnonymousParam a) {
        return super.mapToDouble( new XDoubleFunction(a));
    }

    public XJavaPairRDD mapToPair(AnonymousParam a) {
        JavaPairRDD jpdd = super.mapToPair(new XPairFunction(a));
        return new XJavaPairRDD(jpdd.rdd());
    }

    public XJavaRDD flatMap(AnonymousParam a) {
        JavaRDD jrdd = super.flatMap( new XFlatMapFunction(a));
        return new XJavaRDD(jrdd);
    }

    public JavaDoubleRDD flatMapToDouble(AnonymousParam a) {
        return super.flatMapToDouble(new XFlatMapFunction(a));
    }


    public XJavaPairRDD flatMapToPair(AnonymousParam a) {
        JavaPairRDD jpdd =  super.flatMapToPair(new XFlatMapFunction(a));
        return new XJavaPairRDD(jpdd.rdd());
    }


    public XJavaRDD mapPartitions(AnonymousParam a,boolean preservesPartitioning) {
        JavaRDD jrdd = super.mapPartitions(new XFlatMapFunction(a),preservesPartitioning);
        return new XJavaRDD(jrdd);
    }

    public XJavaRDD mapPartitions(AnonymousParam a ) {
        return mapPartitions(a,true);
    }

    public JavaDoubleRDD mapPartitionsToDouble(AnonymousParam a,boolean preservesPartitioning) {
        return super.mapPartitionsToDouble(new XFlatMapFunction(a),preservesPartitioning);
    }
    public JavaDoubleRDD mapPartitionsToDouble(AnonymousParam a) {
        return mapPartitionsToDouble(a,true);
    }

    public XJavaPairRDD mapPartitionsToPair(AnonymousParam a,boolean preservesPartitioning) {
        JavaPairRDD jpdd =  super.mapPartitionsToPair(new XFlatMapFunction(a),preservesPartitioning);
        return new XJavaPairRDD(jpdd.rdd());
    }

    public XJavaPairRDD mapPartitionsToPair(AnonymousParam a) {
        return mapPartitionsToPair(a,true);
    }

    public void foreachPartition(AnonymousParam a) {
        super.foreachPartition(new XVoidFunction(a));
    }

    public XJavaPairRDD groupBy(AnonymousParam a,int numPartitions) {
        JavaPairRDD jpdd = super.groupBy(new XFunction(a),numPartitions);
        return new XJavaPairRDD(jpdd.rdd());
    }

    public XJavaPairRDD groupBy(AnonymousParam a) {
        return groupBy(a, context().defaultMinPartitions());
    }

    public Object reduce(AnonymousParam a) {
        return super.reduce(new XFunction2(a));
    }

    public Object treeReduce(AnonymousParam a, int depth) {
        return super.treeReduce(new XFunction2(a), depth);
    }

    public Object treeReduce(AnonymousParam a) {
        return super.treeReduce(new XFunction2(a));
    }

    public Object fold(Object zeroValue, AnonymousParam a) {
        return super.fold(zeroValue, new XFunction2(a));
    }

    @Override
    public Object aggregate(Object zeroValue, Function2 seqOp, Function2 combOp) {
        return super.aggregate(zeroValue, seqOp, combOp);
    }

    @Override
    public Object treeAggregate(Object zeroValue, Function2 seqOp, Function2 combOp, int depth) {
        return super.treeAggregate(zeroValue, seqOp , combOp, depth);
    }

    @Override
    public Object treeAggregate(Object zeroValue, Function2 seqOp, Function2 combOp) {
        return super.treeAggregate(zeroValue, seqOp, combOp);
    }


    public XJavaPairRDD keyBy(AnonymousParam a) {
        JavaPairRDD jrdd = super.keyBy(new XFunction(a));
        return new XJavaPairRDD(jrdd.rdd());
    }

    public JavaFutureAction<Void> foreachAsync(AnonymousParam a) {
        return super.foreachAsync(new XVoidFunction(a));
    }

    public JavaFutureAction<Void> foreachPartitionAsync(AnonymousParam a) {
        return super.foreachPartitionAsync(new XVoidFunction(a));
    }
}
