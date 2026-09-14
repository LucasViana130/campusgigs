# Analise e checklist final - CampusGigs

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
| Ja pronto | Todos os requisitos funcionais e regras de negocio do enunciado (ver tabela abaixo) |
| Faltando | Compilar, rodar, testar manualmente (ver secao 3) |
| Problemas encontrados | Nenhum no enunciado; a unica limitacao foi a impossibilidade de compilar/executar neste sandbox |

## 2. Tabela de requisitos

| Requisito | Situacao atual | O que precisa ser feito | Checkpoint |
|---|---|---|---|
| Ambiente sobe via Docker | Dockerfile + docker-compose.yml escritos | Rodar `docker compose up --build` e confirmar | CP1 |
| Migration inicial (Flyway) | `V1__create_initial_schema.sql` criada (users/gigs/hirings) | Confirmar que roda sem erro ao subir o app | CP1 |
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
| Integracao de CEP via HttpExchange | `ViaCepClient` (`@HttpExchange`) + `RestClientConfig` | Testar cadastro com CEP valido | CP5 |
| Falha/timeout do servico externo | `CepLookupService` mapeia para 400 (nao encontrado) ou 502 (indisponivel) | Testar CEP inexistente; simular timeout se possivel | CP5 |
| README completo | `README.md` escrito | Nenhuma | CP5 |
| Roteiro de testes manuais | `docs/test-roteiro.md` escrito | **Executar de verdade e guardar evidencia** | CP5 |

## 3. Checklist final (comparando com o enunciado)

> Legenda: `[OK]` = verificavel por leitura do codigo/estrutura (nao exige execucao).
> `[ATENCAO]` = implementado no codigo, mas depende de execucao para confirmar - NAO testado por mim.
> `[PENDENTE]` = ainda depende de uma acao sua.

- `[OK]` Estrutura de pacotes organizada (controller/service/repository/entity/dto/security/exception/config/client)
- `[OK]` Sem TODOs, mocks permanentes ou funcionalidades fictícias no codigo
- `[OK]` Senha tratada com BCrypt, nunca em texto puro, nunca retornada nas respostas (DTOs dedicados)
- `[OK]` Prestador/contratante sempre resolvidos via `@AuthenticationPrincipal` (JWT), nunca aceitos do corpo da requisicao
- `[OK]` HttpExchange (`@HttpExchange`/`HttpServiceProxyFactory`) usado para o CEP - nenhum RestTemplate/chamada manual
- `[OK]` Flyway com migration versionada (`V1__...`), Hibernate em `ddl-auto: validate`
- `[OK]` Tratamento centralizado de erros (`@RestControllerAdvice`), sem stack trace/SQL exposto
- `[ATENCAO]` Compilacao do projeto (`mvn clean package`) - **nao executada neste ambiente** (sem acesso ao Maven Central)
- `[ATENCAO]` `docker compose up --build` sobe app + banco - **nao executado neste ambiente** (sem Docker)
- `[ATENCAO]` Migration realmente aplicada com sucesso contra um Postgres real - **nao executado**
- `[PENDENTE]` Rodar o roteiro de `docs/test-roteiro.md` (os 15 cenarios, incluindo o de acesso negado por papel obrigatorio) e guardar a evidencia
- `[PENDENTE]` Rodar `git log --oneline` no seu ambiente e confirmar os 5 commits (ja estao neste repositorio, so falta voce dar push)
- `[PENDENTE]` Escrever, com suas proprias palavras, as justificativas dos commits (sugestoes na secao 4)

## 4. Os 5 checkpoints: arquivos, resumo e sugestao de justificativa

### CP1 - Ambiente Docker + migration inicial
**Arquivos**: `pom.xml`, `Dockerfile`, `docker-compose.yml`, `.env.example`, `.gitignore`, `application.yml`, `V1__create_initial_schema.sql`, `CampusGigsApplication.java`, `README.md` (stub).
**Resumo**: projeto Spring Boot minimo, Postgres via Docker Compose, schema inicial completo (users/gigs/hirings) via Flyway.
**Mensagem de commit sugerida**: `CP1: ambiente Docker + primeira migration Flyway (schema inicial)`
**Sugestao de justificativa (adapte com suas palavras)**: "Optei por criar o schema inteiro (usuarios, servicos e contratacoes) ja na primeira migration, porque o dominio inteiro ja estava definido no enunciado - assim nao preciso alterar migrations antigas depois, so criar novas se precisar mudar algo."

### CP2 - Cadastro e autenticacao com senha protegida
**Arquivos**: `User.java`, `Role.java`, `UserRepository.java`, `SecurityConfig.java`, `CustomUserDetailsService.java`, `AuthService.java`, `AuthController.java`, DTOs (`RegisterRequest`, `LoginRequest`, `UserResponse`), `GlobalExceptionHandler.java`, `ApiErrorResponse.java`, `EmailAlreadyInUseException.java`.
**Resumo**: cadastro com hash BCrypt e e-mail unico; login validando credenciais via `AuthenticationManager` do Spring Security (ainda sem emitir token).
**Mensagem de commit sugerida**: `CP2: cadastro de usuario e autenticacao com senha protegida (BCrypt)`
**Sugestao de justificativa**: "Usei o AuthenticationManager e o UserDetailsService do proprio Spring Security em vez de comparar senha na mao, porque e o jeito padrao e mais seguro de validar credenciais - isso tambem deixa o projeto pronto pra plugar o JWT no proximo checkpoint sem reescrever essa parte."

