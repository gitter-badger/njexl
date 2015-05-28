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

import com.noga.njexl.testing.api.Annotations;
import com.noga.njexl.testing.api.Annotations.* ;
import com.noga.njexl.testing.api.ArgConverter;
import com.noga.njexl.testing.api.CallContainer;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by noga on 28/05/15.
 */
public class JClassRunner extends Suite {

    protected final List<Runner> children;

    public JClassRunner(Class<?> clazz) throws Exception {
        super(clazz, Collections.<Runner>emptyList());
        children = new ArrayList<>();
        createRunners();
    }

    Object service(NApiServiceCreator creator, Class clazz) throws Exception{
        // we would do fancy creation later...
        return clazz.newInstance();
    }

    protected List<JApiRunner> runners(MethodRunInformation mi, Object service) throws Exception{
        ArrayList l = new ArrayList();
        ArgConverter converter = new ArgConverter(mi);
        CallContainer[] containers = converter.allContainers();
        for ( int i = 0 ; i < containers.length; i++ ){
            containers[i].service = service ;
            containers[i].pre = mi.base + "/" + mi.nApi.before() ;
            containers[i].post = mi.base + "/" + mi.nApi.after() ;
            JApiRunner runner = JApiRunner.createRunner( containers[i]);
            l.add(runner);
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