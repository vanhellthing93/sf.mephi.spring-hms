package sf.mephi.hotel;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;


@Suite
@SuiteDisplayName("Hotel Service - Unit Tests Only")
@SelectPackages({
        "sf.mephi.hotel.service",
        "sf.mephi.hotel.entity"
})
public class UnitTestsSuite {
}
