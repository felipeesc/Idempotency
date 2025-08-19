package com.idempotency.key.service;

import com.idempotency.key.web.dto.PaymentCommand;
import com.idempotency.key.web.dto.PaymentDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.UUID;


@Service
public class PaymentService {
    @Transactional
    public PaymentDto settle(PaymentCommand cmd){
        return new PaymentDto(
                UUID.randomUUID().toString(),
                "SETTLED",
                "E2E-" + cmd.getTxid(),
                cmd.getAmount(),
                Instant.now()
        );
    }
}
