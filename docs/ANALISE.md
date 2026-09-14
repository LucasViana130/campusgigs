# Analise e checklist final - CampusGigs

> **Nota de revisao (2a auditoria)**: alem da nota abaixo (1a auditoria),
> esta versao reflete uma segunda rodada de correcoes: um mismatch real de
> schema (`users.state` `CHAR` vs `VARCHAR`, corrigido com uma nova migration
> `V2` inserida historicamente no CP2) e o encurtamento das justificativas
> dos commits CP1-CP5 para no maximo 3 linhas (ver secao 6).

> **Nota de revisao (1a auditoria)**: este documento foi atualizado apos uma
> auditoria externa que encontrou pontos reais a corrigir. As correcoes
> foram feitas em commits novos, sem alterar o conteudo funcional dos
> commits CP1-CP5 originais - as MENSAGENS desses 5 commits foram
> reescritas (via `git rebase`) para incluir a justificativa exigida pelo
> enunciado, que antes so existia aqui neste arquivo.

## 0. Observacao sobre o ambiente em que este projeto foi gerado

O upload continha apenas o PDF do enunciado e as instrucoes em texto - **nenhum
codigo de projeto existente foi enviado**. Por isso este nao e uma "revisao"
de um projeto seu: o codigo foi construido do zero, checkpoint por checkpoint,
seguindo estritamente o enunciado.

Alem disso, o ambiente onde o codigo foi escrito **nao tem acesso ao Maven
Central e nao tem Docker/daemon disponivel** (so Java 21 e Git). Isso significa:

- O projeto **nao foi compilado** (`mvn compile`/`package`) neste ambiente.
- **Nenhum teste automatizado ou manual foi executado de fato.**
- O `docker compose up` **nao foi executado** aqui.
- Os pontos abaixo marcados como "corrigidos" foram corrigidos **por analise
  estatica de codigo** (leitura de entidades, migrations e configuracao),
  nao por execucao real - isso so acontece quando voce rodar o projeto.

Tudo abaixo reflete isso: nada que dependa de execucao esta marcado como
`[OK]` so por eu ter lido o codigo. Voce PRECISA rodar `docker compose up
--build` na sua maquina e seguir `docs/test-roteiro.md` antes de entregar.

## 1. Analise tecnica

| Item | Valor |
|---|---|
| Java | 21 (decisao de implementacao - projeto novo) |
| Spring Boot | 3.3.5 |
| Build tool | Maven |
| Banco | PostgreSQL 16 (decisao de implementacao - nenhum banco estava definido) |
| Estrutura | `domain`, `repository`, `dto`, `service`, `controller`, `security`, `config`, `client`, `exception` |
| Dependencias principais | spring-boot-starter-web, -data-jpa, -security, -validation, postgresql, flyway-core, flyway-database-postgresql, jjwt-*, lombok |
| Migrations | `V1__create_initial_schema.sql` (CP1), `V2__alter_user_state_to_varchar.sql` (CP2) - ver secao 6 |
| Ja pronto | Todos os requisitos funcionais e regras de negocio do enunciado (ver tabela abaixo) |
| Faltando | Compilar, rodar, testar manualmente (ver secao 3) |
| Problemas encontrados | Um mismatch de schema real (`state` CHAR vs VARCHAR), corrigido - ver secao 6. Fora isso, nenhum no enunciado; a unica limitacao adicional foi a impossibilidade de compilar/executar neste sandbox |

## 2. Tabela de requisitos

