package com.noga.njexl.spark.impl;

import org.apache.spark.api.java.JavaPairRDD;
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


}
