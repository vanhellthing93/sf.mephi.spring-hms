package sf.mephi.hotel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sf.mephi.common.constants.RoomType;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.hotel.dto.request.ConfirmAvailabilityRequest;
import sf.mephi.hotel.dto.request.CreateRoomRequest;
import sf.mephi.hotel.dto.response.AvailabilityConfirmationDTO;
import sf.mephi.hotel.dto.response.RoomDTO;
import sf.mephi.hotel.entity.Hotel;
import sf.mephi.hotel.entity.Room;
import sf.mephi.hotel.mapper.RoomMapper;
import sf.mephi.hotel.repository.HotelRepository;
import sf.mephi.hotel.repository.RoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAvailableRooms_ShouldReturnAllAvailableRooms() {
        Room room1 = Room.builder().id(1L).roomNumber("101").available(true).build();
        Room room2 = Room.builder().id(2L).roomNumber("102").available(true).build();
        RoomDTO dto1 = RoomDTO.builder().id(1L).roomNumber("101").available(true).build();
        RoomDTO dto2 = RoomDTO.builder().id(2L).roomNumber("102").available(true).build();

        when(roomRepository.findAllAvailable()).thenReturn(Arrays.asList(room1, room2));
        when(roomMapper.toDTO(room1)).thenReturn(dto1);
        when(roomMapper.toDTO(room2)).thenReturn(dto2);

        List<RoomDTO> result = roomService.getAvailableRooms();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getAvailable());
        assertTrue(result.get(1).getAvailable());
        verify(roomRepository).findAllAvailable();
    }

    @Test
    void getRecommendedRooms_ShouldReturnRoomsSortedByTimesBooked() {
        Room room1 = Room.builder().id(1L).roomNumber("101").timesBooked(2).build();
        Room room2 = Room.builder().id(2L).roomNumber("102").timesBooked(5).build();
        RoomDTO dto1 = RoomDTO.builder().id(1L).timesBooked(2).build();
        RoomDTO dto2 = RoomDTO.builder().id(2L).timesBooked(5).build();

        Pageable pageable = PageRequest.of(0, 100);
        when(roomRepository.findRecommendedRooms(pageable)).thenReturn(Arrays.asList(room1, room2));
        when(roomMapper.toDTO(room1)).thenReturn(dto1);
        when(roomMapper.toDTO(room2)).thenReturn(dto2);

        List<RoomDTO> result = roomService.getRecommendedRooms();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getTimesBooked() <= result.get(1).getTimesBooked());
        verify(roomRepository).findRecommendedRooms(any(Pageable.class));
    }

    @Test
    void getRoomById_ShouldReturnRoom_WhenExists() {
        Long roomId = 1L;
        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .price(new BigDecimal("5000.00"))
                .available(true)
                .timesBooked(3)
                .build();
        RoomDTO expectedDto = RoomDTO.builder()
                .id(roomId)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .price(new BigDecimal("5000.00"))
                .available(true)
                .timesBooked(3)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomMapper.toDTO(room)).thenReturn(expectedDto);

        RoomDTO result = roomService.getRoomById(roomId);

        assertNotNull(result);
        assertEquals(roomId, result.getId());
        assertEquals("101", result.getRoomNumber());
        verify(roomRepository).findById(roomId);
    }

    @Test
    void getRoomById_ShouldThrowNotFoundException_WhenNotExists() {
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> roomService.getRoomById(roomId)
        );

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).findById(roomId);
    }

    @Test
    void createRoom_ShouldSaveAndReturnRoom_WhenHotelExists() {
        Long hotelId = 1L;
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(hotelId)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .build();

        Hotel hotel = Hotel.builder().id(hotelId).name("Grand Hotel").build();
        Room room = Room.builder()
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .build();
        Room savedRoom = Room.builder()
                .id(1L)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .hotel(hotel)
                .available(true)
                .timesBooked(0)
                .build();
        RoomDTO expectedDto = RoomDTO.builder()
                .id(1L)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .available(true)
                .timesBooked(0)
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomMapper.toEntity(request)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(savedRoom);
        when(roomMapper.toDTO(savedRoom)).thenReturn(expectedDto);

        RoomDTO result = roomService.createRoom(request);

        assertNotNull(result);
        assertEquals("201", result.getRoomNumber());
        assertEquals(RoomType.SUITE, result.getRoomType());
        verify(hotelRepository).findById(hotelId);
        verify(roomMapper).toEntity(request);
        verify(roomRepository).save(room);
    }

    @Test
    void createRoom_ShouldThrowNotFoundException_WhenHotelNotExists() {
        Long hotelId = 999L;
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(hotelId)
                .roomNumber("201")
                .roomType(RoomType.SUITE)
                .price(new BigDecimal("10000.00"))
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> roomService.createRoom(request)
        );

        assertTrue(exception.getMessage().contains("Hotel not found"));
        verify(hotelRepository).findById(hotelId);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void updateRoom_ShouldUpdateAndReturnRoom_WhenExists() {
        Long roomId = 1L;
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(1L)
                .roomNumber("101-Updated")
                .roomType(RoomType.DELUXE)
                .price(new BigDecimal("7000.00"))
                .build();

        Room existingRoom = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .price(new BigDecimal("5000.00"))
                .build();

        Room updatedRoom = Room.builder()
                .id(roomId)
                .roomNumber("101-Updated")
                .roomType(RoomType.DELUXE)
                .price(new BigDecimal("7000.00"))
                .build();

        RoomDTO expectedDto = RoomDTO.builder()
                .id(roomId)
                .roomNumber("101-Updated")
                .roomType(RoomType.DELUXE)
                .price(new BigDecimal("7000.00"))
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(existingRoom)).thenReturn(updatedRoom);
        when(roomMapper.toDTO(updatedRoom)).thenReturn(expectedDto);

        RoomDTO result = roomService.updateRoom(roomId, request);

        assertNotNull(result);
        assertEquals("101-Updated", result.getRoomNumber());
        assertEquals(RoomType.DELUXE, result.getRoomType());
        verify(roomRepository).findById(roomId);
        verify(roomRepository).save(existingRoom);
    }

    @Test
    void deleteRoom_ShouldDeleteRoom_WhenExists() {
        Long roomId = 1L;
        when(roomRepository.existsById(roomId)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(roomId);

        roomService.deleteRoom(roomId);

        verify(roomRepository).existsById(roomId);
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    void deleteRoom_ShouldThrowNotFoundException_WhenNotExists() {
        Long roomId = 999L;
        when(roomRepository.existsById(roomId)).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> roomService.deleteRoom(roomId)
        );

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).existsById(roomId);
        verify(roomRepository, never()).deleteById(any());
    }

    @Test
    void confirmAvailability_ShouldConfirmAndIncrementTimesBooked_WhenRoomAvailable() {
        Long roomId = 1L;
        String requestId = "test-request-123";
        ConfirmAvailabilityRequest request = ConfirmAvailabilityRequest.builder()
                .requestId(requestId)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .available(true)
                .timesBooked(5)
                .build();

        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        AvailabilityConfirmationDTO result = roomService.confirmAvailability(roomId, request);

        assertNotNull(result);
        assertEquals(requestId, result.getRequestId());
        assertEquals(roomId, result.getRoomId());
        assertTrue(result.getConfirmed());
        assertEquals("Room availability confirmed", result.getMessage());
        verify(roomRepository).findByIdWithLock(roomId);
        verify(roomRepository).save(room);
        assertEquals(6, room.getTimesBooked());
    }

    @Test
    void confirmAvailability_ShouldReturnCached_WhenRequestIdAlreadyProcessed() {
        Long roomId = 1L;
        String requestId = "duplicate-request";
        ConfirmAvailabilityRequest request = ConfirmAvailabilityRequest.builder()
                .requestId(requestId)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        Room room = Room.builder().id(roomId).available(true).timesBooked(5).build();
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        roomService.confirmAvailability(roomId, request);

        AvailabilityConfirmationDTO result = roomService.confirmAvailability(roomId, request);

        assertNotNull(result);
        assertTrue(result.getConfirmed());
        verify(roomRepository, times(1)).findByIdWithLock(roomId);
        verify(roomRepository, times(1)).save(any());
    }

    @Test
    void confirmAvailability_ShouldReturnFalse_WhenRoomNotAvailable() {
        Long roomId = 1L;
        String requestId = "test-request-456";
        ConfirmAvailabilityRequest request = ConfirmAvailabilityRequest.builder()
                .requestId(requestId)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .available(false)
                .timesBooked(5)
                .build();

        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));

        AvailabilityConfirmationDTO result = roomService.confirmAvailability(roomId, request);

        assertNotNull(result);
        assertEquals(requestId, result.getRequestId());
        assertEquals(roomId, result.getRoomId());
        assertFalse(result.getConfirmed());
        assertEquals("Room is not available", result.getMessage());
        verify(roomRepository).findByIdWithLock(roomId);
        verify(roomRepository, never()).save(any());
        assertEquals(5, room.getTimesBooked());
    }

    @Test
    void confirmAvailability_ShouldThrowNotFoundException_WhenRoomNotExists() {
        Long roomId = 999L;
        String requestId = "test-request-789";
        ConfirmAvailabilityRequest request = ConfirmAvailabilityRequest.builder()
                .requestId(requestId)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> roomService.confirmAvailability(roomId, request)
        );

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).findByIdWithLock(roomId);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void releaseSlot_ShouldDecrementTimesBooked_WhenRoomExists() {
        Long roomId = 1L;
        String requestId = "release-request-123";
        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .available(true)
                .timesBooked(6)
                .currentRequestId(requestId)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        roomService.releaseSlot(roomId, requestId);

        verify(roomRepository).findById(roomId);
        verify(roomRepository).save(room);
        assertEquals(5, room.getTimesBooked());
        assertNull(room.getCurrentRequestId());
    }

    @Test
    void releaseSlot_ShouldThrowNotFoundException_WhenRoomNotExists() {
        Long roomId = 999L;
        String requestId = "release-request-456";
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> roomService.releaseSlot(roomId, requestId)
        );

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void releaseSlot_ShouldNotDecrementBelowZero() {
        Long roomId = 1L;
        String requestId = "release-request-789";
        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .available(true)
                .timesBooked(0)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        roomService.releaseSlot(roomId, requestId);

        verify(roomRepository).findById(roomId);
        verify(roomRepository).save(room);
        assertEquals(0, room.getTimesBooked());
    }
}