### CP3 - Emissao/validacao de JWT
**Arquivos**: `pom.xml` (dependencia JJWT), `application.yml` (jwt.secret/expiration), `JwtService.java`, `JwtAuthenticationFilter.java`, `RestAuthenticationEntryPoint.java`, `RestAccessDeniedHandler.java`, `SecurityConfig.java` (reescrito), `AuthResponse.java`, `AuthService.login()` (atualizado), `UserController.java` (`/api/users/me`).
**Resumo**: login passa a devolver um JWT; um filtro le o header `Authorization`, valida o token e popula o `SecurityContext`; endpoint protegido para validar o fluxo.
**Mensagem de commit sugerida**: `CP3: emissao e validacao de JWT, endpoint protegido (/api/users/me)`
**Sugestao de justificativa**: "O token carrega so o e-mail e a role, nada sensivel - a ideia e que qualquer endpoint consiga saber quem esta logado sem consultar o banco toda hora so pra saber a permissao dele."

### CP4 - Dominio de servicos/contratacoes e autorizacao por papel
**Arquivos**: `Gig.java`, `GigStatus.java`, `Hiring.java`, `HiringStatus.java`, `GigRepository.java`, `HiringRepository.java`, DTOs de Gig/Hiring, `ResourceNotFoundException.java`, `BusinessRuleViolationException.java`, `GigService.java`, `HiringService.java`, `GigController.java`, `HiringController.java`, `SecurityConfig.java` (GET publico), `GlobalExceptionHandler.java` (novos handlers).
**Resumo**: publicar/listar/editar/encerrar servico e contratar, com todas as regras de propriedade e papel (USER só mexe no proprio, ADMIN encerra qualquer um, ninguem contrata o proprio servico nem um servico inativo).
**Mensagem de commit sugerida**: `CP4: dominio de servicos/contratacoes e regras de autorizacao por papel`
**Sugestao de justificativa**: "As checagens de dono/ADMIN ficam no service, nao em anotacao no controller, porque a regra depende do dado (quem publicou aquele servico especifico) e nao so da rota - fica mais facil de ler e testar assim."

### CP5 - Integracao de CEP (HttpExchange) e revisao final
**Arquivos**: `ViaCepClient.java`, `ViaCepResponse.java`, `CepLookupService.java`, `CepLookupResult.java`, `RestClientConfig.java`, `CepNotFoundException.java`, `CepServiceUnavailableException.java`, `UpdateCepRequest.java`, `UserService.java`, `UserController.java` (endpoint de CEP), `AuthService.register()` (atualizado), `application.yml` (config de CEP), `GlobalExceptionHandler.java` (novos handlers), `README.md` (final), `docs/test-roteiro.md`.
**Resumo**: cadastro e atualizacao de CEP passam a consultar o ViaCEP via cliente declarativo `@HttpExchange`; falhas sao tratadas sem deixar o cadastro incompleto.
**Mensagem de commit sugerida**: `CP5: integracao de CEP via HttpExchange, tratamento de falha/timeout, README e roteiro de testes`
**Sugestao de justificativa (para o CP5 o professor pede explicitamente essa explicacao)**: "Separei os dois tipos de falha: se o CEP nao existe, o erro e do usuario (400) e o cadastro nao acontece; se o ViaCEP demorar ou cair, o erro e do servico externo (502) e a mensagem nao expõe o motivo tecnico - em nenhum dos dois casos o usuario fica salvo com cidade/UF em branco."

## 5. Resumo final

- **Implementado**: todos os requisitos funcionais, regras de negocio, autenticacao/autorizacao, Flyway, Docker, HttpExchange e tratamento de erros do enunciado.
- **Testado de fato**: nada (ambiente sem Maven Central/Docker) - **isso e uma limitacao do ambiente em que o codigo foi gerado, nao uma etapa pulada por escolha**.
- **O que ficou pendente para voce**: compilar (`docker compose up --build`), rodar o roteiro completo em `docs/test-roteiro.md`, guardar a evidencia e adaptar as justificativas dos commits com suas palavras.
- **Os 5 commits** ja estao no historico do Git deste projeto (`git log --oneline`); basta voce criar um repositorio no GitHub e dar `git push`.
- **Comandos para rodar**: `cp .env.example .env && docker compose up --build`.
- **Pontos do PDF nao atendidos**: nenhum identificado - todos os itens obrigatorios (JWT, roles, Flyway, Docker, HttpExchange, 5 checkpoints, README, evidencia manual) tem codigo correspondente; o que falta e so a validacao pratica na sua maquina.
