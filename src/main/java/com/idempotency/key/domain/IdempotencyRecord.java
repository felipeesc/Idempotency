package com.idempotency.key.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.time.Instant;


@Entity
@Table(name = "idempotency", uniqueConstraints = @UniqueConstraint(columnNames = "key_value"))
@Getter @Setter
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "key_value", nullable = false, length = 80)
    private String key;


    @Column(name = "request_hash", nullable = false, columnDefinition = "char(64)")
    private String requestHash;


    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "response_body", columnDefinition = "bytea")
    private byte[] responseBody;


    @Column(nullable = false, length = 16)
    private String status;


    @Column(nullable = false)
    private Instant createdAt = Instant.now();


    @Column(nullable = false)
    private Instant updatedAt = Instant.now();


    @PreUpdate void touch(){ this.updatedAt = Instant.now(); }
}