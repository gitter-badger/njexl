package noga.commons.njexl.testing.reporting;

import noga.commons.njexl.testing.TestSuiteRunner;

import java.util.ArrayList;

/**
 * Created by noga on 18/04/15.
 */
public interface Reporter extends TestSuiteRunner.TestRunEventListener {

    void init(ArrayList<String> args);

    void location(String location);

    String location();

    String name();
}
