package sf.mephi.hotel.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sf.mephi.common.constants.RoomType;
import sf.mephi.hotel.entity.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    @Query("SELECT r FROM Room r WHERE r.available = true")
    List<Room> findAllAvailable();

    @Query("SELECT r FROM Room r " +
            "WHERE r.available = true " +
            "ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findRecommendedRooms(Pageable pageable);

    // Поиск по типу номера (для фильтрации)
    List<Room> findByRoomTypeAndAvailableTrue(RoomType roomType);
}
