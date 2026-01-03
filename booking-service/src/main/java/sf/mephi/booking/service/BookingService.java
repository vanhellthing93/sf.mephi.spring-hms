package sf.mephi.booking.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.common.constants.BookingStatus;
import sf.mephi.common.dto.PageDTO;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.common.exception.ValidationException;
import sf.mephi.common.util.CorrelationIdUtil;
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

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final HotelServiceClient hotelServiceClient;

    /**
     * Получить все бронирования пользователя с пагинацией
     */
    @Transactional(readOnly = true)
    public PageDTO<BookingDTO> getUserBookings(String username, Pageable pageable) {
        log.info("Fetching bookings for user: {}", username);

        User user = userService.getUserByUsername(username);
        Page<Booking> page = bookingRepository.findByUserId(user.getId(), pageable);

        return PageDTO.fromPage(page, bookingMapper::toDTO);
    }

    /**
     * Получить бронирование по ID
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id, String username) {
        log.info("Fetching booking with id: {} for user: {}", id, username);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_BOOKING_NOT_FOUND, id)
                ));

        // Проверка прав доступа
        if (!booking.getUser().getUsername().equals(username)) {
            throw new ValidationException(ApiConstants.ERROR_FORBIDDEN);
        }

        return bookingMapper.toDTO(booking);
    }

    /**
     * Создать новое бронирование (SAGA Pattern)
     */
    @Transactional
    @CircuitBreaker(name = ApiConstants.HOTEL_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "createBookingFallback")
    @Retry(name = ApiConstants.HOTEL_SERVICE_RETRY)
    public BookingDTO createBooking(CreateBookingRequest request, String username) {
        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestId = UUID.randomUUID().toString();

        log.info("Creating booking for user: {}, roomId: {}, requestId: {}, correlationId: {}",
                username, request.getRoomId(), requestId, correlationId);

        // Валидация дат
        validateBookingDates(request);

        // Проверка идемпотентности
        Optional<Booking> existingBooking = bookingRepository.findByRequestId(requestId);
        if (existingBooking.isPresent()) {
            log.info("Booking already exists for requestId: {}", requestId);
            return bookingMapper.toDTO(existingBooking.get());
        }

        User user = userService.getUserByUsername(username);

        RoomDTO room = hotelServiceClient.getRoomById(request.getRoomId());

        if (!room.getAvailable()) {
            throw new ValidationException(ApiConstants.ERROR_ROOM_UNAVAILABLE);
        }

        ConfirmAvailabilityRequest confirmRequest = ConfirmAvailabilityRequest.builder()
                .requestId(requestId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        AvailabilityConfirmationDTO confirmation =
                hotelServiceClient.confirmAvailability(request.getRoomId(), confirmRequest);

        if (!confirmation.getConfirmed()) {
            log.warn("Room availability not confirmed: {}", confirmation.getMessage());
            throw new ValidationException(ApiConstants.ERROR_ROOM_UNAVAILABLE);
        }

        try {
            Booking booking = bookingMapper.toEntity(request);
            booking.setUser(user);
            booking.setRequestId(requestId);
            booking.setStatus(BookingStatus.CONFIRMED);

            Booking saved = bookingRepository.save(booking);
            log.info("Booking created with id: {}, requestId: {}", saved.getId(), requestId);

            return bookingMapper.toDTO(saved);

        } catch (Exception e) {
            // Компенсирующая транзакция
            log.error("Failed to save booking, releasing slot. RequestId: {}", requestId, e);
            compensateBooking(request.getRoomId(), requestId);
            throw e;
        }
    }

    /**
     * Отменить бронирование (компенсация)
     */
    @Transactional
    public BookingDTO cancelBooking(Long id, String username) {
        log.info("Cancelling booking with id: {} by user: {}", id, username);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_BOOKING_NOT_FOUND, id)
                ));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new ValidationException(ApiConstants.ERROR_FORBIDDEN);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Booking is already cancelled");
        }

        compensateBooking(booking.getRoomId(), booking.getRequestId());

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        log.info("Booking cancelled: {}", id);
        return bookingMapper.toDTO(updated);
    }

    /**
     * Компенсирующая транзакция: освобождение слота
     */
    private void compensateBooking(Long roomId, String requestId) {
        try {
            log.info("Compensating booking - releasing slot for roomId: {}, requestId: {}",
                    roomId, requestId);
            hotelServiceClient.releaseSlot(roomId, requestId);
            log.info("Slot released successfully");
        } catch (Exception e) {
            log.error("Failed to release slot during compensation: roomId={}, requestId={}",
                    roomId, requestId, e);
        }
    }

    /**
     * Валидация дат бронирования
     */
    private void validateBookingDates(CreateBookingRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate()) ||
                request.getEndDate().isEqual(request.getStartDate())) {
            throw new ValidationException(ApiConstants.ERROR_INVALID_DATE_RANGE);
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days > ApiConstants.MAX_BOOKING_DAYS) {
            throw new ValidationException(
                    String.format(ApiConstants.ERROR_BOOKING_TOO_LONG, ApiConstants.MAX_BOOKING_DAYS)
            );
        }
    }

    /**
     * Fallback метод для Circuit Breaker
     */
    private BookingDTO createBookingFallback(CreateBookingRequest request, String username, Exception e) {
        log.error("Circuit breaker fallback triggered for createBooking: {}", e.getMessage());
        throw new ValidationException("Hotel service is temporarily unavailable. Please try again later.");
    }
}
