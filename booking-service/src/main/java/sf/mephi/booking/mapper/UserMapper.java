package sf.mephi.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import sf.mephi.booking.dto.request.RegisterRequest;
import sf.mephi.booking.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Будет зашифрован отдельно
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    User toEntity(RegisterRequest request);
}
