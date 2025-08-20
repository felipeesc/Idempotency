package com.idempotency.key.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String id;
    private String status;
    private String e2eId;
    private BigDecimal amount;
    private Instant createdAt;
}
