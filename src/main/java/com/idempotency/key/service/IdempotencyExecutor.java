package com.idempotency.key.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idempotency.key.domain.IdempotencyRecord;
import com.idempotency.key.repo.IdempotencyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.function.Supplier;


@Service
public class IdempotencyExecutor {
    private final IdempotencyRepository repo;

    private final ObjectMapper mapper;

    public IdempotencyExecutor(IdempotencyRepository repo, ObjectMapper mapper) { this.repo = repo;
        this.mapper = mapper;
    }


    @Transactional
    public <T> T execute(String key, Object requestBody, Class<T> responseType, Supplier<T> action) {
        final String reqHash = sha256(json(requestBody));


        var existing = repo.findByKey(key).orElse(null);
        if (existing != null) {
            return getReqHash(responseType, reqHash, existing);
        }


        var rec = new IdempotencyRecord();
        rec.setKey(key);
        rec.setRequestHash(reqHash);
        rec.setStatus("IN_PROGRESS");
        try {
            repo.saveAndFlush(rec);
        } catch (DataIntegrityViolationException e) {
            var r = repo.findByKey(key).orElseThrow();
            return getReqHash(responseType, reqHash, r);
        }


        T result = action.get();
        rec.setResponseBody(jsonBytes(result));
        rec.setStatus("DONE");
        rec.setUpdatedAt(Instant.now());
        repo.save(rec);


        return result;
    }

    private <T> T getReqHash(Class<T> responseType, String reqHash, IdempotencyRecord r) {
        if (!r.getRequestHash().equals(reqHash)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key reutilizada com payload diferente");
        }
        if ("DONE".equals(r.getStatus()) && r.getResponseBody() != null) {
            return deserialize(r.getResponseBody(), responseType);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Requisição idêntica em processamento");
    }


    private String json(Object o) {
        try { return mapper.writeValueAsString(o); } catch (Exception e) { throw new RuntimeException(e); }
    }
    private byte[] jsonBytes(Object o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private <T> T deserialize(byte[] data, Class<T> type) {
        try { return mapper.readValue(data, type); } catch (Exception e) { throw new RuntimeException(e); }
    }
    private String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
