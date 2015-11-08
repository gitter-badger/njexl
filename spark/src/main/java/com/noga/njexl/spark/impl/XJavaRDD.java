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

    @Override
    public JavaRDD filter(Function f) {
        return super.filter(f);
    }

    @Override
    public JavaRDD sortBy(Function f, boolean ascending, int numPartitions) {
        return super.sortBy(f, ascending, numPartitions);
    }

    @Override
    public JavaRDD map(Function f) {
        return super.map(f);
    }

    @Override
    public JavaRDD mapPartitionsWithIndex(Function2 f, boolean preservesPartitioning) {
        return super.mapPartitionsWithIndex(f, preservesPartitioning);
    }

    @Override
    public JavaDoubleRDD mapToDouble(DoubleFunction f) {
        return super.mapToDouble(f);
    }

    public XJavaPairRDD mapToPair(AnonymousParam a) {
        JavaPairRDD jpdd = super.mapToPair(new XPairFunction(a));
        return new XJavaPairRDD(jpdd.rdd());
    }

    public XJavaRDD flatMap(AnonymousParam a) {
        JavaRDD jrdd = super.flatMap( new XFlatMapFunction(a));
        return new XJavaRDD(jrdd);
    }

    @Override
    public JavaDoubleRDD flatMapToDouble(DoubleFlatMapFunction f) {
        return super.flatMapToDouble(f);
    }

    @Override
    public JavaPairRDD flatMapToPair(PairFlatMapFunction f) {
        return super.flatMapToPair(f);
    }

    @Override
    public JavaRDD mapPartitions(FlatMapFunction f) {
        return super.mapPartitions(f);
    }

    @Override
    public JavaRDD mapPartitions(FlatMapFunction f, boolean preservesPartitioning) {
        return super.mapPartitions(f, preservesPartitioning);
    }

    @Override
    public JavaDoubleRDD mapPartitionsToDouble(DoubleFlatMapFunction f) {
        return super.mapPartitionsToDouble(f);
    }

    @Override
    public JavaPairRDD mapPartitionsToPair(PairFlatMapFunction f) {
        return super.mapPartitionsToPair(f);
    }

    @Override
    public JavaDoubleRDD mapPartitionsToDouble(DoubleFlatMapFunction f, boolean preservesPartitioning) {
        return super.mapPartitionsToDouble(f, preservesPartitioning);
    }

    @Override
    public JavaPairRDD mapPartitionsToPair(PairFlatMapFunction f, boolean preservesPartitioning) {
        return super.mapPartitionsToPair(f, preservesPartitioning);
    }

    @Override
    public void foreachPartition(VoidFunction f) {
        super.foreachPartition(f);
    }

    @Override
    public JavaPairRDD groupBy(Function f) {
        return super.groupBy(f);
    }

    @Override
    public JavaPairRDD groupBy(Function f, int numPartitions) {
        return super.groupBy(f, numPartitions);
    }

    @Override
    public Object reduce(Function2 f) {
        return super.reduce(f);
    }

    @Override
    public Object treeReduce(Function2 f, int depth) {
        return super.treeReduce(f, depth);
    }

    @Override
    public Object treeReduce(Function2 f) {
        return super.treeReduce(f);
    }

    @Override
    public Object fold(Object zeroValue, Function2 f) {
        return super.fold(zeroValue, f);
    }

    @Override
    public Object aggregate(Object zeroValue, Function2 seqOp, Function2 combOp) {
        return super.aggregate(zeroValue, seqOp, combOp);
    }

    @Override
    public Object treeAggregate(Object zeroValue, Function2 seqOp, Function2 combOp, int depth) {
        return super.treeAggregate(zeroValue, seqOp, combOp, depth);
    }

    @Override
    public Object treeAggregate(Object zeroValue, Function2 seqOp, Function2 combOp) {
        return super.treeAggregate(zeroValue, seqOp, combOp);
    }

    @Override
    public JavaPairRDD keyBy(Function f) {
        return super.keyBy(f);
    }

    @Override
    public JavaFutureAction<Void> foreachAsync(VoidFunction f) {
        return super.foreachAsync(f);
    }

    @Override
    public JavaFutureAction<Void> foreachPartitionAsync(VoidFunction f) {
        return super.foreachPartitionAsync(f);
    }
}
