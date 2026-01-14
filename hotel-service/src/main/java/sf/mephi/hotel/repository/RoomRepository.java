package sf.mephi.hotel.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mephi.common.constants.RoomType;
import sf.mephi.hotel.entity.Room;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    @Query("SELECT r FROM Room r WHERE r.available = true")
    List<Room> findAllAvailable();

    @Query("SELECT r FROM Room r " +
            "WHERE r.available = true " +
            "ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findRecommendedRooms(Pageable pageable);

    @Query("SELECT r FROM Room r " +
            "WHERE r.available = true " +
            "AND r.hotel.id = :hotelId " +
            "AND r.roomType = :roomType " +
            "ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findAvailableRoomsByTypeOrderByLoad(
            @Param("hotelId") Long hotelId,
            @Param("roomType") RoomType roomType
    );

    // Для статистики распределения
    @Query("SELECT AVG(r.timesBooked) FROM Room r WHERE r.available = true")
    Double calculateAverageLoad();

    @Query("SELECT MIN(r.timesBooked) FROM Room r WHERE r.available = true")
    Integer findMinLoad();

    @Query("SELECT MAX(r.timesBooked) FROM Room r WHERE r.available = true")
    Integer findMaxLoad();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);

    // Поиск по типу номера (для фильтрации)
    List<Room> findByRoomTypeAndAvailableTrue(RoomType roomType);
}
