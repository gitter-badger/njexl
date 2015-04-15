package noga.commons.njexl.testing.ui;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Created by noga on 15/04/15.
 */

@XStreamAlias("testSuite")
public class WebTestSuite {

    @XStreamAsAttribute
    public String version;


}
