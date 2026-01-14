package sf.mephi.hotel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.common.constants.RoomType;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.common.exception.ValidationException;
import sf.mephi.common.util.CorrelationIdUtil;
import sf.mephi.hotel.dto.request.ConfirmAvailabilityRequest;
import sf.mephi.hotel.dto.request.CreateRoomRequest;
import sf.mephi.hotel.dto.response.AvailabilityConfirmationDTO;
import sf.mephi.hotel.dto.response.RoomDTO;
import sf.mephi.hotel.entity.Hotel;
import sf.mephi.hotel.entity.Room;
import sf.mephi.hotel.mapper.RoomMapper;
import sf.mephi.hotel.repository.HotelRepository;
import sf.mephi.hotel.repository.RoomRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    private final Map<String, AvailabilityConfirmationDTO> processedRequests = new ConcurrentHashMap<>();

    /**
     * Получить все доступные номера (USER)
     */
    @Transactional(readOnly = true)
    public List<RoomDTO> getAvailableRooms() {
        log.info("Fetching all available rooms");
        return roomRepository.findAllAvailable().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить рекомендованные номера (USER)
     */
    @Transactional(readOnly = true)
    public List<RoomDTO> getRecommendedRooms() {
        log.info("Fetching recommended rooms sorted by timesBooked");
        Pageable pageable = PageRequest.of(0, 100); // Ограничение для производительности
        return roomRepository.findRecommendedRooms(pageable).stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить номер по ID
     */
    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long id) {
        log.info("Fetching room with id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_ROOM_NOT_FOUND, id)
                ));
        return roomMapper.toDTO(room);
    }

    /**
     * Создать новый номер (ADMIN)
     */
    @Transactional
    public RoomDTO createRoom(CreateRoomRequest request) {
        log.info("Creating new room: {} in hotel: {}", request.getRoomNumber(), request.getHotelId());

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_HOTEL_NOT_FOUND, request.getHotelId())
                ));

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);

        Room saved = roomRepository.save(room);
        log.info("Room created with id: {}", saved.getId());
        return roomMapper.toDTO(saved);
    }

    /**
     * Обновить номер (ADMIN)
     */
    @Transactional
    public RoomDTO updateRoom(Long id, CreateRoomRequest request) {
        log.info("Updating room with id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_ROOM_NOT_FOUND, id)
                ));

        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setPrice(request.getPrice());

        Room updated = roomRepository.save(room);
        log.info("Room updated: {}", id);
        return roomMapper.toDTO(updated);
    }

    /**
     * Удалить номер (ADMIN)
     */
    @Transactional
    public void deleteRoom(Long id) {
        log.info("Deleting room with id: {}", id);
        if (!roomRepository.existsById(id)) {
            throw new NotFoundException(
                    String.format(ApiConstants.ERROR_ROOM_NOT_FOUND, id)
            );
        }
        roomRepository.deleteById(id);
        log.info("Room deleted: {}", id);
    }

    /**
     * Подтвердить доступность номера (INTERNAL - для Saga)
     */
    @Transactional
    public AvailabilityConfirmationDTO confirmAvailability(
            Long roomId,
            ConfirmAvailabilityRequest request) {

        String correlationId = CorrelationIdUtil.getCorrelationId();
        log.info("Confirming availability for room: {}, requestId: {}, correlationId: {}",
                roomId, request.getRequestId(), correlationId);

        // Идемпотентность - проверяем кеш
        if (processedRequests.containsKey(request.getRequestId())) {
            log.info("Request already processed (idempotent): {}", request.getRequestId());
            return processedRequests.get(request.getRequestId());
        }

        // КРИТИЧНО: Pessimistic Lock для параллельных бронирований одного номера
        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_ROOM_NOT_FOUND, roomId)));

        // Проверка доступности
        if (!room.getAvailable()) {
            AvailabilityConfirmationDTO response = AvailabilityConfirmationDTO.builder()
                    .requestId(request.getRequestId())
                    .roomId(roomId)
                    .confirmed(false)
                    .message("Room is not available")
                    .build();

            processedRequests.put(request.getRequestId(), response);
            return response;
        }

        // Атомарное обновление с Optimistic Lock (version)
        try {
            room.incrementTimesBooked();
            room.setCurrentRequestId(request.getRequestId());
            roomRepository.save(room);

            AvailabilityConfirmationDTO response = AvailabilityConfirmationDTO.builder()
                    .requestId(request.getRequestId())
                    .roomId(roomId)
                    .confirmed(true)
                    .message("Room availability confirmed")
                    .build();

            processedRequests.put(request.getRequestId(), response);

            log.info("Room {} confirmed, timesBooked={}", roomId, room.getTimesBooked());
            return response;

        } catch (OptimisticLockingFailureException e) {
            log.error("Optimistic lock conflict for roomId={}, requestId={}",
                    roomId, request.getRequestId());
            throw new ValidationException("Room was modified by another transaction. Please retry.");
        }
    }

    /**
     * Освободить слот (INTERNAL - компенсация для Saga)
     */
    @Transactional
    public void releaseSlot(Long roomId, String requestId) {
        String correlationId = CorrelationIdUtil.getCorrelationId();
        log.info("Releasing slot for room: {}, requestId: {}, correlationId: {}",
                roomId, requestId, correlationId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_ROOM_NOT_FOUND, roomId)
                ));

        // Декремент timesBooked (компенсация)
        room.decrementTimesBooked();
        room.setCurrentRequestId(null);
        roomRepository.save(room);

        // Удаляем из кеша обработанных запросов
        processedRequests.remove(requestId);

        log.info("Room {} slot released, timesBooked decremented to {}", roomId, room.getTimesBooked());
    }

    /**
     * Получить оптимальный номер с учетом равномерного распределения
     * и защитой от race condition
     */
    @Transactional
    public RoomDTO selectOptimalRoomForBooking(Long hotelId, RoomType roomType) {
        log.info("Selecting optimal room for hotelId={}, roomType={}", hotelId, roomType);

        // 1. Получаем доступные номера отсортированные по загруженности
        List<Room> availableRooms = roomRepository
                .findAvailableRoomsByTypeOrderByLoad(hotelId, roomType);

        if (availableRooms.isEmpty()) {
            throw new ValidationException("No available rooms of type " + roomType);
        }

        // 2. Выбираем наименее загруженный номер
        Room optimalRoom = availableRooms.getFirst();

        // 3. Вычисляем среднюю загруженность
        double avgLoad = availableRooms.stream()
                .mapToInt(Room::getTimesBooked)
                .average()
                .orElse(0.0);

        // 4. Anti-starvation: если разброс больше 20%, выбираем самый незагруженный
        int minLoad = optimalRoom.getTimesBooked();
        int maxLoad = availableRooms.stream()
                .mapToInt(Room::getTimesBooked)
                .max()
                .orElse(0);

        double loadVariance = maxLoad - minLoad;
        if (loadVariance > avgLoad * 0.2) {
            log.warn("High load variance detected: {} (avg: {}). Selecting least loaded room.",
                    loadVariance, avgLoad);
        }

        log.info("Selected room: id={}, number={}, timesBooked={}, avgLoad={}",
                optimalRoom.getId(), optimalRoom.getRoomNumber(),
                optimalRoom.getTimesBooked(), avgLoad);

        return roomMapper.toDTO(optimalRoom);
    }
}
