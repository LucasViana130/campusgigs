# CampusGigs API

API REST da plataforma CampusGigs (Projeto Diamante - Java Advanced).

> Este README esta em construcao. A versao completa (com endpoints, exemplos de
> autenticacao e integracao de CEP) sera adicionada no Checkpoint 5.

## Stack

- Java 21 + Spring Boot 3.3.5
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Flyway
- Docker / Docker Compose
- Spring HttpExchange (integracao de CEP)

## Como subir o ambiente (Docker)

```bash
cp .env.example .env
docker compose up --build
```

A aplicacao sobe em `http://localhost:8080` e o Postgres em `localhost:5432`.
As migrations do Flyway sao executadas automaticamente na inicializacao.
