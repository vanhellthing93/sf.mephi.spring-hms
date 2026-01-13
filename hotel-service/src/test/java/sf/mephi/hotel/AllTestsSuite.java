package sf.mephi.hotel;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Hotel Service - All Tests")
@SelectPackages({
        "sf.mephi.hotel.service",
        "sf.mephi.hotel.controller",
        "sf.mephi.hotel.exception",
        "sf.mephi.hotel.entity"
})
public class AllTestsSuite {
}