| Requisito | Situacao atual | O que precisa ser feito | Checkpoint |
|---|---|---|---|
| Ambiente sobe via Docker | Dockerfile + docker-compose.yml escritos | Rodar `docker compose up --build` e confirmar | CP1 |
| Migration inicial (Flyway) | `V1__create_initial_schema.sql` criada (users/gigs/hirings) | Confirmar que roda sem erro ao subir o app | CP1 |
| Evolucao de schema via Flyway | `V2__alter_user_state_to_varchar.sql` (corrige `state` CHAR->VARCHAR) | Confirmar que ambas as migrations rodam em sequencia | CP2 |
| Cadastro de usuario | `POST /api/auth/register` implementado | Testar via curl | CP2 |
| Senha nunca em texto puro | BCrypt (`PasswordEncoder`) usado no hash | Nenhuma (verificavel no codigo) | CP2 |
| E-mail unico | Constraint `UNIQUE` no banco + checagem na aplicacao (409) | Testar tentativa de e-mail duplicado | CP2 |
| Autenticacao (login) | `POST /api/auth/login` implementado | Testar via curl | CP2/CP3 |
| Emissao/validacao de JWT | `JwtService` + `JwtAuthenticationFilter` implementados | Testar token valido/invalido/ausente | CP3 |
| Endpoint protegido | `GET /api/users/me` | Testar sem token / com token | CP3 |
| Publicar servico | `POST /api/gigs` | Testar via curl | CP4 |
| Listar servicos | `GET /api/gigs` (publico) | Testar via curl | CP4 |
| Contratar servico | `POST /api/hirings` | Testar via curl | CP4 |
| Encerrar servico | `PATCH /api/gigs/{id}/close` | Testar via curl | CP4 |
| USER edita/encerra so o proprio | Checagem de propriedade em `GigService` (403) | Testar cenario negado | CP4 |
| ADMIN encerra qualquer um | Checagem de role em `GigService` (200) | Testar promovendo um usuario a ADMIN | CP4 |
| Nao contratar o proprio servico | Checagem em `HiringService` (400) | Testar via curl | CP4 |
| So contratar servico ATIVO | Checagem em `HiringService` (400) | Testar via curl | CP4 |
| 401 vs 403 diferenciados | `RestAuthenticationEntryPoint` (401) / `RestAccessDeniedHandler` + handler no `GlobalExceptionHandler` (403) | Testar os dois cenarios | CP3/CP4 |
| Tratamento centralizado de erros | `GlobalExceptionHandler` (`@RestControllerAdvice`) | Nenhuma (verificavel no codigo) | CP2-CP5 |
| Integracao de CEP via HttpExchange | `ViaCepClient` (`@HttpExchange` + `@GetExchange`) + `RestClientConfig` | Testar cadastro com CEP valido | CP5 |
| Falha/timeout do servico externo | `CepLookupService` mapeia para 400 (nao encontrado) ou 502 (indisponivel) | Testar CEP inexistente; simular timeout se possivel | CP5 |
| README completo | `README.md` escrito (inclui V1+V2) | Nenhuma | CP5 |
| Roteiro de testes manuais | `docs/test-roteiro.md` escrito, sequencial | **Executar de verdade e guardar evidencia em `docs/evidencias/`** | CP5 |

## 3. Checklist final (comparando com o enunciado)

> Legenda: `[OK]` = verificavel por leitura do codigo/estrutura (nao exige execucao).
> `[PRECISA TESTAR]` = implementado no codigo, mas depende de execucao para confirmar - NAO testado por mim.
> `[PENDENTE]` = ainda nao feito / depende de uma acao sua.

