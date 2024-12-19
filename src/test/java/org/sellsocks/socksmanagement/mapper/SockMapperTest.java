package org.sellsocks.socksmanagement.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.entity.Sock;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.sellsocks.socksmanagement.model.enums.SockColor.WHITE;

public class SockMapperTest {
    private final SockMapper mapper = new SockMapperImpl();

    @Test
    @DisplayName("mapping to dto")
    public void toSockDtoTest() {
        Sock entity = getEntity();
        SockDto dto = mapper.toSockDto(entity);

        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals(entity.getColor().toString(), dto.getColor()),
                () -> assertEquals(entity.getCottonPart(), dto.getCottonPart()),
                () -> assertEquals(entity.getQuantity(), dto.getQuantity())
        );
    }

    @Test
    @DisplayName("mapping to dto, null input")
    public void toSockDtoNullTest() {
        SockDto dto = mapper.toSockDto(null);

        assertNull(dto);
    }

    @Test
    @DisplayName("mapping to entity")
    public void toSockEntityTest() {
        SockDto dto = getDto();
        Sock entity = mapper.toSockEntity(dto);

        assertAll(
                () -> assertNotNull(entity),
                () -> assertEquals(dto.getColor().toUpperCase(), entity.getColor().toString()),
                () -> assertEquals(dto.getCottonPart(), entity.getCottonPart()),
                () -> assertEquals(dto.getQuantity(), entity.getQuantity())
        );
    }

    @Test
    @DisplayName("mapping to entity, null input")
    public void toSockEntityNullTest() {
        Sock entity = mapper.toSockEntity(null);

        assertNull(entity);
    }

    private Sock getEntity() {
        Sock entity = new Sock();
        entity.setColor(WHITE);
        entity.setQuantity(108);
        entity.setCottonPart(42);
        return entity;
    }

    private SockDto getDto() {
        SockDto dto = new SockDto();
        dto.setColor("black");
        dto.setCottonPart(42);
        dto.setQuantity(108);
        return dto;
    }
}
