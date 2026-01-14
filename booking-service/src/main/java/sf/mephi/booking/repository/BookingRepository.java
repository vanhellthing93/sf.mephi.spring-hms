package sf.mephi.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mephi.booking.entity.Booking;
import sf.mephi.common.constants.BookingStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Optional<Booking> findByRequestId(String requestId);

    @Query("SELECT b FROM Booking b WHERE b.status <> :status")
    List<Booking> findActiveBookings(@Param("status") BookingStatus status);

    List<Booking> findByStatus(BookingStatus status);

    /**
     * Подсчитать количество бронирований для комнаты в определённом статусе
     * Используется для проверки балансировки нагрузки
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.roomId = :roomId AND b.status = :status")
    long countByRoomIdAndStatus(@Param("roomId") Long roomId,
                                @Param("status") BookingStatus status);
}
