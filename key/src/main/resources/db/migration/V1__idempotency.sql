create table if not exists idempotency (
                                           id bigserial primary key,
                                           key_value varchar(80) not null,
    request_hash char(64) not null,
    response_body bytea,
    status varchar(16) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique(key_value)
    );