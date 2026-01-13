package sf.mephi.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sf.mephi.common.constants.BookingStatus;
import sf.mephi.common.dto.PageDTO;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.common.exception.ValidationException;
import sf.mephi.booking.client.HotelServiceClient;
import sf.mephi.booking.dto.external.AvailabilityConfirmationDTO;
import sf.mephi.booking.dto.external.ConfirmAvailabilityRequest;
import sf.mephi.booking.dto.external.RoomDTO;
import sf.mephi.booking.dto.request.CreateBookingRequest;
import sf.mephi.booking.dto.response.BookingDTO;
import sf.mephi.booking.entity.Booking;
import sf.mephi.booking.entity.User;
import sf.mephi.booking.mapper.BookingMapper;
import sf.mephi.booking.repository.BookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserService userService;

    @Mock
    private HotelServiceClient hotelServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private CreateBookingRequest createRequest;
    private Booking booking;
    private BookingDTO bookingDTO;
    private RoomDTO roomDTO;
    private AvailabilityConfirmationDTO confirmationDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        createRequest = CreateBookingRequest.builder()
                .roomId(1L)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        booking = Booking.builder()
                .id(1L)
                .user(user)
                .roomId(1L)
                .startDate(createRequest.getStartDate())
                .endDate(createRequest.getEndDate())
                .status(BookingStatus.PENDING)
                .requestId("req-123")
                .build();

        bookingDTO = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .roomId(1L)
                .status(BookingStatus.CONFIRMED)
                .build();

        roomDTO = RoomDTO.builder()
                .id(1L)
                .roomNumber("101")
                .available(true)
                .build();

        confirmationDTO = AvailabilityConfirmationDTO.builder()
                .requestId("req-123")
                .roomId(1L)
                .confirmed(true)
                .message("Room availability confirmed")
                .build();
    }

    @Test
    void getUserBookings_ShouldReturnPagedBookings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> page = new PageImpl<>(List.of(booking));

        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(bookingRepository.findByUserId(1L, pageable)).thenReturn(page);
        when(bookingMapper.toDTO(booking)).thenReturn(bookingDTO);

        PageDTO<BookingDTO> result = bookingService.getUserBookings("testuser", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());

        verify(userService).getUserByUsername("testuser");
        verify(bookingRepository).findByUserId(1L, pageable);
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenUserOwnsIt() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDTO(booking)).thenReturn(bookingDTO);

        BookingDTO result = bookingService.getBookingById(1L, "testuser");

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(bookingRepository).findById(1L);
        verify(bookingMapper).toDTO(booking);
    }

    @Test
    void getBookingById_ShouldThrowException_WhenNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(999L, "testuser"));

        verify(bookingRepository).findById(999L);
    }

    @Test
    void getBookingById_ShouldThrowException_WhenUserDoesNotOwnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.getBookingById(1L, "otheruser"));

        verify(bookingRepository).findById(1L);
    }

    @Test
    void createBooking_ShouldCompleteSuccessfully_WhenRoomAvailable() {
        // SAGA успешный сценарий
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(bookingRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(bookingMapper.toEntity(createRequest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomDTO);
        when(hotelServiceClient.confirmAvailability(eq(1L), any(ConfirmAvailabilityRequest.class)))
                .thenReturn(confirmationDTO);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(bookingDTO);

        BookingDTO result = bookingService.createBooking(createRequest, "testuser");

        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());

        verify(bookingRepository, times(2)).save(any(Booking.class)); // PENDING → CONFIRMED
        verify(hotelServiceClient).getRoomById(1L);
        verify(hotelServiceClient).confirmAvailability(eq(1L), any(ConfirmAvailabilityRequest.class));
    }

    @Test
    void createBooking_ShouldReturnExisting_WhenIdempotentRequest() {
        when(bookingRepository.findByRequestId(anyString())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDTO(booking)).thenReturn(bookingDTO);

        BookingDTO result = bookingService.createBooking(createRequest, "testuser");

        assertNotNull(result);
        verify(bookingRepository).findByRequestId(anyString());
        verify(hotelServiceClient, never()).getRoomById(anyLong());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ShouldExecuteCompensation_WhenRoomUnavailable() {
        // SAGA компенсация
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(bookingRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(bookingMapper.toEntity(createRequest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        roomDTO.setAvailable(false);  // Комната недоступна
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomDTO);

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(createRequest, "testuser"));

        // Проверяем компенсацию: бронирование сохранено в CANCELLED
        verify(bookingRepository, times(2)).save(any(Booking.class)); // PENDING → CANCELLED
        verify(hotelServiceClient).releaseSlot(eq(1L), anyString());
    }

    @Test
    void createBooking_ShouldExecuteCompensation_WhenConfirmationFails() {
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(bookingRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(bookingMapper.toEntity(createRequest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomDTO);

        confirmationDTO.setConfirmed(false);  // Подтверждение не удалось
        when(hotelServiceClient.confirmAvailability(eq(1L), any(ConfirmAvailabilityRequest.class)))
                .thenReturn(confirmationDTO);

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(createRequest, "testuser"));

        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(hotelServiceClient).releaseSlot(eq(1L), anyString());
    }

    @Test
    void createBooking_ShouldThrowException_WhenDatesInvalid() {
        createRequest.setEndDate(LocalDate.of(2026, 2, 28));  // End before start

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(createRequest, "testuser"));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ShouldThrowException_WhenBookingTooLong() {
        createRequest.setEndDate(LocalDate.of(2026, 4, 15));  // 45 дней (> 30)

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(createRequest, "testuser"));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_ShouldCancelAndReleaseSlot() {
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(bookingDTO);
        doNothing().when(hotelServiceClient).releaseSlot(1L, "req-123");

        BookingDTO result = bookingService.cancelBooking(1L, "testuser");

        assertNotNull(result);
        verify(hotelServiceClient).releaseSlot(1L, "req-123");
        verify(bookingRepository).save(argThat(b -> b.getStatus() == BookingStatus.CANCELLED));
    }

    @Test
    void cancelBooking_ShouldThrowException_WhenAlreadyCancelled() {
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.cancelBooking(1L, "testuser"));

        verify(hotelServiceClient, never()).releaseSlot(anyLong(), anyString());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_ShouldThrowException_WhenUserDoesNotOwnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.cancelBooking(1L, "otheruser"));

        verify(hotelServiceClient, never()).releaseSlot(anyLong(), anyString());
    }
}
