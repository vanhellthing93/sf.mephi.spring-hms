package sf.mephi.hotel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.hotel.dto.request.CreateHotelRequest;
import sf.mephi.hotel.dto.response.HotelDTO;
import sf.mephi.hotel.entity.Hotel;
import sf.mephi.hotel.mapper.HotelMapper;
import sf.mephi.hotel.repository.HotelRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    /**
     * Получить все отели
     */
    @Transactional(readOnly = true)
    public List<HotelDTO> getAllHotels() {
        log.info("Fetching all hotels");
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить отель по ID
     */
    @Transactional(readOnly = true)
    public HotelDTO getHotelById(Long id) {
        log.info("Fetching hotel with id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_HOTEL_NOT_FOUND, id)
                ));
        return hotelMapper.toDTO(hotel);
    }

    /**
     * Создать новый отель (ADMIN)
     */
    @Transactional
    public HotelDTO createHotel(CreateHotelRequest request) {
        log.info("Creating new hotel: {}", request.getName());
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel saved = hotelRepository.save(hotel);
        log.info("Hotel created with id: {}", saved.getId());
        return hotelMapper.toDTO(saved);
    }

    /**
     * Обновить отель (ADMIN)
     */
    @Transactional
    public HotelDTO updateHotel(Long id, CreateHotelRequest request) {
        log.info("Updating hotel with id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_HOTEL_NOT_FOUND, id)
                ));

        hotelMapper.updateEntity(request, hotel);
        Hotel updated = hotelRepository.save(hotel);
        log.info("Hotel updated: {}", id);
        return hotelMapper.toDTO(updated);
    }

    /**
     * Удалить отель (ADMIN)
     */
    @Transactional
    public void deleteHotel(Long id) {
        log.info("Deleting hotel with id: {}", id);
        if (!hotelRepository.existsById(id)) {
            throw new NotFoundException(
                    String.format(ApiConstants.ERROR_HOTEL_NOT_FOUND, id)
            );
        }
        hotelRepository.deleteById(id);
        log.info("Hotel deleted: {}", id);
    }

    /**
     * Найти отели по городу
     */
    @Transactional(readOnly = true)
    public List<HotelDTO> getHotelsByCity(String city) {
        log.info("Fetching hotels in city: {}", city);
        return hotelRepository.findByCityIgnoreCase(city).stream()
                .map(hotelMapper::toDTO)
                .collect(Collectors.toList());
    }
}
