package sf.mephi.common.constants;

/**
 * Константы для API endpoints и общих параметров
 */
public final class ApiConstants {

    // Base paths
    public static final String API_V1 = "/api/v1";

    // Hotel Service endpoints
    public static final String HOTELS_PATH = "/hotels";
    public static final String ROOMS_PATH = "/rooms";
    public static final String ROOMS_RECOMMEND_PATH = "/rooms/recommend";
    public static final String ROOMS_AVAILABLE_PATH = "/rooms/available";
    public static final String ROOMS_CONFIRM_PATH = "/rooms/{id}/confirm-availability";
    public static final String ROOMS_RELEASE_PATH = "/rooms/{id}/release";

    // Full Hotel Service paths
    public static final String HOTELS_FULL_PATH = API_V1 + HOTELS_PATH;
    public static final String ROOMS_FULL_PATH = API_V1 + ROOMS_PATH;
    public static final String ROOMS_RECOMMEND_FULL_PATH = API_V1 + ROOMS_RECOMMEND_PATH;

    // Booking Service endpoints
    public static final String BOOKINGS_PATH = "/bookings";
    public static final String BOOKING_BY_ID_PATH = "/bookings/{id}";
    public static final String USER_PATH = "/user";
    public static final String USER_REGISTER_PATH = "/user/register";
    public static final String USER_AUTH_PATH = "/user/auth";

    // Full Booking Service paths
    public static final String BOOKINGS_FULL_PATH = API_V1 + BOOKINGS_PATH;
    public static final String USER_FULL_PATH = API_V1 + USER_PATH;
    public static final String USER_REGISTER_FULL_PATH = API_V1 + USER_REGISTER_PATH;
    public static final String USER_AUTH_FULL_PATH = API_V1 + USER_AUTH_PATH;

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    // Request/Response headers
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    // Validation constraints
    public static final int MIN_BOOKING_DAYS = 1;
    public static final int MAX_BOOKING_DAYS = 30;
    public static final int MAX_HOTEL_NAME_LENGTH = 200;
    public static final int MAX_ADDRESS_LENGTH = 500;
    public static final int MAX_ROOM_NUMBER_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;

    // Resilience4j instance names
    public static final String HOTEL_SERVICE_CIRCUIT_BREAKER = "hotelServiceCircuitBreaker";
    public static final String HOTEL_SERVICE_RETRY = "hotelServiceRetry";
    public static final String HOTEL_SERVICE_TIMEOUT = "hotelServiceTimeout";

    // Error messages
    public static final String ERROR_HOTEL_NOT_FOUND = "Hotel not found with id: %d";
    public static final String ERROR_ROOM_NOT_FOUND = "Room not found with id: %d";
    public static final String ERROR_BOOKING_NOT_FOUND = "Booking not found with id: %d";
    public static final String ERROR_USER_NOT_FOUND_ID = "User not found with id: %d";
    public static final String ERROR_USER_NOT_FOUND = "User not found with id: %s";
    public static final String ERROR_USER_ALREADY_EXISTS = "User with username '%s' already exists";
    public static final String ERROR_ROOM_UNAVAILABLE = "Room is not available for selected dates";
    public static final String ERROR_INVALID_DATE_RANGE = "End date must be after start date";
    public static final String ERROR_BOOKING_TOO_LONG = "Booking duration exceeds maximum allowed days (%d)";
    public static final String ERROR_UNAUTHORIZED = "Authentication required";
    public static final String ERROR_FORBIDDEN = "Access denied";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";

    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
