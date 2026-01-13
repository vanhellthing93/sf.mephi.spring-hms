package sf.mephi.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DateRangeValidatorTest {
    private DateRangeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DateRangeValidator();
        ValidDateRange mockAnnotation = new ValidDateRange() {
            @Override public String start() { return "startDate"; }
            @Override public String end() { return "endDate"; }
            @Override public String message() { return "Invalid"; }
            @Override public Class<?>[] groups() { return new Class[0]; }
            @Override public Class[] payload() { return new Class[0]; }
            @Override public Class<? extends Annotation> annotationType() { return ValidDateRange.class; }
        };
        validator.initialize(mockAnnotation);
    }

    @ParameterizedTest
    @MethodSource("validRanges")
    void isValid_ShouldReturnTrue(LocalDate start, LocalDate end) {
        TestDto dto = new TestDto(start, end);
        assertTrue(validator.isValid(dto, null));
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    void isValid_ShouldReturnFalse(LocalDate start, LocalDate end) {
        TestDto dto = new TestDto(start, end);
        assertFalse(validator.isValid(dto, null));
    }

    static Arguments[] validRanges() {
        return new Arguments[]{
                Arguments.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15))  // end > start
        };
    }

    static Arguments[] invalidRanges() {
        return new Arguments[]{
                Arguments.of(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 10)),  // end < start
                Arguments.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 10))   // end == start — strict after!
        };
    }
}

class TestDto {
    public LocalDate startDate;
    public LocalDate endDate;
    TestDto(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
    }
}
