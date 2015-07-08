/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.testing.api.junit;

import com.noga.njexl.testing.api.*;
import com.noga.njexl.testing.api.Annotations.* ;
import com.noga.njexl.testing.api.ServiceCreatorFactory.ServiceCreator ;
import com.noga.njexl.testing.dataprovider.collection.XStreamIterator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by noga on 28/05/15.
 */
public class JClassRunner extends Suite {

    /**
     * Runs a nApi Class using jUnit
     * @param clazz the class implementing nApi
     * @return result
     * @throws Exception in case of error
     */
    public static Result run(Class<?> clazz) throws Exception {
        JUnitCore c = new JUnitCore();
        JClassRunner suite = new JClassRunner( clazz );
        Result r = c.run(Request.runner(suite));
        return r;
    }

    protected final List<Runner> children;

    public JClassRunner(Class<?> clazz) throws Exception {
        super(clazz, Collections.<Runner>emptyList());
        children = new ArrayList<>();
        createRunners();
    }

    public Object service(NApiServiceCreator creator, Class clazz) throws Exception{
        ServiceCreator serviceCreator = ServiceCreatorFactory.creator(creator);
        Object service = serviceCreator.create(clazz);
        return service;
    }

    public static String dictEntry(String s){
        String[] pairs = s.split("=");
        if ( pairs.length != 2 ){
            return "null:null" ; // defaults
        }
        String ret = String.format("'%s' : '%s'", pairs[0],pairs[1]);
        return ret;
    }

    public static String globals(String[] arr){
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        if ( arr.length > 0 ) {
            buffer.append(dictEntry(arr[0]));
            for (int i = 1; i < arr.length; i++) {
                buffer.append(",");
                buffer.append(dictEntry(arr[i]));
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    protected List<JApiRunner> runners(MethodRunInformation mi, Object service) throws Exception{
        ArgConverter converter = new ArgConverter(mi);
        String globals =  globals(mi.nApi.globals());
        CallContainer[] containers = converter.allContainers();
        // update these containers with ready to run data
        for ( int i = 0 ; i < containers.length; i++ ){
            containers[i].service = service ;
            containers[i].pre = mi.base + "/" + mi.nApi.before() ;
            containers[i].post = mi.base + "/" + mi.nApi.after() ;
            containers[i].globals = globals ;
        }
        /*
          Now what is our strategy?
          How many runners are required?
           [1] All runners gets different Call Container
                : DTD : they gets executed one test data after another

           [2] All threads gets different Call Container
                : DCD : Every Call will get different input
        */
        // now create an iterator
        XStreamIterator<CallContainer> iterator = new XStreamIterator<>(containers);

        ArrayList l = new ArrayList();
        if ( mi.nApiThread.use() && mi.nApiThread.dcd() ){
            // do not worry, rewind if need be
            iterator.setMode(false);
            // no of tests are strictly the no of threads to spawn
            int totCalls = mi.nApiThread.numThreads() * mi.nApiThread.numCallPerThread()  ;
            int tests =  iterator.size() / totCalls ;
            if ( iterator.size() % totCalls  > 0 ) {
                tests += 1;
            }
            for ( int i = 0 ; i < tests ; i++ ){
                JApiRunner jApiRunner = JApiRunner.createRunner(-1, iterator, mi);
                l.add(jApiRunner);
            }
        }
        else{
           // tests are strictly the no of data rows
            for ( int i = 0 ; i < containers.length ; i++ ){
                JApiRunner jApiRunner = JApiRunner.createRunner(i, iterator, mi);
                l.add(jApiRunner);
            }
        }

        return l;
    }

    protected void createRunners() throws Exception{
        Class clazz = super.getTestClass().getJavaClass();
        NApiServiceCreator creator = Annotations.NApiServiceCreator(clazz);
        Object service = service(creator,clazz);
        List<MethodRunInformation> l = Annotations.runs(clazz);
        for ( MethodRunInformation mi : l ){
            List<JApiRunner> runners = runners( mi , service);
            children.addAll(runners);
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return children;
    }
}
