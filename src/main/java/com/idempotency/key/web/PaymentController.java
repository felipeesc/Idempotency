package com.idempotency.key.web;

import com.idempotency.key.service.IdempotencyExecutor;
import com.idempotency.key.service.PaymentService;
import com.idempotency.key.web.dto.PaymentCommand;
import com.idempotency.key.web.dto.PaymentDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/pix")
public class PaymentController {
    private final IdempotencyExecutor idem;
    private final PaymentService service;


    public PaymentController(IdempotencyExecutor idem, PaymentService service) {
        this.idem = idem; this.service = service;
    }


        @PostMapping(path = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        public PaymentDto create(@RequestBody PaymentCommand cmd,
                @RequestHeader("Idempotency-Key") String idemKey) {
        return idem.execute(idemKey, cmd, PaymentDto.class,
                () -> service.settle(cmd));
    }
}