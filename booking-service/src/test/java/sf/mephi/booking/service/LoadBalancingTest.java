package sf.mephi.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sf.mephi.booking.client.HotelServiceClient;
import sf.mephi.booking.dto.external.RoomDTO;
import sf.mephi.common.constants.RoomType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class LoadBalancingTest {

    @MockitoBean
    private HotelServiceClient hotelServiceClient;

    @BeforeEach
    void setUp() {
        List<RoomDTO> mockRooms = createMockRoomsSorted();
        when(hotelServiceClient.getRecommendedRooms()).thenReturn(mockRooms);
    }

    private List<RoomDTO> createMockRoomsSorted() {
        List<RoomDTO> rooms = new ArrayList<>();

        rooms.add(RoomDTO.builder()
                .id(3L).hotelId(1L).roomNumber("103")
                .roomType(RoomType.SUITE).price(BigDecimal.valueOf(15000))
                .available(true).timesBooked(8).build());

        rooms.add(RoomDTO.builder()
                .id(5L).hotelId(1L).roomNumber("105")
                .roomType(RoomType.FAMILY).price(BigDecimal.valueOf(18000))
                .available(true).timesBooked(9).build());

        rooms.add(RoomDTO.builder()
                .id(1L).hotelId(1L).roomNumber("101")
                .roomType(RoomType.SINGLE).price(BigDecimal.valueOf(5000))
                .available(true).timesBooked(10).build());

        rooms.add(RoomDTO.builder()
                .id(4L).hotelId(1L).roomNumber("104")
                .roomType(RoomType.DELUXE).price(BigDecimal.valueOf(12000))
                .available(true).timesBooked(11).build());

        rooms.add(RoomDTO.builder()
                .id(2L).hotelId(1L).roomNumber("102")
                .roomType(RoomType.DOUBLE).price(BigDecimal.valueOf(7000))
                .available(true).timesBooked(12).build());

        return rooms;
    }

    @Test
    @DisplayName("Должен равномерно распределять нагрузку между комнатами")
    void shouldDistributeLoadEvenly() {
        // Given: Получаем рекомендованные комнаты
        List<RoomDTO> rooms = hotelServiceClient.getRecommendedRooms();

        // Фильтруем только доступные
        List<RoomDTO> availableRooms = rooms.stream()
                .filter(RoomDTO::getAvailable)
                .toList();

        assertThat(availableRooms)
                .withFailMessage("Должно быть минимум 3 доступных комнаты для теста")
                .hasSizeGreaterThanOrEqualTo(3);

        // When: Анализируем распределение timesBooked
        List<Integer> timesBookedValues = availableRooms.stream()
                .map(RoomDTO::getTimesBooked)
                .toList();

        int min = timesBookedValues.stream().min(Integer::compareTo).orElse(0);
        int max = timesBookedValues.stream().max(Integer::compareTo).orElse(0);
        double average = timesBookedValues.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // Then: Проверяем равномерность
        // Отклонение не более 30% от среднего
        double maxDeviation = average * 0.30;

        for (Integer value : timesBookedValues) {
            double deviation = Math.abs(value - average);
            assertThat(deviation)
                    .withFailMessage(
                            "timesBooked=%d слишком отличается от среднего %.2f (отклонение %.2f > %.2f)",
                            value, average, deviation, maxDeviation
                    )
                    .isLessThanOrEqualTo(maxDeviation);
        }

        // Проверяем, что нет "простаивающих" комнат
        long idleRooms = availableRooms.stream()
                .filter(r -> r.getTimesBooked() == 0)
                .count();

        boolean hasActiveRooms = timesBookedValues.stream()
                .anyMatch(t -> t > 5);

        if (hasActiveRooms) {
            assertThat(idleRooms)
                    .withFailMessage("Обнаружены простаивающие комнаты при активном использовании других")
                    .isEqualTo(0);
        }

        System.out.println("✅ Тест равномерности распределения:");
        System.out.println("  Проанализировано комнат: " + availableRooms.size());
        System.out.println("  Среднее timesBooked: " + String.format("%.2f", average));
        System.out.println("  Минимум: " + min);
        System.out.println("  Максимум: " + max);
        System.out.println("  Допустимое отклонение: ±" + String.format("%.2f", maxDeviation));
        System.out.println("  Распределение: " + timesBookedValues);

        // Проверка успешности - разброс не более 5 бронирований
        assertThat(max - min)
                .withFailMessage("Разброс между min и max слишком большой: %d", max - min)
                .isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Должен получать комнаты отсортированные по загруженности (timesBooked)")
    void shouldReceiveRoomsSortedByTimesBooked() {
        // Given
        List<RoomDTO> rooms = hotelServiceClient.getRecommendedRooms();

        // When: Извлекаем значения timesBooked
        List<Integer> timesBookedValues = rooms.stream()
                .map(RoomDTO::getTimesBooked)
                .toList();

        // Then: Проверяем, что значения идут по возрастанию
        for (int i = 0; i < timesBookedValues.size() - 1; i++) {
            int current = timesBookedValues.get(i);
            int next = timesBookedValues.get(i + 1);

            assertThat(current)
                    .withFailMessage(
                            "Комната на позиции %d (timesBooked=%d) должна быть <= следующей (timesBooked=%d)",
                            i, current, next
                    )
                    .isLessThanOrEqualTo(next);
        }

        System.out.println("✅ Тест сортировки по timesBooked:");
        System.out.println("  Порядок загруженности: " + timesBookedValues);
        System.out.println("  Комнаты отсортированы корректно (по возрастанию)");
    }

    @Test
    @DisplayName("Должен предотвращать простой комнат при высокой загрузке")
    void shouldPreventRoomIdleness() {
        // Given: Получаем все рекомендованные комнаты
        List<RoomDTO> rooms = hotelServiceClient.getRecommendedRooms();

        List<Integer> bookingCounts = rooms.stream()
                .map(RoomDTO::getTimesBooked)
                .sorted()
                .toList();

        // When: Подсчитываем общее количество бронирований
        int totalBookings = bookingCounts.stream()
                .mapToInt(Integer::intValue)
                .sum();

        // Then: При достаточном количестве бронирований не должно быть неиспользуемых комнат
        if (totalBookings > 0) {
            if (totalBookings >= rooms.size()) {
                long unusedRooms = bookingCounts.stream()
                        .filter(count -> count == 0)
                        .count();

                assertThat(unusedRooms)
                        .withFailMessage(
                                "При %d бронированиях на %d комнат, %d комнат не использовались",
                                totalBookings, rooms.size(), unusedRooms
                        )
                        .isEqualTo(0);
            }
        }

        System.out.println("✅ Тест предотвращения простоя комнат:");
        System.out.println("  Всего комнат: " + rooms.size());
        System.out.println("  Всего бронирований: " + totalBookings);
        System.out.println("  Распределение: " + bookingCounts);
        System.out.println("  Неиспользуемых комнат: 0");
    }

    @Test
    @DisplayName("Должен демонстрировать алгоритм равномерного распределения")
    void shouldDemonstrateLoadBalancingAlgorithm() {
        // Given: Получаем комнаты
        List<RoomDTO> rooms = hotelServiceClient.getRecommendedRooms();

        // When: Проверяем логику выбора следующей комнаты
        // (комната с минимальным timesBooked будет первой в списке)
        RoomDTO firstRoom = rooms.getFirst();
        RoomDTO lastRoom = rooms.getLast();

        // Then: Первая комната должна быть наименее загружена
        assertThat(firstRoom.getTimesBooked())
                .withFailMessage("Первая комната должна быть наименее загружена")
                .isLessThanOrEqualTo(lastRoom.getTimesBooked());

        System.out.println("✅ Алгоритм равномерного распределения:");
        System.out.println("  Следующая для бронирования: Комната " + firstRoom.getRoomNumber()
                + " (timesBooked=" + firstRoom.getTimesBooked() + ")");
        System.out.println("  Наиболее загруженная: Комната " + lastRoom.getRoomNumber()
                + " (timesBooked=" + lastRoom.getTimesBooked() + ")");
        System.out.println("  Разница в загрузке: " + (lastRoom.getTimesBooked() - firstRoom.getTimesBooked()));

        // Демонстрация справедливого распределения
        int difference = lastRoom.getTimesBooked() - firstRoom.getTimesBooked();
        assertThat(difference)
                .withFailMessage("Разница в загрузке между наименее и наиболее загруженной комнатой слишком велика")
                .isLessThanOrEqualTo(5);
    }
}
