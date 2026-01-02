package sf.mephi.hotel.mapper;

import org.mapstruct.*;
import sf.mephi.hotel.dto.request.CreateHotelRequest;
import sf.mephi.hotel.dto.response.HotelDTO;
import sf.mephi.hotel.entity.Hotel;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HotelMapper {

    @Mapping(target = "totalRooms", expression = "java(hotel.getRooms() != null ? hotel.getRooms().size() : 0)")
    HotelDTO toDTO(Hotel hotel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Hotel toEntity(CreateHotelRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    void updateEntity(CreateHotelRequest request, @MappingTarget Hotel hotel);
}
