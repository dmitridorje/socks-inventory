package org.sellsocks.socksmanagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SockUpdateDto {

    private String color;

    @Min(value = 0, message = "Cotton percentage must be at least 0")
    @Max(value = 100, message = "Cotton percentage must be at most 100")
    private Integer cottonPart;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
