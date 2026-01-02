package sf.mephi.hotel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mephi.common.constants.ApiConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(max = ApiConstants.MAX_HOTEL_NAME_LENGTH, message = "Hotel name is too long")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = ApiConstants.MAX_ADDRESS_LENGTH, message = "Address is too long")
    private String address;

    @NotBlank(message = "City is required")
    private String city;
}
