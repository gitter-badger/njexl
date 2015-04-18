package noga.commons.njexl.testing.reporting;

import noga.commons.njexl.testing.TestAssert;
import noga.commons.njexl.testing.TestSuiteRunner;
import noga.commons.njexl.testing.Utils;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by noga on 18/04/15.
 */
public class SimpleTextReporter implements Reporter {

    public enum Sync{
        CONSOLE,
        FILE,
        NULL
    }

    protected PrintStream printStream ;

    protected String location;

    Sync type;

    @Override
    public String location() {
        return location;
    }

    @Override
    public void init(ArrayList<String> args) {
        if ( args.size() == 0 ){
            type = Sync.CONSOLE ;
            return;
        }
        type = Enum.valueOf(Sync.class, args.get(0));
    }

    @Override
    public void location(String location) {
        this.location = "";
        printStream = System.out ;
        if (Sync.FILE == type ) {
            try {
                this.location = location +"/" + Utils.ts() + "_" + name();
                printStream = new PrintStream(location);
                this.location = location;
            } catch (Exception e) {
                this.location = "";
            }
        }
    }

    @Override
    public String name() {
        return "TextReport.txt";
    }

    @Override
    public void onAssertion(TestAssert.AssertionEvent assertionEvent) {
        printStream.printf("%s|@ %s\n", Utils.ts(), assertionEvent);
    }

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
            case BEFORE_TEST:
            case ABORT_TEST:
            case IGNORE_TEST:
                printStream.printf("%s|%s|%s|%s:%d\n", Utils.ts(),
                        testRunEvent.feature, testRunEvent.type,
                        testRunEvent.table.name(), testRunEvent.row);

            case OK_TEST:
                printStream.printf("%s|%s|%s|%s:%d >o> %s \n",Utils.ts(),
                        testRunEvent.feature, testRunEvent.type,
                        testRunEvent.table.name(), testRunEvent.row , testRunEvent.runObject );
                break;
            case ERROR_TEST:
                printStream.printf("%s|%s|%s|%s:%d >e> %s \n",Utils.ts(),
                        testRunEvent.feature, testRunEvent.type,
                        testRunEvent.table.name(), testRunEvent.row , testRunEvent.error );
                break;
        }
    }
}
