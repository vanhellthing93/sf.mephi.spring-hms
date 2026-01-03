package sf.mephi.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.booking.dto.external.RoomDTO;
import sf.mephi.booking.dto.external.AvailabilityConfirmationDTO;
import sf.mephi.booking.dto.external.ConfirmAvailabilityRequest;

import java.util.List;

@FeignClient(
        name = "hotel-service",
        path = ApiConstants.API_V1
)
public interface HotelServiceClient {

    @GetMapping(ApiConstants.ROOMS_PATH + "/{id}")
    RoomDTO getRoomById(@PathVariable(value = "id") Long id);

    @GetMapping(ApiConstants.ROOMS_PATH + "/recommend")
    List<RoomDTO> getRecommendedRooms();

    @PostMapping(ApiConstants.ROOMS_PATH + "/{id}/confirm-availability")
    AvailabilityConfirmationDTO confirmAvailability(
            @PathVariable(value = "id") Long id,
            @RequestBody ConfirmAvailabilityRequest request
    );

    @PostMapping(ApiConstants.ROOMS_PATH + "/{id}/release")
    void releaseSlot(
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "requestId") String requestId
    );
}
