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
import sf.mephi.hotel.dto.request.ConfirmAvailabilityRequest;
import sf.mephi.hotel.dto.request.CreateRoomRequest;
import sf.mephi.hotel.dto.response.AvailabilityConfirmationDTO;
import sf.mephi.hotel.dto.response.RoomDTO;
import sf.mephi.hotel.service.RoomService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.API_V1 + ApiConstants.ROOMS_PATH)
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "Room CRUD and availability operations")
public class RoomController {

    private final RoomService roomService;

    /**
     * GET /api/v1/rooms - получить все доступные номера (USER)
     */
    @GetMapping
    @Operation(summary = "Get available rooms", description = "Returns list of all available rooms")
    public ResponseEntity<List<RoomDTO>> getAvailableRooms() {
        log.info("GET /api/v1/rooms - fetching available rooms");
        List<RoomDTO> rooms = roomService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * GET /api/v1/rooms/recommend - получить рекомендованные номера (USER)
     */
    @GetMapping("/recommend")
    @Operation(
            summary = "Get recommended rooms",
            description = "Returns available rooms sorted by booking count (load balancing algorithm)"
    )
    public ResponseEntity<List<RoomDTO>> getRecommendedRooms() {
        log.info("GET /api/v1/rooms/recommend - fetching recommended rooms");
        List<RoomDTO> rooms = roomService.getRecommendedRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * GET /api/v1/rooms/{id} - получить номер по ID (USER)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID", description = "Returns room details by ID")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        log.info("GET /api/v1/rooms/{}", id);
        RoomDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    /**
     * POST /api/v1/rooms - создать новый номер (ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new room",
            description = "Creates a new room in a hotel (ADMIN only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<RoomDTO> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {

        log.info("POST /api/v1/rooms - creating room: {} in hotel: {}",
                request.getRoomNumber(), request.getHotelId());
        RoomDTO created = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/rooms/{id} - обновить номер (ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update room",
            description = "Updates existing room (ADMIN only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<RoomDTO> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomRequest request) {

        log.info("PUT /api/v1/rooms/{} - updating room", id);
        RoomDTO updated = roomService.updateRoom(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/v1/rooms/{id} - удалить номер (ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete room",
            description = "Deletes room by ID (ADMIN only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        log.info("DELETE /api/v1/rooms/{}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/rooms/{id}/confirm-availability - подтвердить доступность (INTERNAL)
     */
    @PostMapping("/{id}/confirm-availability")
    @Operation(
            summary = "Confirm room availability (INTERNAL)",
            description = "Confirms room availability and creates temporary booking slot"
    )
    public ResponseEntity<AvailabilityConfirmationDTO> confirmAvailability(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmAvailabilityRequest request) {

        log.info("POST /api/v1/rooms/{}/confirm-availability - requestId: {}", id, request.getRequestId());
        AvailabilityConfirmationDTO confirmation = roomService.confirmAvailability(id, request);
        return ResponseEntity.ok(confirmation);
    }

    /**
     * POST /api/v1/rooms/{id}/release - освободить слот (INTERNAL)
     */
    @PostMapping("/{id}/release")
    @Operation(
            summary = "Release room slot (INTERNAL)",
            description = "Releases temporary booking slot (compensation action)"
    )
    public ResponseEntity<Void> releaseSlot(
            @PathVariable Long id,
            @RequestParam String requestId) {

        log.info("POST /api/v1/rooms/{}/release - requestId: {}", id, requestId);
        roomService.releaseSlot(id, requestId);
        return ResponseEntity.ok().build();
    }
}
