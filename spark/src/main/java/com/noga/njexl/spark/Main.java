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

package com.noga.njexl.spark;

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.JexlEngine;
import com.noga.njexl.lang.Script;
import com.noga.njexl.spark.impl.ScalaInteract;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Map;


/**
 * A Main class to have command line interface, if need be.
 * Created by noga on 15/04/15.
 */
public class Main {


    private static final FlatMapFunction<String, String> WORDS_EXTRACTOR =
            new FlatMapFunction<String, String>() {
                @Override
                public Iterable<String> call(String s) throws Exception {
                    return Arrays.asList(s.split(" "));
                }
            };

    private static final PairFunction<String, String, Integer> WORDS_MAPPER =
            new PairFunction<String, String, Integer>() {
                @Override
                public Tuple2<String, Integer> call(String s) throws Exception {
                    return new Tuple2<String, Integer>(s, 1);
                }
            };

    private static final Function2<Integer, Integer, Integer> WORDS_REDUCER =
            new Function2<Integer, Integer, Integer>() {
                @Override
                public Integer call(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            };


    private Main() {
    }

    public static void ain(String[] args) {

        String inputFileName = "samples/big.txt" ;
        String outputDirName = "output" ;

        SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> file = context.textFile(inputFileName);
        JavaRDD<String> words = file.flatMap(WORDS_EXTRACTOR);
        JavaPairRDD<String, Integer> pairs = words.mapToPair(WORDS_MAPPER);
        JavaPairRDD<String, Integer> counter = pairs.reduceByKey(WORDS_REDUCER);

        counter.saveAsTextFile(outputDirName);
    }

    public static void main(String[] args) throws Exception {
        JexlContext  context = com.noga.njexl.lang.Main.getContext();
        Map map = com.noga.njexl.lang.Main.getFunction(context);
        map.put("spark", XSpark.class );
        context.set( ScalaInteract.MY_NAME , ScalaInteract.class );
        JexlEngine engine = com.noga.njexl.lang.Main.getJexl( context );
        engine.setFunctions( map );
        Script s = engine.importScript("samples/wc.jxl");
        s.execute(context);
    }
}
