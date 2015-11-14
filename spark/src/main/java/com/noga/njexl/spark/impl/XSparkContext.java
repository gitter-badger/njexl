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
