package com.noga.njexl.spark.impl;

import com.noga.njexl.lang.extension.iterators.YieldedIterator;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.RDD;
import scala.collection.Seq;
import scala.reflect.ClassTag;

/**
 * Created by noga on 08/11/15.
 */
public class XSparkContext extends SparkContext {

    public XSparkContext() {
        super();
    }

    public XSparkContext(SparkConf conf){
        super(conf);
    }

    public XSparkContext(String master, String appName) {
        super(master, appName);
    }

    public XSparkContext(String master, String appName, String sparkHome) {
        super(master, appName, sparkHome);
    }

    public XJavaRDD binaryFiles(String path) {
        RDD rdd = super.binaryFiles(path, defaultMinPartitions() );
        return new XJavaRDD(rdd);
    }

    public XJavaRDD wholeTextFiles(String path) {
        RDD rdd = super.wholeTextFiles(path, defaultMinPartitions());
        return new XJavaRDD(rdd);
    }

    public XJavaRDD objectFile(String path ) {
        RDD rdd = super.objectFile(path, defaultMinPartitions(), ScalaInteract.$TAG );
        return new XJavaRDD(rdd);
    }

    public XJavaRDD textFile(String path) {
        RDD rdd = super.textFile(path, defaultMinPartitions());
        return new XJavaRDD(rdd);
    }

    public XJavaRDD parallelize(Object seq, int numSlices) {
        RDD rdd = super.parallelize(ScalaInteract.$s(seq), numSlices, ScalaInteract.$TAG);
        return new XJavaRDD(rdd);
    }

    public XJavaRDD parallelize(Object seq) {
        return parallelize(seq, defaultParallelism());
    }
}
