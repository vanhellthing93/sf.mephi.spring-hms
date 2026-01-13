package sf.mephi.hotel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sf.mephi.common.constants.RoomType;
import sf.mephi.hotel.config.BaseControllerTest;
import sf.mephi.hotel.dto.request.ConfirmAvailabilityRequest;
import sf.mephi.hotel.dto.request.CreateRoomRequest;
import sf.mephi.hotel.dto.response.AvailabilityConfirmationDTO;
import sf.mephi.hotel.dto.response.RoomDTO;
import sf.mephi.hotel.service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoomControllerTest extends BaseControllerTest {

    @MockitoBean
    private RoomService roomService;

    @Test
    @WithMockUser(roles = "USER")
    void getAvailableRooms_ShouldReturnRooms() throws Exception {
        RoomDTO room = RoomDTO.builder()
                .id(1L)
                .roomNumber("101")
                .available(true)
                .price(new BigDecimal("5000.00"))
                .build();
        when(roomService.getAvailableRooms()).thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].roomNumber").value("101"));

        verify(roomService).getAvailableRooms();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRecommendedRooms_ShouldReturnSortedRooms() throws Exception {
        RoomDTO room1 = RoomDTO.builder().id(1L).roomNumber("101").timesBooked(2).build();
        RoomDTO room2 = RoomDTO.builder().id(2L).roomNumber("102").timesBooked(5).build();
        when(roomService.getRecommendedRooms()).thenReturn(Arrays.asList(room1, room2));

        mockMvc.perform(get("/api/v1/rooms/recommend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].timesBooked").value(2))
                .andExpect(jsonPath("$[1].timesBooked").value(5));

        verify(roomService).getRecommendedRooms();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRoomById_ShouldReturnRoom() throws Exception {
        RoomDTO room = RoomDTO.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .price(new BigDecimal("5000.00"))
                .build();
        when(roomService.getRoomById(1L)).thenReturn(room);

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("101"));

        verify(roomService).getRoomById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_ShouldReturnCreated_WhenAdmin() throws Exception {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(1L)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .build();

        RoomDTO room = RoomDTO.builder()
                .id(1L)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .build();
        when(roomService.createRoom(any(CreateRoomRequest.class))).thenReturn(room);

        mockMvc.perform(post("/api/v1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("201"));

        verify(roomService).createRoom(any(CreateRoomRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRoom_ShouldReturn403_WhenNotAdmin() throws Exception {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(1L)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .build();

        mockMvc.perform(post("/api/v1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(roomService, never()).createRoom(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoom_ShouldReturnUpdated() throws Exception {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(1L)
                .roomNumber("101-Updated")
                .roomType(RoomType.DELUXE)
                .price(new BigDecimal("7000.00"))
                .build();

        RoomDTO room = RoomDTO.builder().id(1L).roomNumber("101-Updated").build();
        when(roomService.updateRoom(eq(1L), any(CreateRoomRequest.class))).thenReturn(room);

        mockMvc.perform(put("/api/v1/rooms/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101-Updated"));

        verify(roomService).updateRoom(eq(1L), any(CreateRoomRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoom_ShouldReturnNoContent() throws Exception {
        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/api/v1/rooms/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(1L);
    }

    @Test
    void confirmAvailability_ShouldReturnConfirmation() throws Exception {
        ConfirmAvailabilityRequest request = ConfirmAvailabilityRequest.builder()
                .requestId("req-123")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        AvailabilityConfirmationDTO confirmation = AvailabilityConfirmationDTO.builder()
                .requestId("req-123")
                .roomId(1L)
                .confirmed(true)
                .message("Room availability confirmed")
                .build();
        when(roomService.confirmAvailability(eq(1L), any())).thenReturn(confirmation);

        mockMvc.perform(post("/api/v1/rooms/1/confirm-availability")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true))
                .andExpect(jsonPath("$.requestId").value("req-123"));

        verify(roomService).confirmAvailability(eq(1L), any());
    }

    @Test
    void releaseSlot_ShouldReturn200() throws Exception {
        doNothing().when(roomService).releaseSlot(1L, "req-123");

        mockMvc.perform(post("/api/v1/rooms/1/release")
                        .with(csrf())
                        .param("requestId", "req-123"))
                .andExpect(status().isOk());

        verify(roomService).releaseSlot(1L, "req-123");
    }

    @Test
    void getAvailableRooms_ShouldReturn401_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isUnauthorized());

        verify(roomService, never()).getAvailableRooms();
    }
}
