package sf.mephi.booking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import sf.mephi.common.constants.BookingStatus;
import sf.mephi.common.dto.PageDTO;
import sf.mephi.booking.config.BaseControllerTest;
import sf.mephi.booking.dto.request.CreateBookingRequest;
import sf.mephi.booking.dto.response.BookingDTO;
import sf.mephi.booking.service.BookingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest extends BaseControllerTest {

    @MockitoBean
    private BookingService bookingService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyBookings_ShouldReturnPagedBookings() throws Exception {
        BookingDTO booking = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .roomId(1L)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        PageDTO<BookingDTO> page = PageDTO.<BookingDTO>builder()
                .content(List.of(booking))
                .totalElements(1L)
                .totalPages(1)
                .pageSize(10)
                .pageNumber(0)
                .build();

        when(bookingService.getUserBookings(eq("testuser"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/bookings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(bookingService).getUserBookings(eq("testuser"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getBookingById_ShouldReturnBooking() throws Exception {
        BookingDTO booking = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .roomId(1L)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getBookingById(1L, "testuser")).thenReturn(booking);

        mockMvc.perform(get("/api/v1/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).getBookingById(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBooking_ShouldReturnCreated() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(1L)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        BookingDTO booking = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .roomId(1L)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.createBooking(any(CreateBookingRequest.class), eq("testuser")))
                .thenReturn(booking);

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).createBooking(any(CreateBookingRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void cancelBooking_ShouldReturnCancelled() throws Exception {
        BookingDTO booking = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .roomId(1L)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingService.cancelBooking(1L, "testuser")).thenReturn(booking);

        mockMvc.perform(delete("/api/v1/bookings/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelBooking(1L, "testuser");
    }

    @Test
    void getMyBookings_ShouldReturn401_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/my"))
                .andExpect(status().isUnauthorized());

        verify(bookingService, never()).getUserBookings(anyString(), any(Pageable.class));
    }

    @Test
    void createBooking_ShouldReturn401_WhenUnauthorized() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(1L)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(bookingService, never()).createBooking(any(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBooking_ShouldReturn400_WhenValidationFails() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(null)  // Это теперь ВАЛИДНО - автовыбор комнаты
                .startDate(null)  // ← Невалидно: дата обязательна
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBooking_ShouldReturn400_WhenDateRangeInvalid() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(1L)
                .startDate(LocalDate.of(2026, 3, 5))
                .endDate(LocalDate.of(2026, 3, 1))  // End before start
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), anyString());
    }
}
