package com.noga.njexl.testing.reporting;

import com.noga.njexl.testing.TestAssert.*;
import com.noga.njexl.testing.TestSuiteRunner.*;

import java.util.ArrayList;

/**
 * Created by noga on 18/04/15.
 */
public interface Reporter extends TestRunEventListener, AssertionEventListener {

    void init(ArrayList<String> args);

    void location(String location);

    String location();

    String name();
}
