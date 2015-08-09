/**
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

package com.noga.njexl.testing.reporting;

import com.noga.njexl.testing.TestAssert.*;
import com.noga.njexl.testing.TestSuiteRunner.*;

import java.util.List;

/**
 * A generic reporter infra
 */
public interface Reporter extends TestRunEventListener, AssertionEventListener {

    /**
     * Arbitrary args to pass for initializing the reporter
     * @param args a list of arguments, all string
     */
    void init(List<String> args);

    /**
     * Where we want the log to be created
     * @param location the path to the log
     */
    void location(String location);

    /**
     * Gets back the current path of the log
     * @return location of the log
     */
    String location();

    /**
     * What name this reporter has
     * @return name of the reporter
     */
    String name();
}
