package sf.mephi.common.validation;

import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class ValidDateRangeTest {
    @Test
    void annotation_ShouldHaveExpectedDefaults() throws NoSuchMethodException {
        assertEquals("End date must be after start date", ValidDateRange.class.getDeclaredMethod("message").getDefaultValue());
    }

    @Test
    void mockAnnotation_ShouldWork() {
        ValidDateRange mockAnn = new ValidDateRange() {
            @Override public String startDate() { return "start"; }
            @Override public String endDate() { return "end"; }
            @Override public String message() { return "Custom msg"; }
            @Override public Class<?>[] groups() { return new Class[0]; }
            @Override public Class<? extends Payload>[] payload() { return new Class[0]; }
            @Override public Class<? extends Annotation> annotationType() { return ValidDateRange.class; }
        };
        assertEquals("Custom msg", mockAnn.message());
        assertEquals("start", mockAnn.startDate());
    }
}
