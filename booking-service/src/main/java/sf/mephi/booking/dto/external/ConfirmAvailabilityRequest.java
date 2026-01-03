package sf.mephi.booking.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAvailabilityRequest {
    private String requestId;
    private LocalDate startDate;
    private LocalDate endDate;
}
