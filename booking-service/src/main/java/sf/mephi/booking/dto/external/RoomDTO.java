package sf.mephi.booking.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mephi.common.constants.RoomType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal price;
    private Boolean available;
    private Integer timesBooked;
}
