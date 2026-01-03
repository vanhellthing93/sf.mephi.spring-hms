package sf.mephi.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.common.dto.PageDTO;
import sf.mephi.booking.dto.request.CreateBookingRequest;
import sf.mephi.booking.dto.response.BookingDTO;
import sf.mephi.booking.service.BookingService;

@Slf4j
@RestController
@RequestMapping(ApiConstants.API_V1 + ApiConstants.BOOKINGS_PATH)
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Booking operations with SAGA pattern")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(
            summary = "Get user bookings",
            description = "Returns paginated list of current user's bookings"
    )
    public ResponseEntity<PageDTO<BookingDTO>> getUserBookings(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

        String username = authentication.getName();
        log.info("GET /api/v1/bookings - user: {}, page: {}, size: {}", username, page, size);

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PageDTO<BookingDTO> bookings = bookingService.getUserBookings(username, pageable);

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get booking by ID",
            description = "Returns booking details (only for booking owner)"
    )
    public ResponseEntity<BookingDTO> getBookingById(
            @PathVariable(value = "id") Long id,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("GET /api/v1/bookings/{} - user: {}", id, username);

        BookingDTO booking = bookingService.getBookingById(id, username);
        return ResponseEntity.ok(booking);
    }

    @PostMapping
    @Operation(
            summary = "Create new booking",
            description = "Creates a new booking using SAGA pattern (2-phase commit with compensation)"
    )
    public ResponseEntity<BookingDTO> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("POST /api/v1/bookings - user: {}, roomId: {}", username, request.getRoomId());

        BookingDTO booking = bookingService.createBooking(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel booking",
            description = "Cancels booking and releases room slot (compensation action)"
    )
    public ResponseEntity<BookingDTO> cancelBooking(
            @PathVariable(value = "id") Long id,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("DELETE /api/v1/bookings/{} - user: {}", id, username);

        BookingDTO cancelled = bookingService.cancelBooking(id, username);
        return ResponseEntity.ok(cancelled);
    }
}
