package sf.mephi.hotel.entity;

import org.junit.jupiter.api.Test;
import sf.mephi.common.constants.RoomType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void incrementTimesBooked_ShouldIncreaseCounter() {
        Room room = Room.builder().timesBooked(5).build();

        room.incrementTimesBooked();

        assertEquals(6, room.getTimesBooked());
    }

    @Test
    void decrementTimesBooked_ShouldDecreaseCounter_WhenAboveZero() {
        Room room = Room.builder().timesBooked(5).build();

        room.decrementTimesBooked();

        assertEquals(4, room.getTimesBooked());
    }

    @Test
    void decrementTimesBooked_ShouldNotGoBelowZero() {
        Room room = Room.builder().timesBooked(0).build();

        room.decrementTimesBooked();

        assertEquals(0, room.getTimesBooked());
    }

    @Test
    void builder_ShouldCreateRoomWithAllFields() {
        Hotel hotel = Hotel.builder().id(1L).name("Test Hotel").build();

        Room room = Room.builder()
                .id(1L)
                .hotel(hotel)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .price(new BigDecimal("5000.00"))
                .available(true)
                .timesBooked(3)
                .currentRequestId("req-123")
                .version(0)
                .build();

        assertEquals(1L, room.getId());
        assertEquals("101", room.getRoomNumber());
        assertEquals(RoomType.DOUBLE, room.getRoomType());
        assertEquals(new BigDecimal("5000.00"), room.getPrice());
        assertTrue(room.getAvailable());
        assertEquals(3, room.getTimesBooked());
        assertEquals("req-123", room.getCurrentRequestId());
    }
}
