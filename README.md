# CampusGigs API

API REST de uma plataforma de freelas entre alunos de uma universidade
(Projeto Diamante - Java Advanced). Um aluno se cadastra, se autentica,
publica um servico (freela) e outro aluno, autenticado, contrata esse
servico - respeitando autenticacao, autorizacao e regras de negocio.

## Stack

- Java 21 + Spring Boot 3.3.5
- Spring Security + JWT (JJWT 0.12.6)
- Spring Data JPA + PostgreSQL 16
- Flyway (versionamento de schema)
- Docker / Docker Compose
- Spring HttpExchange (RestClient + HttpServiceProxyFactory) para integracao de CEP (ViaCEP)

## Pre-requisitos

- Docker e Docker Compose
- (Para rodar fora do Docker) JDK 21 e Maven 3.9+

## Como executar (Docker)

```bash
cp .env.example .env
# ajuste .env se quiser, principalmente JWT_SECRET em um ambiente real
docker compose up --build
```

A API sobe em `http://localhost:8080` e o Postgres em `localhost:5432`.
As migrations do Flyway rodam automaticamente na inicializacao da aplicacao.

Para parar o ambiente:

```bash
docker compose down          # para os containers, mantem os dados do banco
docker compose down -v       # para os containers E apaga o volume do banco
```

## Como executar sem Docker (opcional)

1. Suba um Postgres local (ou use `docker compose up db` para subir so o banco).
2. Exporte as variaveis de ambiente (ou copie `.env.example` para `.env` e exporte-as) e rode:

```bash
mvn spring-boot:run
```

## Configuracao / variaveis de ambiente

| Variavel              | Descricao                                   | Default (dev)        |
|------------------------|----------------------------------------------|-----------------------|
| `DB_HOST`/`DB_PORT`    | Host/porta do Postgres                        | `localhost` / `5432`  |
| `DB_NAME`              | Nome do banco                                 | `campusgigs`          |
| `DB_USER`/`DB_PASSWORD`| Credenciais do banco                          | `campusgigs`          |
| `JWT_SECRET`           | Chave HS256 (min. 32 caracteres)              | chave de dev insegura |
| `JWT_EXPIRATION_MS`    | Validade do token em milissegundos            | `3600000` (1h)        |
| `CEP_API_BASE_URL`     | Base URL do servico externo de CEP            | `https://viacep.com.br` |

Nunca commite um `.env` real com segredos de producao (ja esta no `.gitignore`).

## Como as migrations funcionam

O schema e 100% controlado pelo Flyway (`src/main/resources/db/migration`),
com o Hibernate em modo `validate` (nunca gera/altera schema sozinho).

- `V1__create_initial_schema.sql`: cria `users`, `gigs` e `hirings`, com
  chaves primarias/estrangeiras, `UNIQUE` de e-mail e `CHECK` para os enums
  de papel/situacao.
- `V2__alter_user_state_to_varchar.sql`: corrige o tipo da coluna
  `users.state` de `CHAR(2)` para `VARCHAR(2)` - a V1 criou a coluna como
  `CHAR(2)`, mas o Hibernate mapeia `String` como `VARCHAR` por padrao, o
  que quebrava `ddl-auto=validate` na inicializacao. A V1 nao foi editada
  retroativamente; a correcao virou uma nova migration, como o proprio
  enunciado pede para qualquer evolucao de schema.

Se o schema precisar mudar no futuro, a convencao e criar uma nova migration
(`V3__...`, `V4__...`); migrations ja aplicadas em checkpoints anteriores
nunca sao editadas retroativamente.

## Papeis (roles)

- `USER`: pode publicar servicos, contratar servicos de outros usuarios,
  editar e encerrar os proprios servicos.
- `ADMIN`: tudo que `USER` pode, alem de encerrar o servico de qualquer usuario.

Todo usuario novo e criado com o papel `USER`. Promover alguem a `ADMIN` e um
processo manual (ex.: `UPDATE users SET role = 'ADMIN' WHERE email = '...'`),
ja que o enunciado nao pede um endpoint de administracao de papeis.

## Integracao de CEP

