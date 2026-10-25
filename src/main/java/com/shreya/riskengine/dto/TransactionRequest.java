package com.shreya.riskengine.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransactionRequest(
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer and 2 decimal digits")
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(INR|USD|EUR|GBP)$", message = "Currency must be one of: INR, USD, EUR, GBP")
    String currency,

    @NotBlank(message = "Channel is required")
    @Pattern(regexp =  "^(ONLINE|BRANCH)$", message = "Channel must be ONLINE or BRANCH")
    String channel,

    @NotNull(message = "International flag is required")
    Boolean international
) {}
