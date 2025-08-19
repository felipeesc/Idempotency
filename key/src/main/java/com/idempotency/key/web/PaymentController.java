package com.idempotency.key.web;

import com.idempotency.key.service.IdempotencyExecutor;
import com.idempotency.key.service.PaymentService;
import com.idempotency.key.web.dto.PaymentCommand;
import com.idempotency.key.web.dto.PaymentDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/pix/payments")
public class PaymentController {
    private final IdempotencyExecutor idem;
    private final PaymentService service;


    public PaymentController(IdempotencyExecutor idem, PaymentService service) {
        this.idem = idem; this.service = service;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto create(@RequestBody @Valid PaymentCommand cmd,
                             @RequestHeader("Idempotency-Key") String idemKey) {
        return idem.execute(idemKey, cmd, PaymentDto.class,
                () -> service.settle(cmd));
    }
}