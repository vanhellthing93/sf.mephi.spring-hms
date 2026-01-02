package sf.mephi.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.hotel.dto.request.CreateHotelRequest;
import sf.mephi.hotel.dto.response.HotelDTO;
import sf.mephi.hotel.service.HotelService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.API_V1 + ApiConstants.HOTELS_PATH)
@RequiredArgsConstructor
@Tag(name = "Hotel Management", description = "Hotel CRUD operations")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    @Operation(summary = "Get all hotels", description = "Returns list of all hotels")
    public ResponseEntity<List<HotelDTO>> getAllHotels(
            @RequestParam(value = "city", required = false) String city) {  // ✅ Явное имя

        log.info("GET /api/v1/hotels - city filter: {}", city);

        List<HotelDTO> hotels = city != null
                ? hotelService.getHotelsByCity(city)
                : hotelService.getAllHotels();

        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel by ID", description = "Returns hotel details by ID")
    public ResponseEntity<HotelDTO> getHotelById(
            @PathVariable(value = "id") Long id) {  // ✅ Явное имя

        log.info("GET /api/v1/hotels/{}", id);
        HotelDTO hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new hotel",
            description = "Creates a new hotel (ADMIN only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<HotelDTO> createHotel(
            @Valid @RequestBody CreateHotelRequest request) {

        log.info("POST /api/v1/hotels - creating hotel: {}", request.getName());
        HotelDTO created = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update hotel",
            description = "Updates existing hotel (ADMIN only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<HotelDTO> updateHotel(
            @PathVariable(value = "id") Long id,  // ✅ Явное имя
            @Valid @RequestBody CreateHotelRequest request) {

        log.info("PUT /api/v1/hotels/{} - updating hotel", id);
        HotelDTO updated = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete hotel",
            description = "Deletes hotel by ID (ADMIN only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Void> deleteHotel(
            @PathVariable(value = "id") Long id) {  // ✅ Явное имя

        log.info("DELETE /api/v1/hotels/{}", id);
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
