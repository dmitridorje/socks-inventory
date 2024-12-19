package org.sellsocks.socksmanagement.validation;

import org.junit.jupiter.api.Test;
import org.sellsocks.socksmanagement.model.enums.CriteriaOperation;
import org.sellsocks.socksmanagement.model.enums.SockColor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SockParametersValidatorTest {

    private final SockParametersValidator validator = new SockParametersValidator();

    @Test
    void shouldReturnSockColorWhenValidColorIsProvided() {
        String color = "RED";

        SockColor result = validator.validateAndParseColor(color);

        assertEquals(SockColor.RED, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenColorIsBlank() {
        String color = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateAndParseColor(color));

        assertEquals("Color must not be blank", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidColorIsProvided() {
        String color = "INVALID_COLOR";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateAndParseColor(color));

        assertEquals("Invalid sock color: INVALID_COLOR", exception.getMessage());
    }

    @Test
    void shouldReturnSockColorWhenColorIsUppercase() {
        String color = "green";

        SockColor result = validator.validateAndParseColor(color);

        assertEquals(SockColor.GREEN, result);
    }

    @Test
    void shouldReturnCriteriaOperationWhenValidOperationIsProvided() {
        String operation = "moreThan";

        CriteriaOperation result = validator.validateAndParseOperation(operation);

        assertEquals(CriteriaOperation.MORETHAN, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOperationIsBlank() {
        String operation = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateAndParseOperation(operation));

        assertEquals("Operation must not be null or blank and must be one of the following: moreThan, lessThan, equal.",
                exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidOperationIsProvided() {
        String operation = "invalidOperation";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateAndParseOperation(operation));

        assertEquals("Invalid operation: invalidOperation. Valid values: moreThan, lessThan, equal.",
                exception.getMessage());
    }

    @Test
    void shouldReturnCriteriaOperationWhenOperationIsUppercase() {
        String operation = "lessThan";

        CriteriaOperation result = validator.validateAndParseOperation(operation);

        assertEquals(CriteriaOperation.LESSTHAN, result);
    }
}
