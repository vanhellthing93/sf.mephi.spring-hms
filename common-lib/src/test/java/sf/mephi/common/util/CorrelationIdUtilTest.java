package sf.mephi.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdUtilTest {

    @BeforeEach
    void setUp() {
        CorrelationIdUtil.clearCorrelationId();
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clearCorrelationId();
    }

    @Test
    void generateCorrelationId_ShouldReturnUUIDFormat() {
        String id = CorrelationIdUtil.generateCorrelationId();
        assertNotNull(id);
        assertTrue(id.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"));
    }

    @Test
    void setGetClearCorrelationId_ShouldWorkWithMDC() {
        String testId = "test-uuid-123";
        CorrelationIdUtil.setCorrelationId(testId);
        assertEquals(testId, CorrelationIdUtil.getCorrelationId());
        assertEquals(testId, MDC.get(CorrelationIdUtil.CORRELATION_ID_MDC_KEY));

        CorrelationIdUtil.clearCorrelationId();
        assertNull(CorrelationIdUtil.getCorrelationId());
        assertNull(MDC.get(CorrelationIdUtil.CORRELATION_ID_MDC_KEY));
    }
}
