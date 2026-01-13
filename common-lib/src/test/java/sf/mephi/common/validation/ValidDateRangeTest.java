package sf.mephi.common.validation;

import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidDateRangeTest {

    @Test
    void mockAnnotation_ShouldWork() {
        ValidDateRange mockAnn = new ValidDateRange() {
            @Override public String start() { return "startDate"; }
            @Override public String end() { return "endDate"; }
            @Override public String message() { return "Custom msg"; }
            @Override public Class<?>[] groups() { return new Class[0]; }
            @Override public Class<? extends Payload>[] payload() { return new Class[0]; }
            @Override public Class<? extends Annotation> annotationType() { return ValidDateRange.class; }
        };

        assertEquals("Custom msg", mockAnn.message());
        assertEquals("startDate", mockAnn.start());
        assertEquals("endDate", mockAnn.end());
    }
}
