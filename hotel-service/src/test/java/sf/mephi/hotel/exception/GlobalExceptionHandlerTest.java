package sf.mephi.hotel.exception;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.common.exception.ValidationException;
import sf.mephi.hotel.config.BaseControllerTest;
import sf.mephi.hotel.service.HotelService;
import sf.mephi.hotel.service.RoomService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest extends BaseControllerTest {

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private RoomService roomService;

    @Test
    @WithMockUser(roles = "USER")
    void handleNotFoundException_ShouldReturn404() throws Exception {
        when(hotelService.getHotelById(999L))
                .thenThrow(new NotFoundException("Hotel not found: 999"));

        mockMvc.perform(get("/api/v1/hotels/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Hotel not found: 999"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void handleValidationException_ShouldReturn400() throws Exception {
        when(roomService.getRoomById(1L))
                .thenThrow(new ValidationException("Invalid room data"));

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid room data"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void handleRoomUnavailableException_ShouldReturn409() throws Exception {
        when(roomService.getRoomById(1L))
                .thenThrow(new RoomUnavailableException("Room is already booked"));

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Room is already booked"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void handleUnexpectedException_ShouldReturn500() throws Exception {
        when(hotelService.getHotelById(1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/hotels/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
