package sf.mephi.hotel.mapper;

import org.mapstruct.*;
import sf.mephi.hotel.dto.request.CreateRoomRequest;
import sf.mephi.hotel.dto.response.RoomDTO;
import sf.mephi.hotel.entity.Room;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomMapper {

    @Mapping(target = "hotelId", source = "hotel.id")
    @Mapping(target = "hotelName", source = "hotel.name")
    RoomDTO toDTO(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "timesBooked", constant = "0")
    Room toEntity(CreateRoomRequest request);
}