Ao cadastrar (`POST /api/auth/register`) ou atualizar (`PUT /api/users/me/cep`)
o CEP de um usuario, a API consulta o [ViaCEP](https://viacep.com.br) atraves
de um cliente HTTP **declarativo** (`@HttpExchange`), preenchendo cidade e UF
automaticamente. Ver a secao "Endpoints" para exemplos.

Comportamento em caso de falha (a decisao tecnica por tras disso esta
detalhada na justificativa do commit do CP5):

- **CEP com formato valido mas inexistente** -> `400 Bad Request` claro,
  cadastro/atualizacao NAO e realizado.
- **Timeout ou indisponibilidade do ViaCEP** -> `502 Bad Gateway`, com
  mensagem generica (o erro interno do servico externo nunca e exposto).

## Endpoints principais

Todas as respostas de erro seguem o formato:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Requisicao invalida",
  "message": "Um ou mais campos estao invalidos",
  "path": "/api/auth/register",
  "fieldErrors": { "email": "e-mail invalido" }
}
```

### Cadastrar usuario

```
POST /api/auth/register
Content-Type: application/json

{
  "name": "Maria Souza",
  "email": "maria@ufu.br",
  "password": "senha123",
  "cep": "38400100"
}
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "name": "Maria Souza",
  "email": "maria@ufu.br",
  "cep": "38400100",
  "city": "Uberlandia",
  "state": "MG",
  "role": "USER"
}
```

### Autenticar e obter o JWT

```
POST /api/auth/login
Content-Type: application/json

{
  "email": "maria@ufu.br",
  "password": "senha123"
}
```

Resposta `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "type": "Bearer",
  "expiresInMs": 3600000,
  "user": { "id": 1, "name": "Maria Souza", "...": "..." }
}
```

### Enviando o JWT / exemplo de chamada autenticada

Toda requisicao a um endpoint protegido deve enviar o header:

```
Authorization: Bearer <token>
```

Exemplo completo (publicar um servico):

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9...."

curl -i -X POST http://localhost:8080/api/gigs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Aulas particulares de Calculo 1",
        "description": "Aulas de reforco para a prova final",
        "category": "Aulas",
        "price": 50.00
      }'
```

### Demais endpoints

| Metodo | Rota                     | Protegido? | Regra de autorizacao                                 |
|--------|--------------------------|------------|--------------------------------------------------------|
| POST   | `/api/auth/register`     | Nao        | -                                                        |
| POST   | `/api/auth/login`        | Nao        | -                                                        |
| GET    | `/api/users/me`          | Sim        | usuario autenticado                                      |
| PUT    | `/api/users/me/cep`      | Sim        | usuario autenticado (so o proprio CEP)                   |
| POST   | `/api/gigs`              | Sim        | qualquer usuario autenticado                             |
| GET    | `/api/gigs`              | Nao        | publico (filtro opcional `?status=ATIVO`)                |
| GET    | `/api/gigs/{id}`         | Nao        | publico                                                  |
| PUT    | `/api/gigs/{id}`         | Sim        | apenas o prestador dono do servico                       |
| PATCH  | `/api/gigs/{id}/close`   | Sim        | prestador dono OU `ADMIN`                                |
| POST   | `/api/hirings`           | Sim        | qualquer usuario autenticado, exceto no proprio servico  |

## Roteiro de testes manuais

Veja [`docs/COMO-TESTAR.md`](docs/COMO-TESTAR.md) para o passo a passo
completo (identidade Git, subir o ambiente, rodar os testes, guardar
evidencia e revisar antes do push), e
[`docs/test-roteiro.md`](docs/test-roteiro.md) para os comandos `curl` dos
16 cenarios em si, cobrindo todos os casos exigidos pelo enunciado
(incluindo o obrigatorio de acesso negado por papel).

## Decisoes de implementacao (nao especificadas explicitamente no enunciado)

- **Banco de dados**: PostgreSQL (nenhum banco estava definido previamente).
- **API de CEP**: ViaCEP, publica e gratuita (nenhuma API estava definida previamente).
- **Biblioteca JWT**: JJWT 0.12.x.
- **Nomes de rota**: `/api/gigs` e `/api/hirings` (nao havia rotas pre-existentes a preservar).
- **Contratacao**: criada com situacao inicial `SOLICITADA`. O enunciado nao
  pede endpoints para transicionar `ACEITA -> CONCLUIDA -> CANCELADA`, entao
  eles nao foram implementados para nao inventar requisitos alem do pedido.
