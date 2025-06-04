package org.sellsocks.socksmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sellsocks.socksmanagement.mapper.SockMapper;
import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.dto.SockUpdateDto;
import org.sellsocks.socksmanagement.model.entity.Sock;
import org.sellsocks.socksmanagement.service.SockService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/socks")
@Validated
@RequiredArgsConstructor
@Tag(name = "Socks Inventory", description = "Operations related to management of socks inventory")
public class SockController {

    private final SockService sockService;
    private final SockMapper sockMapper;

    @GetMapping("/test")
    public String testMe() {
        return "I'm working as intended for testing";
    }

    @Operation(
            summary = "Add incoming socks to inventory",
            description = "This method is used to add new socks to the inventory or update quantities of existing entries."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sock successfully added",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SockDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MethodArgumentNotValidException.class))
    )
    @PostMapping("/income")
    public SockDto incomeSocks(@Valid @RequestBody SockDto sockIncome) {
        return sockService.addSockIncome(sockMapper.toSockEntity(sockIncome));
    }

    @Operation(
            summary = "Subtract outgoing socks from inventory",
            description = "This method is used to subtract socks from the inventory."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sock successfully subtracted",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SockDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MethodArgumentNotValidException.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Not enough socks in inventory",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = IllegalStateException.class))
    )
    @PostMapping("/outcome")
    public SockDto outcomeSocks(@Valid @RequestBody SockDto sockOutcome) {
        return sockService.subtractSockOutcome(sockMapper.toSockEntity(sockOutcome));
    }

    @Operation(
            summary = "Get quantity of socks",
            description = "Get the quantity of socks based on color, operation, and cotton percentage."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Quantity of socks fetched successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class))
    )
    @GetMapping
    public int getNumberOfSocks(
            @Parameter(description = "Color of the socks", required = true)
            @RequestParam
            @Schema(allowableValues = {"RED", "PINK", "GREEN", "PURPLE", "BLACK", "WHITE"}) String color,
            @Parameter(description = "Criteria operation", required = true)
            @RequestParam
            @Schema(allowableValues = {"moreThan", "lessThan", "equal"}) String operation,
            @Parameter(description = "Cotton percentage in the socks", required = true)
            @RequestParam
            @Min(value = 0, message = "Cotton percentage must be at least 0")
            @Max(value = 100, message = "Cotton percentage must be at most 100") Integer cottonPart) {
        return sockService.getFilteredSocksQuantity(color, operation, cottonPart);
    }

    @Operation(
            summary = "Update sock details",
            description = "This method allows updating the details of a sock by its ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sock successfully updated (NB: It will be merged onto another sock " +
                    "if their color and cotton percentage will be identical after update).",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SockDto.class))
    )
    @PutMapping("/{id}")
    public SockDto updateSock(@PathVariable Long id, @Valid @RequestBody SockUpdateDto sockUpdateDto) {
        return sockService.updateSock(id, sockUpdateDto);
    }

    @Operation(
            summary = "Upload a batch of socks",
            description = "This method allows uploading a batch of socks via a CSV file."
    )
    @ApiResponse(
            responseCode = "200",
            description = "File processed successfully (NB: Any entry will be merged onto another sock " +
                    "if their color and cotton percentage will be identical after update)."
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid file format"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error processing the file"
    )
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadSocksBatch(@RequestParam("file") MultipartFile file) {
        sockService.processCsvFile(file);
        return ResponseEntity.ok("File processed successfully");
    }

    @Operation(
            summary = "Get filtered and sorted socks",
            description = "Get a list of socks filtered by color and/or cotton percentage, and sorted by a given attribute."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Filtered and sorted socks fetched successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
    )
    @GetMapping("/sorted")
    public ResponseEntity<List<SockDto>> getFilteredAndSortedSocks(
            @Parameter(description = "Color of the socks") @RequestParam(required = false) String color,
            @Parameter(description = "Minimum cotton percentage") @RequestParam(required = false) Integer cottonPartMin,
            @Parameter(description = "Maximum cotton percentage") @RequestParam(required = false) Integer cottonPartMax,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "color") String sortBy,
            @Parameter(description = "Sorting order (asc or desc)") @RequestParam(defaultValue = "asc") String sortOrder) {

        List<Sock> socks = sockService.getFilteredAndSortedSocks(color, cottonPartMin, cottonPartMax, sortBy, sortOrder);

        List<SockDto> response = socks.stream()
                .map(sockMapper::toSockDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
