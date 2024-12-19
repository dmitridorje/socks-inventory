package org.sellsocks.socksmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.dto.SockUpdateDto;
import org.sellsocks.socksmanagement.model.entity.Sock;
import org.sellsocks.socksmanagement.repository.SockRepository;
import org.sellsocks.socksmanagement.util.ContainerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sellsocks.socksmanagement.model.enums.SockColor.BLACK;
import static org.sellsocks.socksmanagement.model.enums.SockColor.GREEN;
import static org.sellsocks.socksmanagement.model.enums.SockColor.PURPLE;
import static org.sellsocks.socksmanagement.model.enums.SockColor.RED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class SockControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SockRepository sockRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private File tempCsvFile;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = ContainerCreator.POSTGRES_CONTAINER;

    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void overrideSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> false);
    }

    @BeforeEach
    public void setUp() throws IOException {

        objectMapper = new ObjectMapper();

        tempCsvFile = File.createTempFile("socks", ".csv");

        try (FileWriter writer = new FileWriter(tempCsvFile)) {
            writer.append("color,cottonPart,quantity\n");
            writer.append("RED,100,100\n");
        }
    }

    @Test
    @DisplayName("Should add specified quantity and save/update entry in database")
    void testAddSockIncome_Success() throws Exception {
        SockDto sockDto = SockDto.builder()
                .color("black")
                .cottonPart(15)
                .quantity(42)
                .build();

        String contentJson = objectMapper.writeValueAsString(sockDto);

        mockMvc.perform(post("/api/socks/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.cottonPart").value(15))
                .andExpect(jsonPath("$.quantity").value(150))
                .andDo(MockMvcResultHandlers.print());

        Optional<Sock> savedSockOptional = sockRepository.findByColorAndCottonPart(BLACK, 15);

        Sock savedSock = savedSockOptional.orElseThrow(() ->
                new IllegalStateException("Expected a value, but Optional was empty"));

        assertEquals(150, savedSock.getQuantity());
    }

    @Test
    @DisplayName("Should return Bad Request in case of null/blank arguments")
    void testAddSockIncome_BadRequest() throws Exception {
        SockDto invalidSockDto = SockDto.builder()
                .color("")
                .build();

        String contentJson = objectMapper.writeValueAsString(invalidSockDto);

        mockMvc.perform(post("/api/socks/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.color").value("Color must not be blank"))
                .andExpect(jsonPath("$.cottonPart").value("Cotton percentage must not be null"))
                .andExpect(jsonPath("$.quantity").value("Quantity must not be null"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Should subtract specified quantity and save/update entry in database")
    void testSubtractSockOutcome_Success() throws Exception {
        SockDto sockDto = SockDto.builder()
                .color("purple")
                .cottonPart(30)
                .quantity(41)
                .build();

        String contentJson = objectMapper.writeValueAsString(sockDto);

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.color").value("PURPLE"))
                .andExpect(jsonPath("$.cottonPart").value(30))
                .andExpect(jsonPath("$.quantity").value(1))
                .andDo(MockMvcResultHandlers.print());

        Optional<Sock> savedSockOptional = sockRepository.findByColorAndCottonPart(PURPLE, 30);

        Sock savedSock = savedSockOptional.orElseThrow(() ->
                new IllegalStateException("Expected a value, but Optional was empty"));

        assertEquals(1, savedSock.getQuantity());
    }

    @Test
    @DisplayName("Should upload socks batch with real CSV file")
    public void testUploadSocksBatch_Success() throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(tempCsvFile)) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    tempCsvFile.getName(),
                    MediaType.MULTIPART_FORM_DATA_VALUE,
                    fileInputStream
            );

            mockMvc.perform(multipart("/api/socks/batch")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isOk());

            Optional<Sock> savedSockOptional = sockRepository.findByColorAndCottonPart(RED, 100);

            Sock savedSock = savedSockOptional.orElseThrow(() ->
                    new IllegalStateException("Expected a value, but Optional was empty"));

            assertEquals(100, savedSock.getQuantity());
        }
    }

    @Test
    @DisplayName("Should return total number of socks according to criteria provided")
    void testGetNumberOfSocks_Success() throws Exception {
        String color = "purple";
        String operation = "moreThan";
        int cottonPercent = 25;

        mockMvc.perform(get("/api/socks")
                        .param("color", (color))
                        .param("operation", (operation))
                        .param("cottonPart", valueOf(cottonPercent)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("84"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Should update entry in database according to data provided")
    void testUpdateSock_Success() throws Exception {
        Long sockId = 2L;

        SockUpdateDto sockUpdateDto = SockUpdateDto.builder()
                .color("green")
                .cottonPart(1)
                .quantity(99)
                .build();

        String contentJson = objectMapper.writeValueAsString(sockUpdateDto);

        mockMvc.perform(put("/api/socks/{id}", sockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.color").value("GREEN"))
                .andExpect(jsonPath("$.cottonPart").value(1))
                .andExpect(jsonPath("$.quantity").value(99))
                .andDo(MockMvcResultHandlers.print());

        Optional<Sock> savedSockOptional = sockRepository.findByColorAndCottonPart(GREEN, 1);

        Sock savedSock = savedSockOptional.orElseThrow(() ->
                new IllegalStateException("Expected a value, but Optional was empty"));

        assertEquals(99, savedSock.getQuantity());
    }

    @Test
    @DisplayName("Should merge entry onto existing one after update if color/cotton field are identical")
    void testUpdateSock_Merge() throws Exception {
        Long sockId = 2L;

        SockUpdateDto sockUpdateDto = SockUpdateDto.builder()
                .color("black")
                .cottonPart(15)
                .quantity(92)
                .build();

        String contentJson = objectMapper.writeValueAsString(sockUpdateDto);

        mockMvc.perform(put("/api/socks/{id}", sockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.cottonPart").value(15))
                .andExpect(jsonPath("$.quantity").value(200))
                .andDo(MockMvcResultHandlers.print());

        Optional<Sock> savedSockOptional = sockRepository.findByColorAndCottonPart(BLACK, 15);

        Sock savedSock = savedSockOptional.orElseThrow(() ->
                new IllegalStateException("Expected a value, but Optional was empty"));

        assertEquals(3, savedSock.getId());
        assertEquals(200, savedSock.getQuantity());
    }

    @Test
    @DisplayName("Should return list of sorted entities according to parameters provided")
    void testGetFilteredAndSortedSocks_Success() throws Exception {

        jdbcTemplate.update("INSERT INTO sock (color, cotton_part, quantity) VALUES (?, ?, ?)",
                "RED", 75, 10);
        jdbcTemplate.update("INSERT INTO sock (color, cotton_part, quantity) VALUES (?, ?, ?)",
                "GREEN", 50, 15);

        mockMvc.perform(get("/api/socks/sorted")
                        .param("cottonPartMin", "45")
                        .param("cottonPartMax", "75")
                        .param("sortBy", "cottonPart")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].color").value("RED"))
                .andExpect(jsonPath("$[0].cottonPart").value(75))
                .andExpect(jsonPath("$[1].id").value(5))
                .andExpect(jsonPath("$[1].color").value("GREEN"))
                .andExpect(jsonPath("$[1].cottonPart").value(50))
                .andExpect(jsonPath("$[2].id").value(2))
                .andExpect(jsonPath("$[2].color").value("PURPLE"))
                .andExpect(jsonPath("$[2].cottonPart").value(45))
                .andDo(MockMvcResultHandlers.print());
    }
}
