package sf.mephi.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mephi.common.constants.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
