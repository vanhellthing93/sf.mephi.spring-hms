package sf.mephi.hotel.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.hotel.dto.request.CreateHotelRequest;
import sf.mephi.hotel.dto.response.HotelDTO;
import sf.mephi.hotel.entity.Hotel;
import sf.mephi.hotel.mapper.HotelMapper;
import sf.mephi.hotel.repository.HotelRepository;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void getAllHotels_ShouldReturnAllHotels() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Grand Hotel").build();
        Hotel hotel2 = Hotel.builder().id(2L).name("City Inn").build();
        HotelDTO dto1 = HotelDTO.builder().id(1L).name("Grand Hotel").build();
        HotelDTO dto2 = HotelDTO.builder().id(2L).name("City Inn").build();

        when(hotelRepository.findAll()).thenReturn(Arrays.asList(hotel1, hotel2));
        when(hotelMapper.toDTO(hotel1)).thenReturn(dto1);
        when(hotelMapper.toDTO(hotel2)).thenReturn(dto2);

        List<HotelDTO> result = hotelService.getAllHotels();

        assertEquals(2, result.size());
        assertEquals("Grand Hotel", result.get(0).getName());
        assertEquals("City Inn", result.get(1).getName());
        verify(hotelRepository).findAll();
        verify(hotelMapper, times(2)).toDTO(any(Hotel.class));
    }

    @Test
    void getHotelById_ShouldReturnHotel_WhenExists() {
        Long hotelId = 1L;
        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Grand Hotel")
                .address("123 Main St")
                .city("Moscow")
                .build();
        HotelDTO expectedDto = HotelDTO.builder()
                .id(hotelId)
                .name("Grand Hotel")
                .address("123 Main St")
                .city("Moscow")
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toDTO(hotel)).thenReturn(expectedDto);

        HotelDTO result = hotelService.getHotelById(hotelId);

        assertNotNull(result);
        assertEquals(hotelId, result.getId());
        assertEquals("Grand Hotel", result.getName());
        verify(hotelRepository).findById(hotelId);
        verify(hotelMapper).toDTO(hotel);
    }

    @Test
    void getHotelById_ShouldThrowNotFoundException_WhenNotExists() {
        Long hotelId = 999L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> hotelService.getHotelById(hotelId)
        );

        assertTrue(exception.getMessage().contains("Hotel not found"));
        assertTrue(exception.getMessage().contains(String.valueOf(hotelId)));
        verify(hotelRepository).findById(hotelId);
        verify(hotelMapper, never()).toDTO(any());
    }

    @Test
    void createHotel_ShouldSaveAndReturnHotel() {
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("New Hotel")
                .address("456 Park Ave")
                .city("Saint Petersburg")
                .build();

        Hotel hotel = Hotel.builder()
                .name("New Hotel")
                .address("456 Park Ave")
                .city("Saint Petersburg")
                .build();

        Hotel savedHotel = Hotel.builder()
                .id(1L)
                .name("New Hotel")
                .address("456 Park Ave")
                .city("Saint Petersburg")
                .createdAt(LocalDateTime.now())
                .build();

        HotelDTO expectedDto = HotelDTO.builder()
                .id(1L)
                .name("New Hotel")
                .address("456 Park Ave")
                .city("Saint Petersburg")
                .createdAt(LocalDateTime.now())
                .totalRooms(0)
                .build();

        when(hotelMapper.toEntity(request)).thenReturn(hotel);
        when(hotelRepository.save(hotel)).thenReturn(savedHotel);
        when(hotelMapper.toDTO(savedHotel)).thenReturn(expectedDto);

        HotelDTO result = hotelService.createHotel(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Hotel", result.getName());
        verify(hotelMapper).toEntity(request);
        verify(hotelRepository).save(hotel);
        verify(hotelMapper).toDTO(savedHotel);
    }

    @Test
    void updateHotel_ShouldUpdateAndReturnHotel_WhenExists() {
        Long hotelId = 1L;
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("Updated Hotel")
                .address("789 New St")
                .city("Kazan")
                .build();

        Hotel existingHotel = Hotel.builder()
                .id(hotelId)
                .name("Old Hotel")
                .address("123 Old St")
                .city("Moscow")
                .build();

        Hotel updatedHotel = Hotel.builder()
                .id(hotelId)
                .name("Updated Hotel")
                .address("789 New St")
                .city("Kazan")
                .build();

        HotelDTO expectedDto = HotelDTO.builder()
                .id(hotelId)
                .name("Updated Hotel")
                .address("789 New St")
                .city("Kazan")
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(existingHotel));
        doNothing().when(hotelMapper).updateEntity(request, existingHotel);
        when(hotelRepository.save(existingHotel)).thenReturn(updatedHotel);
        when(hotelMapper.toDTO(updatedHotel)).thenReturn(expectedDto);

        HotelDTO result = hotelService.updateHotel(hotelId, request);

        assertNotNull(result);
        assertEquals("Updated Hotel", result.getName());
        assertEquals("789 New St", result.getAddress());
        verify(hotelRepository).findById(hotelId);
        verify(hotelMapper).updateEntity(request, existingHotel);
        verify(hotelRepository).save(existingHotel);
    }

    @Test
    void updateHotel_ShouldThrowNotFoundException_WhenNotExists() {
        Long hotelId = 999L;
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("Updated Hotel")
                .address("789 New St")
                .city("Kazan")
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> hotelService.updateHotel(hotelId, request)
        );

        assertTrue(exception.getMessage().contains("Hotel not found"));
        verify(hotelRepository).findById(hotelId);
        verify(hotelMapper, never()).updateEntity(any(), any());
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void deleteHotel_ShouldDeleteHotel_WhenExists() {
        Long hotelId = 1L;
        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        doNothing().when(hotelRepository).deleteById(hotelId);

        hotelService.deleteHotel(hotelId);

        verify(hotelRepository).existsById(hotelId);
        verify(hotelRepository).deleteById(hotelId);
    }

    @Test
    void deleteHotel_ShouldThrowNotFoundException_WhenNotExists() {
        Long hotelId = 999L;
        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> hotelService.deleteHotel(hotelId)
        );

        assertTrue(exception.getMessage().contains("Hotel not found"));
        verify(hotelRepository).existsById(hotelId);
        verify(hotelRepository, never()).deleteById(any());
    }

    @Test
    void getHotelsByCity_ShouldReturnFilteredHotels() {
        String city = "Moscow";
        Hotel hotel1 = Hotel.builder().id(1L).name("Moscow Hotel 1").city(city).build();
        Hotel hotel2 = Hotel.builder().id(2L).name("Moscow Hotel 2").city(city).build();
        HotelDTO dto1 = HotelDTO.builder().id(1L).name("Moscow Hotel 1").city(city).build();
        HotelDTO dto2 = HotelDTO.builder().id(2L).name("Moscow Hotel 2").city(city).build();

        when(hotelRepository.findByCityIgnoreCase(city)).thenReturn(Arrays.asList(hotel1, hotel2));
        when(hotelMapper.toDTO(hotel1)).thenReturn(dto1);
        OngoingStubbing<HotelDTO> hotelDTOOngoingStubbing = when(hotelMapper.toDTO(hotel2)).thenReturn(dto2);

        List<HotelDTO> result = hotelService.getHotelsByCity(city);

        assertEquals(2, result.size());
        assertEquals("Moscow", result.get(0).getCity());
        assertEquals("Moscow", result.get(1).getCity());
        verify(hotelRepository).findByCityIgnoreCase(city);
    }

    @Test
    void getHotelsByCity_ShouldReturnEmptyList_WhenNoCityMatch() {
        String city = "Nonexistent";
        when(hotelRepository.findByCityIgnoreCase(city)).thenReturn(List.of());

        List<HotelDTO> result = hotelService.getHotelsByCity(city);

        assertTrue(result.isEmpty());
        verify(hotelRepository).findByCityIgnoreCase(city);
    }
}
