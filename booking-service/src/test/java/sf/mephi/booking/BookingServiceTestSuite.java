package sf.mephi.booking;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test Suite for Booking Service
 * Runs all unit and integration tests
 */
@Suite
@SuiteDisplayName("Booking Service Test Suite")
@SelectPackages({
        "sf.mephi.booking.controller",
        "sf.mephi.booking.service",
        "sf.mephi.booking.mapper",
        "sf.mephi.booking.repository",
        "sf.mephi.booking.security",
        "sf.mephi.booking.client"
})
public class BookingServiceTestSuite {

}