- `[OK]` Estrutura de pacotes organizada (controller/service/repository/entity/dto/security/exception/config/client)
- `[OK]` Sem TODOs, mocks permanentes ou funcionalidades fictícias no codigo
- `[OK]` Senha tratada com BCrypt, nunca em texto puro, nunca retornada nas respostas (DTOs dedicados)
- `[OK]` Prestador/contratante sempre resolvidos via `@AuthenticationPrincipal` (JWT), nunca aceitos do corpo da requisicao
- `[OK]` Autorizacao (role) resolvida via `UserDetailsService` a partir do banco a cada requisicao - nao depende de claim do token
- `[OK]` HttpExchange (`@HttpExchange` no nivel da interface + `@GetExchange` no metodo, via `HttpServiceProxyFactory`) usado para o CEP - nenhum RestTemplate/chamada manual
- `[OK]` Flyway com migrations versionadas (`V1`, `V2`), Hibernate em `ddl-auto: validate`
- `[OK]` Todas as colunas das entidades JPA (`User`, `Gig`, `Hiring`) conferidas uma a uma contra as migrations (tipo, tamanho, precision/scale, nullability, enum) - o unico mismatch encontrado (`state`) foi corrigido na `V2`
- `[OK]` Tratamento centralizado de erros (`@RestControllerAdvice`), sem stack trace/SQL exposto
- `[OK]` Cada um dos 5 commits (CP1-CP5) tem corpo com justificativa de ate 3 linhas (verificavel com `git log`)
- `[PRECISA TESTAR]` Compilacao do projeto (`mvn clean package`) - **nao executada neste ambiente** (sem acesso ao Maven Central)
- `[PRECISA TESTAR]` `docker compose up --build` sobe app + banco, `V1` e `V2` aplicadas em sequencia sem erro de schema-validation - **nao executado neste ambiente** (sem Docker)
- `[PRECISA TESTAR]` Todos os endpoints (registro, login, gigs, hirings, CEP) respondendo como esperado
- `[PENDENTE]` Rodar o roteiro de `docs/test-roteiro.md` (16 passos cobrindo os 15 cenarios do enunciado, incluindo o de acesso negado por papel obrigatorio) e **guardar a evidencia real** em `docs/evidencias/` - o PDF exige essa evidencia, e ela nao existe ainda
- `[PENDENTE]` Configurar `git config user.name`/`user.email` com sua identidade real e reatribuir a autoria dos commits antes do push (ver secao 7 - **nao fiz isso por voce, de proposito**)
- `[PENDENTE]` Dar `git push` para um repositorio seu no GitHub

## 4. Os 5 checkpoints: arquivos, resumo e justificativa

> As justificativas abaixo sao exatamente as que estao no corpo de cada
> commit (`git log` mostra titulo + corpo, no maximo 3 linhas fisicas cada).
> Voce pode reescreve-las com suas proprias palavras antes da entrega, ja
> que o professor exige isso - o conteudo tecnico ja reflete decisoes reais
> do codigo, nao e so uma sugestao solta.

### CP1 - Ambiente Docker + migration inicial
**Arquivos**: `pom.xml`, `Dockerfile`, `docker-compose.yml`, `.env.example`, `.gitignore`, `application.yml`, `V1__create_initial_schema.sql`, `CampusGigsApplication.java`, `README.md` (stub).
**Resumo**: projeto Spring Boot minimo, Postgres via Docker Compose, schema inicial completo (users/gigs/hirings) via Flyway.
**Commit**: `CP1: ambiente Docker + primeira migration Flyway (schema inicial)`
**Justificativa (no corpo do commit)**: "Schema inteiro (users, gigs, hirings) criado ja na V1, pois o dominio completo ja estava definido no enunciado."

### CP2 - Cadastro e autenticacao com senha protegida
**Arquivos**: `User.java`, `Role.java`, `UserRepository.java`, `SecurityConfig.java`, `CustomUserDetailsService.java`, `AuthService.java`, `AuthController.java`, DTOs (`RegisterRequest`, `LoginRequest`, `UserResponse`), `GlobalExceptionHandler.java`, `ApiErrorResponse.java`, `EmailAlreadyInUseException.java`, **`V2__alter_user_state_to_varchar.sql`**.
**Resumo**: cadastro com hash BCrypt e e-mail unico; login validando credenciais via `AuthenticationManager` do Spring Security (ainda sem emitir token). A `V2` entra aqui porque e neste checkpoint que a entidade `User` passa a existir e o Hibernate (`ddl-auto=validate`) comeca a validar a coluna `state` contra o schema.
**Commit**: `CP2: cadastro de usuario e autenticacao com senha protegida (BCrypt)`
**Justificativa (no corpo do commit)**: "Login e cadastro usam AuthenticationManager + UserDetailsService do Spring Security (com BCrypt), preparando o terreno para o JWT no CP3."

