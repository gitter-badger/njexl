/*
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

package com.noga.njexl.spark;

import com.noga.njexl.lang.Interpreter.NamedArgs;
import com.noga.njexl.spark.impl.ScalaInteract;
import com.noga.njexl.spark.impl.XSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.RDD;

/**
 * Created by noga on 07/11/15.
 */
public class XSpark {

    public static SparkConf conf(Object...args ){
        SparkConf conf = new SparkConf();
        if ( args.length == 0 ){
            return conf;
        }
        for ( Object o : args ){
            if ( o instanceof NamedArgs){
                NamedArgs na = (NamedArgs)o;
                String value = String.valueOf(na.value) ;
                switch (na.name.toLowerCase() ){
                    case "app" :
                        conf.setAppName(value);
                        break;
                    case "home":
                        conf.setSparkHome(value);
                        break;
                    case "master":
                        conf.setMaster(value);
                    default:
                        break;
                }
            }
        }
        return conf;
    }

    public static SparkContext ctx(Object...args){

        switch (args.length ){
            case 1:
                return new XSparkContext( (SparkConf)args[0] );
            case 2:
                return new XSparkContext( String.valueOf(args[0]), String.valueOf(args[1]) );
            case 3 :
                return new XSparkContext( String.valueOf(args[0]), String.valueOf(args[1]), String.valueOf(args[2]) );
            default:
        }
        return new SparkContext();
    }

    public static JavaRDD jc(Object... args){
        if ( args.length ==  0 ) return null;
        if ( args[0] instanceof RDD){
            return new JavaRDD((RDD)args[0], ScalaInteract.$TAG );
        }
        return null;
    }

    public static JavaPairRDD jp(Object... args){
        if ( args.length ==  0 ) return null;
        if ( args[0] instanceof RDD){
            return new JavaPairRDD((RDD)args[0], ScalaInteract.$TAG,ScalaInteract.$TAG );
        }
        return null;
    }
}
