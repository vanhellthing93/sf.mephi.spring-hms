package sf.mephi.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sf.mephi.booking.client.HotelServiceClient;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceApplicationTests {

    @MockitoBean
    private HotelServiceClient hotelServiceClient;

    @Test
    void contextLoads() {
        // Контекст должен успешно загрузиться
    }
}