### CP3 - Emissao/validacao de JWT
**Arquivos**: `pom.xml` (dependencia JJWT), `application.yml` (jwt.secret/expiration), `JwtService.java`, `JwtAuthenticationFilter.java`, `RestAuthenticationEntryPoint.java`, `RestAccessDeniedHandler.java`, `SecurityConfig.java` (reescrito), `AuthResponse.java`, `AuthService.login()` (atualizado), `UserController.java` (`/api/users/me`).
**Resumo**: login passa a devolver um JWT; um filtro le o header `Authorization`, valida o token e popula o `SecurityContext`; endpoint protegido para validar o fluxo.
**Commit**: `CP3: emissao e validacao de JWT, endpoint protegido (/api/users/me)`
**Justificativa (no corpo do commit)**: "O JWT carrega so o e-mail (subject); a autorizacao de cada requisicao vem do banco via UserDetailsService no JwtAuthenticationFilter, nao do token."
**Nota (arquitetura real)**: o token nunca carregou uma claim de role usada para autorizar nada - a role sempre veio do banco via `UserDetailsService.loadUserByUsername`. Uma claim `"role"` chegou a existir no token entre a 1a e a 2a auditoria, mas nunca foi lida por nenhum ponto do codigo; foi removida por ser informacao morta e potencialmente enganosa (ver secao 6 da revisao anterior no historico do commit `refactor(security)`).

### CP4 - Dominio de servicos/contratacoes e autorizacao por papel
**Arquivos**: `Gig.java`, `GigStatus.java`, `Hiring.java`, `HiringStatus.java`, `GigRepository.java`, `HiringRepository.java`, DTOs de Gig/Hiring, `ResourceNotFoundException.java`, `BusinessRuleViolationException.java`, `GigService.java`, `HiringService.java`, `GigController.java`, `HiringController.java`, `SecurityConfig.java` (GET publico), `GlobalExceptionHandler.java` (novos handlers).
**Resumo**: publicar/listar/editar/encerrar servico e contratar, com todas as regras de propriedade e papel (USER só mexe no proprio, ADMIN encerra qualquer um, ninguem contrata o proprio servico nem um servico inativo).
**Commit**: `CP4: dominio de servicos/contratacoes e regras de autorizacao por papel`
**Justificativa (no corpo do commit)**: "Checagens de dono/ADMIN ficam no service (dependem do dado, nao da rota). Prestador/contratante sempre vem do JWT, nunca do corpo da requisicao."

### CP5 - Integracao de CEP (HttpExchange) e revisao final
**Arquivos**: `ViaCepClient.java`, `ViaCepResponse.java`, `CepLookupService.java`, `CepLookupResult.java`, `RestClientConfig.java`, `CepNotFoundException.java`, `CepServiceUnavailableException.java`, `UpdateCepRequest.java`, `UserService.java`, `UserController.java` (endpoint de CEP), `AuthService.register()` (atualizado), `application.yml` (config de CEP), `GlobalExceptionHandler.java` (novos handlers), `README.md` (final), `docs/test-roteiro.md`.
**Resumo**: cadastro e atualizacao de CEP passam a consultar o ViaCEP via cliente declarativo `@HttpExchange`/`@GetExchange`; falhas sao tratadas sem deixar o cadastro incompleto.
**Commit**: `CP5: integracao de CEP via HttpExchange, tratamento de falha/timeout, README e roteiro de testes`
**Justificativa (no corpo do commit)**: "CEP invalido/inexistente e erro do cliente (400); timeout/indisponibilidade do ViaCEP e erro do servico externo (502), sem expor detalhe tecnico."

## 5. Resumo final

