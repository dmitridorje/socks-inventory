package org.sellsocks.socksmanagement.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.entity.Sock;
import org.sellsocks.socksmanagement.model.enums.SockColor;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SockMapper {

    @Mapping(target = "color", source = "color", qualifiedByName = "stringToEnum")
    Sock toSockEntity(SockDto sockDto);

    SockDto toSockDto(Sock sock);

    @Named("stringToEnum")
    default SockColor stringToEnum(String color) {
        try {
            return SockColor.valueOf(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid color: " + color);
        }
    }
}
