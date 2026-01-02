package sf.mephi.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sf.mephi.hotel.entity.Hotel;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Поиск по городу (для фильтрации)
    List<Hotel> findByCityIgnoreCase(String city);
}
