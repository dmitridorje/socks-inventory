package org.sellsocks.socksmanagement.validation;

import org.sellsocks.socksmanagement.model.enums.CriteriaOperation;
import org.sellsocks.socksmanagement.model.enums.SockColor;
import org.springframework.stereotype.Component;

@Component
public class SockParametersValidator {

    public SockColor validateAndParseColor(String color) {
        if (color.isBlank()) {
            throw new IllegalArgumentException("Color must not be blank");
        }

        try {
            return SockColor.valueOf(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sock color: " + color);
        }
    }

    public int validateCottonPart(int cottonPart) {
        if (cottonPart < 0 || cottonPart > 100) {
            throw new IllegalArgumentException("Cotton percentage must be between 0 and 100");
        }
        return cottonPart;
    }

    public int validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        return quantity;
    }

    public CriteriaOperation validateAndParseOperation(String operation) {
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("Operation must not be null or blank " +
                    "and must be one of the following: moreThan, lessThan, equal.");
        }

        try {
            return CriteriaOperation.valueOf(operation.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid operation: " + operation +
                    ". Valid values: moreThan, lessThan, equal.");
        }
    }
}
