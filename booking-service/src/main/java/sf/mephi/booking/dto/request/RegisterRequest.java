package sf.mephi.booking.dto.request;

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
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = ApiConstants.MIN_USERNAME_LENGTH,
            max = ApiConstants.MAX_USERNAME_LENGTH,
            message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = ApiConstants.MIN_PASSWORD_LENGTH,
            message = "Password must be at least 8 characters")
    private String password;
}
