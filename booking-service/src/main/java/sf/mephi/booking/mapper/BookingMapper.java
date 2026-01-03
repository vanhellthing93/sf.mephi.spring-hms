package sf.mephi.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import sf.mephi.booking.dto.request.CreateBookingRequest;
import sf.mephi.booking.dto.response.BookingDTO;
import sf.mephi.booking.entity.Booking;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    BookingDTO toDTO(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requestId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Booking toEntity(CreateBookingRequest request);
}
