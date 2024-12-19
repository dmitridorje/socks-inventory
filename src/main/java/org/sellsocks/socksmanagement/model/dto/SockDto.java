package org.sellsocks.socksmanagement.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SockDto {

    @Schema(hidden = true)
    private Long id;

    @NotBlank(message = "Color must not be blank")
    private String color;

    @NotNull(message = "Cotton percentage must not be null")
    @Min(value = 0, message = "Cotton percentage must be at least 0")
    @Max(value = 100, message = "Cotton percentage must be at most 100")
    private Integer cottonPart;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
