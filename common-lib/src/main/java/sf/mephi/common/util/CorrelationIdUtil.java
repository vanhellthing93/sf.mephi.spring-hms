package sf.mephi.common.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationIdUtil {
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private CorrelationIdUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }

    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }
}
