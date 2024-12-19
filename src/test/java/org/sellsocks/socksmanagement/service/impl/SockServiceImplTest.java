package org.sellsocks.socksmanagement.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellsocks.socksmanagement.mapper.SockMapper;
import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.entity.Sock;
import org.sellsocks.socksmanagement.model.enums.SockColor;
import org.sellsocks.socksmanagement.repository.SockRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.sellsocks.socksmanagement.model.enums.SockColor.BLACK;

@ExtendWith(MockitoExtension.class)
public class SockServiceImplTest {

    private Sock existingSock;
    private Sock updatedSock;
    private Sock outputSock;
    private SockColor color;
    private int cottonPart;
    private int deltaQuantity;

    @Mock
    private SockRepository sockRepository;

    @Mock
    private SockMapper sockMapper;

    @InjectMocks
    private SockServiceImpl sockService;

    @BeforeEach
    void setUp() {
        color = BLACK;
        cottonPart = 30;
        deltaQuantity = 50;

        existingSock = Sock.builder()
                .id(1L)
                .color(color)
                .cottonPart(cottonPart)
                .quantity(100)
                .build();

        outputSock = Sock.builder()
                .color(color)
                .cottonPart(cottonPart)
                .quantity(deltaQuantity)
                .build();
    }

    @AfterEach
    void tearDown() {
        existingSock = outputSock = null;
    }

    @Test
    @DisplayName("Should add sock income successfully")
    void addSockIncome_Success() {
        Sock inputSock = Sock.builder()
                .color(color)
                .cottonPart(cottonPart)
                .quantity(deltaQuantity)
                .build();

        updatedSock = Sock.builder()
                .id(1L)
                .color(color)
                .cottonPart(cottonPart)
                .quantity(150)
                .build();

        SockDto expectedDto = SockDto.builder()
                .color(color.toString())
                .cottonPart(cottonPart)
                .quantity(150)
                .build();

        when(sockRepository.findByColorAndCottonPart(color, cottonPart))
                .thenReturn(Optional.of(existingSock));
        when(sockRepository.save(Mockito.any(Sock.class)))
                .thenReturn(updatedSock);

        when(sockMapper.toSockDto(updatedSock)).thenReturn(expectedDto);

        SockDto result = sockService.addSockIncome(inputSock);

        assertNotNull(result);
        assertEquals(expectedDto, result);

        verify(sockRepository).findByColorAndCottonPart(color, cottonPart);
        verify(sockRepository).save(argThat(sock ->
                sock.getColor().equals(color) &&
                        sock.getCottonPart() == cottonPart &&
                        sock.getQuantity() == 150
        ));
        verify(sockMapper).toSockDto(updatedSock);
    }

    @Test
    @DisplayName("Should subtract socks successfully")
    void subtractSockOutcome_Success() {
        updatedSock = Sock.builder()
                .id(1L)
                .color(color)
                .cottonPart(cottonPart)
                .quantity(50)
                .build();

        SockDto expectedDto = SockDto.builder()
                .color(color.toString())
                .cottonPart(cottonPart)
                .quantity(50)
                .build();

        when(sockRepository.findByColorAndCottonPart(BLACK, cottonPart))
                .thenReturn(Optional.of(existingSock));
        when(sockMapper.toSockDto(updatedSock))
                .thenReturn(expectedDto);

        SockDto result = sockService.subtractSockOutcome(outputSock);

        assertNotNull(result);
        assertEquals(expectedDto, result);

        verify(sockRepository).findByColorAndCottonPart(BLACK, cottonPart);
        verify(sockMapper).toSockDto(updatedSock);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if socks are not found")
    void subtractSockOutcome_NotFound() {
        when(sockRepository.findByColorAndCottonPart(BLACK, cottonPart))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> sockService.subtractSockOutcome(outputSock));

        assertEquals("No socks found with given parameters: color = BLACK, cotton percentage = 30",
                exception.getMessage());

        verify(sockRepository).findByColorAndCottonPart(BLACK, cottonPart);
        verifyNoMoreInteractions(sockRepository);
        verifyNoInteractions(sockMapper);
    }

    @Test
    @DisplayName("Should throw IllegalStateException if requested quantity exceeds available")
    void subtractSockOutcome_IllegalState() {
        outputSock = Sock.builder()
                .color(color)
                .cottonPart(cottonPart)
                .quantity(101)
                .build();

        when(sockRepository.findByColorAndCottonPart(BLACK, cottonPart))
                .thenReturn(Optional.of(existingSock));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sockService.subtractSockOutcome(outputSock));

        assertEquals("Not enough socks in stock: available=100, requested=101", exception.getMessage());

        verify(sockRepository).findByColorAndCottonPart(BLACK, cottonPart);
        verifyNoMoreInteractions(sockRepository);
        verifyNoInteractions(sockMapper);
    }
}
