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

package com.noga.njexl.tests;

import com.noga.njexl.testing.api.Annotations.*;
import com.noga.njexl.testing.api.junit.JClassRunner;
import org.junit.runner.RunWith;

/**
 * Created by noga on 27/05/15.
 */

@RunWith(JClassRunner.class)
@NApiService(base = "samples/")
@NApiServiceCreator(name="c1")
public class NApiAnnotationSample {

    @NApiServiceInit(creator = "c1")
    public NApiAnnotationSample(){}

    @NApi(dataSource = "UIData.xlsx", dataTable = "Sheet1" ,
            before = "pre.jexl", after = "post.jexl" )
    @NApiThread
    public void testMethod(String a, int b) {
        System.out.printf("%s , %d\n", a, b);
    }

}