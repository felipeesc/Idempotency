# Idempotency (Spring Boot + Postgres + Docker)

Exemplo de **idempotência** em API de pagamentos (`/pix/payments`) usando **Spring Boot 3 / Java 21 / JPA** com **Postgres**. A mesma requisição (mesma `Idempotency-Key` + mesmo payload) retorna **sempre** a mesma resposta; payload diferente com a mesma chave gera **409**.

---

## Requisitos

* Docker e Docker Compose
* `curl` (e opcionalmente `jq` para formatar JSON)

---

## Subir e testar

### Subir tudo

```bash
  docker compose build && docker compose up -d
```

### 1ª requisição (cria e persiste a resposta)

```bash
   curl -s -X POST http://localhost:8080/pix/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-123' \
  -d '{"txid":"TX1","amount": 10.50}' | jq
```

### 2ª requisição idêntica (mesma chave + payload) → retorna a MESMA resposta

```bash
    curl -s -X POST http://localhost:8080/pix/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-123' \
  -d '{"txid":"TX1","amount": 10.50}' | jq
```

### 3ª requisição com a mesma chave mas payload diferente → 409

```bash
    curl -i -X POST http://localhost:8080/pix/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-123' \
  -d '{"txid":"TX1","amount": 99.99}'
```


---

## Endpoint

* `POST /pix/payments`
  **Headers:** `Content-Type: application/json`, `Idempotency-Key: <unique>`
  **Body:** `{ "txid": "TX1", "amount": 10.50 }`

**Resposta (exemplo)**

```json
{
  "id": "b3c9…",
  "status": "SETTLED",
  "e2eId": "E2E-TX1",
  "amount": 10.5,
  "createdAt": "2025-08-19T08:00:00Z"
}
```

---

## Estrutura do projeto

```
idempotency-demo/
├─ docker-compose.yml
├─ Dockerfile
├─ pom.xml
├─ README.md
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/idempotency/
│  │  │  ├─ IdempotencyDemoApplication.java
│  │  │  ├─ config/
│  │  │  │  └─ JacksonConfig.java                 
│  │  │  ├─ domain/IdempotencyRecord.java
│  │  │  ├─ repo/IdempotencyRepository.java
│  │  │  ├─ service/IdempotencyExecutor.java
│  │  │  ├─ service/PaymentService.java
│  │  │  ├─ web/PaymentController.java
│  │  │  └─ web/dto/{PaymentCommand.java,PaymentDto.java}
│  │  └─ resources/
│  │     ├─ application.yml
│  │     └─ db/migration/
│  │        ├─ V1__idempotency.sql
│  │        └─ V2__resize_request_hash.sql       
```

---

## Configuração

* **Postgres**: via Docker (db `idempotency`, user `idemp`, senha `idemp`).
* **Flyway** cria a tabela `idempotency` na subida.
* **App** porta `:8080`

---

## Como funciona a idempotência (resumo)

1. O cliente envia `Idempotency-Key` + payload.
2. A API calcula um **hash** do payload, busca a chave:

    * se **existe** e **bate o hash**: retorna a **mesma resposta** persistida;
    * se **existe** e **hash difere**: **409**;
    * se **não existe**: cria `IN_PROGRESS`, executa a ação, persiste a resposta e marca `DONE`.

---

