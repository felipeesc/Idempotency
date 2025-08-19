package com.idempotency.key.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.math.BigDecimal;


@Data
public class PaymentCommand {
    @NotBlank
    private String txid;
    @NotNull
    private BigDecimal amount;
}
