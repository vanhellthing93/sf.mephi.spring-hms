package sf.mephi.common.constants;

/**
 * Статусы бронирования для Saga Pattern
 */
public enum BookingStatus {
    /**
     * Бронирование создано, ожидает подтверждения от Hotel Service
     */
    PENDING,

    /**
     * Бронирование подтверждено, номер забронирован
     */
    CONFIRMED,

    /**
     * Бронирование отменено (компенсация или пользователем)
     */
    CANCELLED,

    /**
     * Бронирование завершено (гость выехал)
     */
    COMPLETED,

    /**
     * Ошибка при обработке бронирования
     */
    FAILED
}
