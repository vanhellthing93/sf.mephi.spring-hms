package sf.mephi.hotel.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotelTest {

    @Test
    void builder_ShouldCreateHotelWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        List<Room> rooms = new ArrayList<>();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Grand Hotel")
                .address("123 Main St")
                .city("Moscow")
                .createdAt(now)
                .rooms(rooms)
                .build();

        assertEquals(1L, hotel.getId());
        assertEquals("Grand Hotel", hotel.getName());
        assertEquals("123 Main St", hotel.getAddress());
        assertEquals("Moscow", hotel.getCity());
        assertEquals(now, hotel.getCreatedAt());
        assertEquals(rooms, hotel.getRooms());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("City Inn");
        hotel.setAddress("456 Park Ave");
        hotel.setCity("Saint Petersburg");

        assertEquals(1L, hotel.getId());
        assertEquals("City Inn", hotel.getName());
        assertEquals("456 Park Ave", hotel.getAddress());
        assertEquals("Saint Petersburg", hotel.getCity());
    }

    @Test
    void rooms_ShouldBeInitializedAsEmptyList() {
        Hotel hotel = Hotel.builder()
                .name("Test Hotel")
                .address("Test Address")
                .city("Test City")
                .build();

        assertNotNull(hotel.getRooms());
        assertTrue(hotel.getRooms().isEmpty());
    }

    @Test
    void addRoom_ShouldAddRoomToList() {
        Hotel hotel = Hotel.builder()
                .name("Test Hotel")
                .rooms(new ArrayList<>())
                .build();

        Room room = Room.builder()
                .roomNumber("101")
                .hotel(hotel)
                .build();

        hotel.getRooms().add(room);

        assertEquals(1, hotel.getRooms().size());
        assertEquals("101", hotel.getRooms().get(0).getRoomNumber());
    }
}
