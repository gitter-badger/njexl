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

import com.noga.njexl.testing.TestAssert;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.TestSuiteRunner;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of a @{Reporter} - simple text one
 */
public class SimpleTextReporter implements Reporter {

    /**
     * Gets a reporter
     * @param type what sort of sync we are using? See @{Sync}
     * @param location where we want it to go
     * @return a reporter initialized properly
     */
    public static SimpleTextReporter reporter(Sync type, String location){
        SimpleTextReporter reporter = new SimpleTextReporter();
        ArrayList<String> list = new ArrayList();
        list.add(type.toString());
        reporter.init(list);
        reporter.location(location);
        return reporter ;
    }

    /**
     * The Sync type
     */
    public enum Sync{
        /**
         * Console type - stdout and stderr
         */
        CONSOLE,
        /**
         * File type
         */
        FILE,
        /**
         * Just ignores the logging
         */
        NULL
    }

    protected PrintStream printStream ;

    protected String location;

    protected String fileName = "TextReport.txt" ;

    Sync type;

    @Override
    public String location() {
        return location;
    }

    @Override
    public void init(List<String> args) {
        if ( args.size() == 0 ){
            type = Sync.CONSOLE ;
            fileName = "" ;
            return;
        }
        type = Enum.valueOf(Sync.class, args.get(0));
        if ( args.size() > 1 ){
            fileName = args.get(2);
        }
    }

    @Override
    public void location(String location) {
        this.location = "";
        printStream = System.out ;
        if (Sync.FILE == type ) {
            try {
                this.location = location +"/" +  name();
                printStream = new PrintStream(this.location);
            } catch (Exception e) {
                this.location = "";
            }
        }
    }

    @Override
    public String name() {
        return fileName ;
    }

    @Override
    public void onAssertion(TestAssert.AssertionEvent assertionEvent) {
        printStream.printf("%s|%s:%d|@ %s\n", Utils.ts(), dsTable,row,assertionEvent);
    }

    String dsTable;
    int row;

    @Override
    public void onTestRunEvent(TestSuiteRunner.TestRunEvent testRunEvent) {
        if ( Sync.NULL == type ){
            return;
        }
        switch (testRunEvent.type){
            case BEFORE_FEATURE:
            case AFTER_FEATURE:
                printStream.printf("%s|%s|%s\n", Utils.ts(), testRunEvent.feature, testRunEvent.type);
                break;

            case IGNORE_TEST:
                dsTable = testRunEvent.table.name() ;
                row = testRunEvent.row ;
                printStream.printf("%s|%s|%s:%d|%s\n", Utils.ts(),
                        testRunEvent.feature, dsTable, row , testRunEvent.type);
                break;

            case BEFORE_TEST:
                dsTable = testRunEvent.table.name() ;
                row = testRunEvent.row ;
            case ABORT_TEST:
                printStream.printf("%s|%s|%s:%d|%s\n", Utils.ts(),
                        testRunEvent.feature, dsTable, row , testRunEvent.type);
                break;

            case OK_TEST:
                printStream.printf("%s|%s|%s:%d|%s >o> %s \n",Utils.ts(),
                        testRunEvent.feature,
                        dsTable,row , testRunEvent.type,testRunEvent.runObject );
                break;
            case ERROR_TEST:
                printStream.printf("%s|%s|%s:%d|%s >e> %s \n",Utils.ts(),
                        testRunEvent.feature,
                        dsTable, row , testRunEvent.type,testRunEvent.error );
                break;
        }
    }
}
