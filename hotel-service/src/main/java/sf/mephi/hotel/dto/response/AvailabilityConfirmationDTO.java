package sf.mephi.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityConfirmationDTO {
    private String requestId;
    private Long roomId;
    private Boolean confirmed;
    private String message;
}