- **Implementado**: todos os requisitos funcionais, regras de negocio, autenticacao/autorizacao, Flyway (V1+V2), Docker, HttpExchange e tratamento de erros do enunciado, ate onde e verificavel por leitura de codigo.
- **Testado de fato**: nada (ambiente sem Maven Central/Docker) - **isso e uma limitacao do ambiente em que o codigo foi gerado, nao uma etapa pulada por escolha**.
- **O que ficou pendente para voce**: compilar/subir (`docker compose up --build`), rodar o roteiro completo em `docs/test-roteiro.md` e **produzir a evidencia real** exigida pelo PDF (em `docs/evidencias/`, commit separado), configurar sua identidade Git (secao 7) antes do push.
- **Commits**: o historico tem os 5 checkpoints obrigatorios (CP1-CP5, cada um com justificativa no corpo) mais commits adicionais de documentacao/correcao de auditoria. O numero exato muda a cada rodada de correcao - confira sempre com `git log --oneline | wc -l` em vez de confiar em um numero fixo aqui.
- **Comandos para rodar**: `cp .env.example .env && docker compose up --build`.
- **Pontos do PDF nao atendidos**: no nivel de codigo, nenhum requisito funcional/tecnico ficou sem implementacao correspondente. **Porem**, o PDF exige explicitamente evidencia real de testes manuais, e essa evidencia **ainda nao existe** (nada foi executado) - por isso o item "evidencia de testes manuais" permanece `[PENDENTE]` na secao 3, e a entrega **nao deve ser considerada pronta** ate que voce gere essa evidencia de verdade.

## 6. Correcoes desta rodada de auditoria (2a)

1. **Mismatch de schema real: `users.state` `CHAR(2)` vs `VARCHAR`** - a
   entidade `User` mapeia `state` como `String` (`@Column(length = 2)`),
   que o Hibernate espera como `VARCHAR(2)`; a `V1` criou a coluna como
   `CHAR(2)` (`bpchar` no Postgres). Com `ddl-auto=validate`, isso falharia
   a inicializacao com um erro do tipo "wrong column type encountered ...
   found [bpchar (Types#CHAR)], but expecting [varchar(2) (Types#VARCHAR)]".
   Corrigido com uma nova migration, `V2__alter_user_state_to_varchar.sql`
   (`ALTER TABLE users ALTER COLUMN state TYPE VARCHAR(2) USING
   state::VARCHAR(2)`), inserida historicamente no commit do CP2 (onde a
   entidade `User` passa a existir). A `V1` **nao foi alterada**.
2. **Revisao completa entidade-por-entidade** - todas as colunas de `User`,
   `Gig` e `Hiring` foram conferidas uma a uma contra `V1`/`V2` (tipo,
   tamanho, nullability, precision/scale de `BigDecimal`, enums). O unico
   mismatch encontrado foi o do item 1. `description` (`TEXT` +
   `columnDefinition="text"`) foi checado a parte: o driver JDBC do
   Postgres reporta `TEXT` como `Types.VARCHAR` (mesmo codigo que `String`
   espera), entao nao ha conflito ali - diferente do caso `CHAR`, que tem
   um `Types.CHAR` proprio.
3. **Justificativas dos commits CP1-CP5 encurtadas** - a 1a auditoria havia
   corrigido o CONTEUDO das justificativas, mas os corpos ficaram com 4-5
   linhas fisicas (o enunciado pede no maximo 3). Reescritas para 2 linhas
   cada, preservando a mesma decisao tecnica (ver secao 4). Reaproveitei a
   mesma operacao de rebase para inserir a `V2` no CP2, em vez de reescrever
   o historico duas vezes.
4. **Numero de commits fixo removido do resumo final** - a secao 5 citava
   "os 7 commits", que ja estava desatualizado (o historico tinha 11 antes
   desta rodada). Trocado por uma instrucao para conferir com
   `git log --oneline | wc -l`, que nao fica errada com o tempo.
5. **`docs/test-roteiro.md` nao exige mais reescrever o CP5** - a orientacao
   de "anexar a evidencia ao commit do CP5" foi trocada por: salvar em
   `docs/evidencias/` e criar um commit novo depois dos testes.

## 7. Autoria dos commits - acao obrigatoria antes do push

Todos os commits deste repositorio foram criados no sandbox com uma
identidade generica (`CampusGigs Dev <dev@campusgigs.local>`). **Isso nao
deve ser enviado ao professor.** Configure sua identidade real e reatribua
a autoria de TODOS os commits antes de dar push - os comandos exatos estao
na mensagem de entrega (fora deste arquivo), pois dependem do seu nome e
e-mail reais, que eu nao tenho e nao devo inventar.
